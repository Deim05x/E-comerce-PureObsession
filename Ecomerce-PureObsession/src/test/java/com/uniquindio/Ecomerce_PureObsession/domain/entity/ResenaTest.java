package com.uniquindio.Ecomerce_PureObsession.domain.entity;

import com.uniquindio.Ecomerce_PureObsession.domain.exception.ReglaDominioException;
import com.uniquindio.Ecomerce_PureObsession.domain.valueObject.Precio;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ResenaTest {

    @Test
    void debePermitirUnaResenaDelClienteCuandoElPedidoFueEntregado() {
        UUID clienteId = UUID.randomUUID();
        Pedido pedido = unPedido(clienteId);
        pedido.confirmar();
        pedido.marcarEnviado();
        pedido.marcarEntregado();

        Resena resena = Resena.crear(UUID.randomUUID(), pedido, clienteId, "Excelente duración.");

        assertEquals(pedido.getId(), resena.getPedidoId());
    }

    @Test
    void noDebePermitirUnaResenaAntesDeLaEntrega() {
        UUID clienteId = UUID.randomUUID();

        assertThrows(ReglaDominioException.class,
                () -> Resena.crear(UUID.randomUUID(), unPedido(clienteId), clienteId, "Excelente duración."));
    }

    private Pedido unPedido(UUID clienteId) {
        LineaPedido linea = LineaPedido.crear(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), "Decant 10 ml",
                new Precio(50000.0, "COP"), 1);
        return Pedido.crear(UUID.randomUUID(), clienteId, List.of(linea));
    }
}
