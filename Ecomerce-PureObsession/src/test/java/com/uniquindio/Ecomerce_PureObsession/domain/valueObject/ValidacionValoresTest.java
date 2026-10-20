package com.uniquindio.Ecomerce_PureObsession.domain.valueObject;
import com.uniquindio.Ecomerce_PureObsession.domain.exception.ReglaDominioException;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class ValidacionValoresTest {
    @Test void precioDebeRechazarValoresNoFinitosYNulos() {
        // Arrange / Act & Assert
        assertThrows(ReglaDominioException.class, () -> Precio.crear(null, "COP"));
        assertThrows(ReglaDominioException.class, () -> Precio.crear(Double.NaN, "COP"));
        assertThrows(ReglaDominioException.class, () -> Precio.crear(Double.POSITIVE_INFINITY, "COP"));
    }
    @Test void monedaDebeSerValidaYNormalizada() {
        // Arrange / Act & Assert
        assertThrows(ReglaDominioException.class, () -> Precio.crear(1.0, null));
        assertThrows(ReglaDominioException.class, () -> Precio.crear(1.0, "  "));
        assertThrows(ReglaDominioException.class, () -> Precio.crear(1.0, "PESOS"));
        assertEquals(Precio.crear(1.0, "COP"), Precio.crear(1.0, " cop "));
    }
    @Test void soloElDisponiblePermiteCero() {
        // Arrange / Act & Assert
        assertEquals(0, VolumenDisponible.crear(0).mililitros());
        assertThrows(ReglaDominioException.class, () -> VolumenDisponible.crear(-1));
        assertThrows(ReglaDominioException.class, () -> Volumen.crear(0));
    }
}
