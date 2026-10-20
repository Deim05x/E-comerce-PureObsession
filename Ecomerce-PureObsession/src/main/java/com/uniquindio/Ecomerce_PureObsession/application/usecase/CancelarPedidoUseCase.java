package com.uniquindio.Ecomerce_PureObsession.application.usecase;

import com.uniquindio.Ecomerce_PureObsession.application.dto.response.PedidoResponse;
import com.uniquindio.Ecomerce_PureObsession.application.port.in.CancelarPedidoPort;
import com.uniquindio.Ecomerce_PureObsession.application.port.out.*;
import com.uniquindio.Ecomerce_PureObsession.domain.exception.ReglaDominioException;
import com.uniquindio.Ecomerce_PureObsession.domain.repository.PedidoRepository;
import java.util.UUID;

/** Cancela exclusivamente pedidos CREADOS y libera existencias en la misma transacción. */
public final class CancelarPedidoUseCase implements CancelarPedidoPort {
    private final PedidoRepository pedidos;
    private final CuentaRepository cuentas;
    private final InventarioVentaPort inventario;
    public CancelarPedidoUseCase(PedidoRepository pedidos, CuentaRepository cuentas, InventarioVentaPort inventario) {
        this.pedidos = pedidos; this.cuentas = cuentas; this.inventario = inventario;
    }
    @Override public PedidoResponse ejecutar(UUID id, String actor, String clave) {
        var pedido = pedidos.buscarPorId(id).orElseThrow(() -> new ReglaDominioException("El pedido no existe."));
        AutorizacionPedido.verificar(pedido, actor, clave, cuentas);
        pedido.cancelarAntesDePago();
        pedido.getLineas().forEach(linea -> inventario.liberar(linea.getItemId(), linea.getCantidad()));
        pedidos.guardar(pedido);
        return PedidoResponse.desde(pedido, null);
    }
}
