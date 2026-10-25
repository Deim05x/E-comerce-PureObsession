package com.uniquindio.Ecomerce_PureObsession.application.usecase;

import com.uniquindio.Ecomerce_PureObsession.application.port.out.CuentaRepository;
import com.uniquindio.Ecomerce_PureObsession.domain.entity.Pedido;
import com.uniquindio.Ecomerce_PureObsession.domain.exception.ReglaDominioException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/** Autorización por titular de cuenta o clave de invitado, independiente de HTTP. */
final class AutorizacionPedido {
    private AutorizacionPedido() {}
    static void verificar(Pedido pedido, String actor, String clave, CuentaRepository cuentas) {
        if (!pedido.esInvitado()) {
            if (actor == null || actor.isBlank()) throw new ReglaDominioException("No autorizado para este pedido.");
            var cuenta = cuentas.buscarPorEmail(actor)
                    .orElseThrow(() -> new ReglaDominioException("No autorizado para este pedido."));
            if (!pedido.getClienteId().equals(cuenta.getId()))
                throw new ReglaDominioException("No autorizado para este pedido.");
            return;
        }
        if (clave == null || clave.isBlank() || pedido.getClaveConsultaHash() == null)
            throw new ReglaDominioException("Clave de consulta requerida.");
        byte[] esperado = pedido.getClaveConsultaHash().getBytes(StandardCharsets.UTF_8);
        byte[] recibido = ClaveConsultaPedido.hash(clave).getBytes(StandardCharsets.UTF_8);
        if (!MessageDigest.isEqual(esperado, recibido))
            throw new ReglaDominioException("No autorizado para este pedido.");
    }
}
