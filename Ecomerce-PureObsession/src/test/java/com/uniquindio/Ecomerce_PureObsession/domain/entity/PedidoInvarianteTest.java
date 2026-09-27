package com.uniquindio.Ecomerce_PureObsession.domain.entity;

import com.uniquindio.Ecomerce_PureObsession.domain.exception.ReglaDominioException;
import com.uniquindio.Ecomerce_PureObsession.domain.valueObject.Precio;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class PedidoInvarianteTest {

    private LineaPedido unaLinea(double precio, int cantidad) {
        return new LineaPedido(UUID.randomUUID(), UUID.randomUUID(), "One Million 10ml",
                new Precio(precio, "COP"), cantidad);
    }

    @Test
    void noDebePermitirCrearUnPedidoSinAlMenosUnaLinea() {
        assertThrows(ReglaDominioException.class, () ->
                Pedido.crear(UUID.randomUUID(), UUID.randomUUID(), List.of()));
    }

    @Test
    void elTotalDelPedidoDebeSerSiempreLaSumaDeLosSubtotalesDeSusLineas() {
        LineaPedido linea1 = unaLinea(50000.0, 2); // 100.000
        LineaPedido linea2 = unaLinea(30000.0, 1); // 30.000

        Pedido pedido = Pedido.crear(UUID.randomUUID(), UUID.randomUUID(), List.of(linea1, linea2));

        assertEquals(130000.0, pedido.getTotal().precio());
    }

    @Test
    void noDebePermitirAgregarLineasAUnPedidoQueYaFueConfirmado() {
        Pedido pedido = Pedido.crear(UUID.randomUUID(), UUID.randomUUID(), List.of(unaLinea(50000.0, 1)));
        pedido.confirmar();

        assertThrows(ReglaDominioException.class, () -> pedido.agregarLinea(unaLinea(20000.0, 1)));
    }

    @Test
    void noDebePermitirMarcarEntregadoUnPedidoQueNoHaSidoEnviado() {
        Pedido pedido = Pedido.crear(UUID.randomUUID(), UUID.randomUUID(), List.of(unaLinea(50000.0, 1)));
        pedido.confirmar();

        assertThrows(ReglaDominioException.class, pedido::marcarEntregado);
    }

    @Test
    void soloUnPedidoEntregadoDebePermitirResena() {
        Pedido pedido = Pedido.crear(UUID.randomUUID(), UUID.randomUUID(), List.of(unaLinea(50000.0, 1)));

        assertFalse(pedido.permiteResena());

        pedido.confirmar();
        pedido.marcarEnviado();
        pedido.marcarEntregado();

        assertTrue(pedido.permiteResena());
    }
}