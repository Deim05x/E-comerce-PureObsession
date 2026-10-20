package com.uniquindio.Ecomerce_PureObsession.application.usecase;

import com.uniquindio.Ecomerce_PureObsession.application.dto.request.*;
import com.uniquindio.Ecomerce_PureObsession.application.dto.response.PedidoResponse;
import com.uniquindio.Ecomerce_PureObsession.application.port.in.ComprarPort;
import com.uniquindio.Ecomerce_PureObsession.application.port.out.*;
import com.uniquindio.Ecomerce_PureObsession.domain.entity.*;
import com.uniquindio.Ecomerce_PureObsession.domain.exception.ReglaDominioException;
import com.uniquindio.Ecomerce_PureObsession.domain.repository.PedidoRepository;
import com.uniquindio.Ecomerce_PureObsession.domain.valueObject.*;
import java.util.*;

/** Orquesta checkout; la configuración ejecuta el caso de uso en una sola transacción. */
public final class ComprarUseCase implements ComprarPort {
    private final CuentaRepository cuentas;
    private final PresentacionRepository presentaciones;
    private final PedidoRepository pedidos;
    private final InventarioVentaPort inventario;
    public ComprarUseCase(CuentaRepository cuentas, PresentacionRepository presentaciones,
                          PedidoRepository pedidos, InventarioVentaPort inventario) {
        this.cuentas = cuentas; this.presentaciones = presentaciones;
        this.pedidos = pedidos; this.inventario = inventario;
    }
    @Override public PedidoResponse ejecutar(String actor, CrearPedidoRequest solicitud) {
        if (solicitud == null || solicitud.items() == null || solicitud.items().isEmpty())
            throw new ReglaDominioException("Debe seleccionar al menos una presentación.");
        var cuenta = actor == null || actor.isBlank() ? null : cuentas.buscarPorEmail(actor)
                .orElseThrow(() -> new ReglaDominioException("La cuenta no existe."));
        DatosEntrega entrega = new DatosEntrega(cuenta == null ? solicitud.nombre() : cuenta.getNombre(),
                cuenta == null ? solicitud.correo() : cuenta.getEmail(), solicitud.telefono(), solicitud.direccion());
        List<ItemCheckoutRequest> items = new ArrayList<>(solicitud.items());
        if (items.stream().anyMatch(Objects::isNull))
            throw new ReglaDominioException("Hay un artículo de compra inválido.");
        if (items.stream().anyMatch(item -> item.presentacionId() == null))
            throw new ReglaDominioException("Hay un artículo de compra inválido.");
        items.sort(Comparator.comparing(item -> item.presentacionId().toString()));
        Set<UUID> ids = new HashSet<>();
        List<LineaPedido> lineas = new ArrayList<>();
        for (ItemCheckoutRequest item : items) {
            if (item.presentacionId() == null || item.cantidad() <= 0 || item.cantidad() > 1000)
                throw new ReglaDominioException("El artículo o la cantidad son inválidos.");
            if (!ids.add(item.presentacionId()))
                throw new ReglaDominioException("Una presentación no debe repetirse en el pedido.");
            Presentacion oferta = presentaciones.buscarPorId(item.presentacionId())
                    .orElseThrow(() -> new ReglaDominioException("La presentación no existe."));
            if (!oferta.isPublicada())
                throw new ReglaDominioException("La presentación no está publicada.");
            inventario.reservar(oferta.getId(), item.cantidad());
            String descripcion = "Presentación " + oferta.getTipo() + " de " + oferta.getVolumenMl() + " ml";
            lineas.add(LineaPedido.crearConVendedor(UUID.randomUUID(), oferta.getId(), oferta.getFraganciaId(),
                    oferta.getVendedorId(), descripcion, Precio.crear(oferta.getPrecio().doubleValue(), "COP"), item.cantidad()));
        }
        String clave = cuenta == null ? ClaveConsultaPedido.nuevaClave() : null;
        Pedido pedido = Pedido.crearParaCheckout(UUID.randomUUID(), cuenta == null ? null : cuenta.getId(), entrega,
                clave == null ? null : ClaveConsultaPedido.hash(clave), lineas);
        pedidos.guardar(pedido);
        return PedidoResponse.desde(pedido, clave);
    }
}
