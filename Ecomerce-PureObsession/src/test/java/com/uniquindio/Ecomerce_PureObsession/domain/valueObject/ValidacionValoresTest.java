package com.uniquindio.Ecomerce_PureObsession.domain.valueObject;
import com.uniquindio.Ecomerce_PureObsession.domain.exception.ReglaDominioException;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class ValidacionValoresTest {
    @Test void precioDebeRechazarValoresNoFinitosYNulos() {
        // Arrange / Act & Assert
        assertThrows(ReglaDominioException.class, () -> new Precio(null, "COP"));
        assertThrows(ReglaDominioException.class, () -> new Precio(Double.NaN, "COP"));
        assertThrows(ReglaDominioException.class, () -> new Precio(Double.POSITIVE_INFINITY, "COP"));
    }
    @Test void monedaDebeSerValidaYNormalizada() {
        // Arrange / Act & Assert
        assertThrows(ReglaDominioException.class, () -> new Precio(1.0, null));
        assertThrows(ReglaDominioException.class, () -> new Precio(1.0, "  "));
        assertThrows(ReglaDominioException.class, () -> new Precio(1.0, "PESOS"));
        assertEquals(new Precio(1.0, "COP"), new Precio(1.0, " cop "));
    }
    @Test void soloElDisponiblePermiteCero() {
        // Arrange / Act & Assert
        assertEquals(0, new VolumenDisponible(0).mililitros());
        assertThrows(ReglaDominioException.class, () -> new VolumenDisponible(-1));
        assertThrows(ReglaDominioException.class, () -> new Volumen(0));
    }
}
