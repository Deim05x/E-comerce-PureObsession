package com.uniquindio.Ecomerce_PureObsession.application.usecase;
import com.uniquindio.Ecomerce_PureObsession.application.dto.request.EliminarFraganciaRequest;
import com.uniquindio.Ecomerce_PureObsession.domain.entity.*;
import com.uniquindio.Ecomerce_PureObsession.domain.exception.ReglaDominioException;
import com.uniquindio.Ecomerce_PureObsession.infrastructure.persistence.*;
import org.junit.jupiter.api.Test;
import java.util.*;
import static com.uniquindio.Ecomerce_PureObsession.DatosPrueba.*;
import static org.junit.jupiter.api.Assertions.*;

class EliminarFraganciaUseCaseTest {
    @Test void noDebeEliminarInventarioConPedidoActivoDeLaFragancia() {
        // Arrange: el id del artículo es distinto del id de la fragancia.
        var botellas = new BotellaMadreRepositoryEnMemoria(); var pedidos = new PedidoRepositoryEnMemoria();
        var botella = botella(); botellas.guardar(botella);
        var pedido = Pedido.crear(UUID.randomUUID(), UUID.randomUUID(), List.of(linea(botella.getFragancia().getId(), "COP")));
        pedidos.guardar(pedido); var caso = new EliminarFraganciaUseCase(botellas, pedidos);
        // Act & Assert
        assertThrows(ReglaDominioException.class, () -> caso.ejecutar(new EliminarFraganciaRequest(botella.getFragancia().getId())));
        assertTrue(botellas.buscarPorId(botella.getId()).isPresent());
    }
    @Test void debeEliminarTrasCancelarSinBorrarElHistorialDelPedido() {
        // Arrange
        var botellas = new BotellaMadreRepositoryEnMemoria(); var pedidos = new PedidoRepositoryEnMemoria();
        var botella = botella(); botellas.guardar(botella);
        var pedido = Pedido.crear(UUID.randomUUID(), UUID.randomUUID(), List.of(linea(botella.getFragancia().getId(), "COP")));
        pedido.cancelar(); pedidos.guardar(pedido);
        // Act
        var respuesta = new EliminarFraganciaUseCase(botellas, pedidos)
            .ejecutar(new EliminarFraganciaRequest(botella.getFragancia().getId()));
        // Assert
        assertEquals(1, respuesta.botellasEliminadas());
        assertTrue(botellas.buscarPorId(botella.getId()).isEmpty());
        assertTrue(pedidos.buscarPorId(pedido.getId()).isPresent());
        assertEquals("Decant 10 ml", pedido.getLineas().get(0).getDescripcionFragancia());
    }
}
