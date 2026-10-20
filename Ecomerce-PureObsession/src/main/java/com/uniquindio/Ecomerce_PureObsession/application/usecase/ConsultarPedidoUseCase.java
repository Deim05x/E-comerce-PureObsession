package com.uniquindio.Ecomerce_PureObsession.application.usecase;

import com.uniquindio.Ecomerce_PureObsession.application.dto.response.PedidoResponse;
import com.uniquindio.Ecomerce_PureObsession.application.port.in.ConsultarPedidoPort;
import com.uniquindio.Ecomerce_PureObsession.application.port.out.CuentaRepository;
import com.uniquindio.Ecomerce_PureObsession.domain.exception.ReglaDominioException;
import com.uniquindio.Ecomerce_PureObsession.domain.repository.PedidoRepository;
import java.util.List;
import java.util.UUID;

public final class ConsultarPedidoUseCase implements ConsultarPedidoPort {
    private final PedidoRepository pedidos;
    private final CuentaRepository cuentas;
    public ConsultarPedidoUseCase(PedidoRepository pedidos, CuentaRepository cuentas) {
        this.pedidos = pedidos; this.cuentas = cuentas;
    }
    @Override public PedidoResponse consultar(UUID id, String actor, String clave) {
        var pedido = pedidos.buscarPorId(id).orElseThrow(() -> new ReglaDominioException("El pedido no existe."));
        AutorizacionPedido.verificar(pedido, actor, clave, cuentas);
        return PedidoResponse.desde(pedido, null);
    }
    @Override public List<PedidoResponse> listarMios(String actor) {
        if (actor == null || actor.isBlank()) throw new ReglaDominioException("Inicie sesión.");
        var cuenta = cuentas.buscarPorEmail(actor).orElseThrow(() -> new ReglaDominioException("La cuenta no existe."));
        return pedidos.listarPorCliente(cuenta.getId()).stream().map(pedido -> PedidoResponse.desde(pedido, null)).toList();
    }
}
