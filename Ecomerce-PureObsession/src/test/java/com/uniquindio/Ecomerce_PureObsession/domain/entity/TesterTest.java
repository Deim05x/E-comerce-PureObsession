package com.uniquindio.Ecomerce_PureObsession.domain.entity;

import com.uniquindio.Ecomerce_PureObsession.domain.exception.ReglaDominioException;
import com.uniquindio.Ecomerce_PureObsession.domain.valueObject.Concentracion;
import com.uniquindio.Ecomerce_PureObsession.domain.valueObject.FamiliaOlfativa;
import com.uniquindio.Ecomerce_PureObsession.domain.valueObject.PiramideOlfativa;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TesterTest {

    private Fragancia unaFragancia() {
        return new Fragancia(UUID.randomUUID(), "One Million", FamiliaOlfativa.AMADERADA,
                Concentracion.EAU_DE_PARFUM, new PiramideOlfativa("Naranja", "Canela", "Cuero"));
    }

    @Test
    void dosTestersConLaMismaIdentidadSonElMismo() {
        // Arrange
        UUID mismoId = UUID.randomUUID();
        Tester original = new Tester(mismoId, unaFragancia());
        Tester otro = new Tester(mismoId, unaFragancia());

        // Act & Assert
        assertEquals(original, otro); // Entidad: igual por IDENTIDAD (mismo id)
    }

    @Test
    void noDebePermitirAplicarEnvolturaDeRegaloAUnTester() {
        // Arrange
        Tester tester = new Tester(UUID.randomUUID(), unaFragancia());

        // Act & Assert
        assertThrows(ReglaDominioException.class, tester::aplicarEnvolturaRegalo); // regla protegida
        assertFalse(tester.isEnvolturaRegalo()); // el estado no cambió
    }
}
