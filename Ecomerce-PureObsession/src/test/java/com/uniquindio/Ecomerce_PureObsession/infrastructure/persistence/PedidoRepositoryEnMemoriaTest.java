package com.uniquindio.Ecomerce_PureObsession.infrastructure.persistence;
import com.uniquindio.Ecomerce_PureObsession.domain.entity.*;
import org.junit.jupiter.api.Test;
import java.util.*;
import static com.uniquindio.Ecomerce_PureObsession.DatosPrueba.*;
import static org.junit.jupiter.api.Assertions.*;
class PedidoRepositoryEnMemoriaTest {
    @Test void debeGuardarBuscarYFiltrarPorCliente() {
        // Arrange
        var repo = new PedidoRepositoryEnMemoria(); var cliente = UUID.randomUUID();
        var pedido = Pedido.crear(UUID.randomUUID(), cliente, List.of(linea(UUID.randomUUID(), "COP")));
        // Act
        repo.guardar(pedido);
        // Assert
        assertEquals(pedido, repo.buscarPorId(pedido.getId()).orElseThrow());
        assertEquals(List.of(pedido), repo.listarPorCliente(cliente));
        assertTrue(repo.listarPorCliente(UUID.randomUUID()).isEmpty());
    }
    @Test void debeConsultarFraganciaEItemSoloEnPedidosActivos() {
        // Arrange
        var repo = new PedidoRepositoryEnMemoria(); var fragancia = UUID.randomUUID(); var linea = linea(fragancia, "COP");
        var pedido = Pedido.crear(UUID.randomUUID(), UUID.randomUUID(), List.of(linea)); repo.guardar(pedido);
        // Act & Assert
        assertTrue(repo.existePedidoActivoConFragancia(fragancia));
        assertTrue(repo.existePedidoActivoConItem(linea.getItemId()));
        pedido.cancelar(); repo.guardar(pedido);
        assertFalse(repo.existePedidoActivoConFragancia(fragancia));
        assertFalse(repo.existePedidoActivoConItem(linea.getItemId()));
    }
}
