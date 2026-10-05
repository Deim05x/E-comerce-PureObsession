package com.uniquindio.Ecomerce_PureObsession.domain.entity;
import com.uniquindio.Ecomerce_PureObsession.domain.valueObject.*;
import com.uniquindio.Ecomerce_PureObsession.domain.exception.ReglaDominioException;
import org.junit.jupiter.api.Test;
import java.util.*;
import static com.uniquindio.Ecomerce_PureObsession.DatosPrueba.*;
import static org.junit.jupiter.api.Assertions.*;
class ValidacionAgregadosTest {
    @Test void noDebeAceptarIdentidadesNulas() {
        // Arrange
        var fragancia = fragancia(); var volumen = new Volumen(100);
        // Act & Assert
        assertThrows(ReglaDominioException.class, () -> BotellaMadre.crear(null, fragancia, volumen));
        assertThrows(ReglaDominioException.class, () -> Pedido.crear(null, UUID.randomUUID(), List.of(linea(fragancia.getId(), "COP"))));
    }
    @Test void noDebeMezclarMonedasNiAlterarElPedidoTrasElRechazo() {
        // Arrange
        var id = UUID.randomUUID(); var primera = linea(id, "COP"); var incompatible = linea(id, "USD");
        var pedido = Pedido.crear(UUID.randomUUID(), UUID.randomUUID(), List.of(primera));
        // Act & Assert
        assertThrows(ReglaDominioException.class, () -> pedido.agregarLinea(incompatible));
        assertEquals(1, pedido.getLineas().size());
        assertEquals(100.0, pedido.getTotal().precio());
        assertThrows(ReglaDominioException.class, () -> Pedido.crear(UUID.randomUUID(), UUID.randomUUID(), List.of(primera, incompatible)));
    }
    @Test void noDebeAceptarLineasNulasNiDuplicadas() {
        // Arrange
        var linea = linea(UUID.randomUUID(), "COP");
        var pedido = Pedido.crear(UUID.randomUUID(), UUID.randomUUID(), List.of(linea));
        // Act & Assert
        assertThrows(ReglaDominioException.class, () -> pedido.agregarLinea(null));
        assertThrows(ReglaDominioException.class, () -> pedido.agregarLinea(linea));
        assertEquals(1, pedido.getLineas().size());
    }
    @Test void coleccionesNoDebenPermitirModificarElAgregadoDesdeFuera() {
        // Arrange
        var botella = botella(); botella.crearDecant(UUID.randomUUID(), new Volumen(10));
        var pedido = Pedido.crear(UUID.randomUUID(), UUID.randomUUID(), List.of(linea(UUID.randomUUID(), "COP")));
        // Act & Assert
        assertThrows(UnsupportedOperationException.class, () -> botella.getDecants().clear());
        assertThrows(UnsupportedOperationException.class, () -> pedido.getLineas().clear());
    }
    @Test void noDebeExtraerDeFraganciaInactiva() {
        // Arrange
        var botella = botella(); botella.getFragancia().desactivar();
        // Act & Assert
        assertThrows(ReglaDominioException.class, () -> botella.crearDecant(UUID.randomUUID(), new Volumen(10)));
        assertEquals(100, botella.getVolumenDisponible().mililitros());
    }
    @Test void soloElCompradorPuedeResenar() {
        // Arrange
        var pedido = Pedido.crear(UUID.randomUUID(), UUID.randomUUID(), List.of(linea(UUID.randomUUID(), "COP")));
        pedido.confirmar(); pedido.marcarEnviado(); pedido.marcarEntregado();
        // Act & Assert
        assertThrows(ReglaDominioException.class, () -> Resena.crear(UUID.randomUUID(), pedido, UUID.randomUUID(), "Bien"));
    }
    @Test void sumaDecimalDebeSerCoherenteConLosSubtotales() {
        // Arrange
        var f = UUID.randomUUID();
        var a = LineaPedido.crear(UUID.randomUUID(), UUID.randomUUID(), f, "A", new Precio(0.1, "COP"), 1);
        var b = LineaPedido.crear(UUID.randomUUID(), UUID.randomUUID(), f, "B", new Precio(0.2, "COP"), 1);
        // Act
        var pedido = Pedido.crear(UUID.randomUUID(), UUID.randomUUID(), List.of(a, b));
        // Assert
        assertEquals(0.3, pedido.getTotal().precio());
    }
}
