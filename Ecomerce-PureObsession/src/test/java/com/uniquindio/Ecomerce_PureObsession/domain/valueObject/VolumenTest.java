package com.uniquindio.Ecomerce_PureObsession.domain.valueObject;

import com.uniquindio.Ecomerce_PureObsession.domain.exception.ReglaDominioException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class VolumenTest {

    @Test
    void dosVolumenesConLosMismosMililitrosDebenSerIguales() {
        // Arrange
        Volumen v1 = new Volumen(50);
        Volumen v2 = new Volumen(50);

        // Act & Assert
        assertEquals(v1, v2); // Value Object: igual por VALOR (record -> equals generado)
    }

    @Test
    void noDebePermitirCrearUnVolumenMenorOIgualACero() {
        // Arrange & Act & Assert
        assertThrows(ReglaDominioException.class, () -> new Volumen(0));
    }
}
