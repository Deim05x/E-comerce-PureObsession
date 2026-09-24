package com.uniquindio.Ecomerce_PureObsession.domain.valueObject;

import com.uniquindio.Ecomerce_PureObsession.domain.exception.ReglaDominioException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PrecioTest {

    @Test
    void dosPreciosConElMismoValorYMonedaDebenSerIguales() {
        // Arrange
        Precio p1 = new Precio(85000.0, "COP");
        Precio p2 = new Precio(85000.0, "COP");

        // Act & Assert
        assertEquals(p1, p2); // Value Object: igual por VALOR
    }

    @Test
    void noDebePermitirCrearUnPrecioNegativo() {
        // Arrange & Act & Assert
        assertThrows(ReglaDominioException.class, () -> new Precio(-100.0, "COP"));
    }
}
