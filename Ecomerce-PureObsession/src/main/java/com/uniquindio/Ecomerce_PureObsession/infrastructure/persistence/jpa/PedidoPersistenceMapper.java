package com.uniquindio.Ecomerce_PureObsession.infrastructure.persistence.jpa;

import com.uniquindio.Ecomerce_PureObsession.domain.entity.*;
import com.uniquindio.Ecomerce_PureObsession.domain.valueObject.*;
import java.util.List;

public final class PedidoPersistenceMapper {
    private PedidoPersistenceMapper() {}

    public static PedidoJpa aJpa(Pedido pedido) {
        PedidoJpa j = new PedidoJpa();
        j.id = pedido.getId(); j.clienteId = pedido.getClienteId();
        j.estado = pedido.getEstado(); j.claveConsultaHash = pedido.getClaveConsultaHash();
        if (pedido.getEntrega() != null) {
            j.nombreEntrega = pedido.getEntrega().nombre(); j.correoEntrega = pedido.getEntrega().correo();
            j.telefonoEntrega = pedido.getEntrega().telefono(); j.direccionEntrega = pedido.getEntrega().direccion();
        }
        for (LineaPedido linea : pedido.getLineas()) {
            LineaJpa l = new LineaJpa();
            l.id = linea.getId(); l.itemId = linea.getItemId(); l.fraganciaId = linea.getFraganciaId();
            l.vendedorId = linea.getVendedorId(); l.descripcion = linea.getDescripcionFragancia();
            l.moneda = linea.getPrecioUnitario().moneda(); l.precio = linea.getPrecioUnitario().precio();
            l.cantidad = linea.getCantidad(); j.lineas.add(l);
        }
        return j;
    }

    public static Pedido aDominio(PedidoJpa j) {
        List<LineaPedido> lineas = j.lineas.stream().map(l -> l.vendedorId == null
                ? LineaPedido.crear(l.id, l.itemId, l.fraganciaId, l.descripcion, Precio.crear(l.precio, l.moneda), l.cantidad)
                : LineaPedido.crearConVendedor(l.id, l.itemId, l.fraganciaId, l.vendedorId,
                        l.descripcion, Precio.crear(l.precio, l.moneda), l.cantidad)).toList();
        if (j.nombreEntrega == null) return Pedido.reconstituir(j.id, j.clienteId, lineas, j.estado);
        DatosEntrega entrega = new DatosEntrega(j.nombreEntrega, j.correoEntrega, j.telefonoEntrega, j.direccionEntrega);
        return Pedido.reconstituir(j.id, j.clienteId, entrega, j.claveConsultaHash, lineas, j.estado);
    }
}
