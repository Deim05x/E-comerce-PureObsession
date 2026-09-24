package com.uniquindio.Ecomerce_PureObsession.domain.entity;

import com.uniquindio.Ecomerce_PureObsession.domain.exception.ReglaDominioException;
import com.uniquindio.Ecomerce_PureObsession.domain.valueObject.Concentracion;
import com.uniquindio.Ecomerce_PureObsession.domain.valueObject.FamiliaOlfativa;
import com.uniquindio.Ecomerce_PureObsession.domain.valueObject.PiramideOlfativa;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FraganciaTest {

    @Test
    void dosFraganciasConLaMismaIdentidadSonLaMisma() {
        // Arrange
        UUID mismoId = UUID.randomUUID();
        PiramideOlfativa piramide = new PiramideOlfativa("Bergamota", "Jazmín", "Almizcle");

        Fragancia original = new Fragancia(mismoId, "Sauvage", FamiliaOlfativa.AMADERADA,
                Concentracion.EAU_DE_TPOILETTE, piramide);
        Fragancia otra = new Fragancia(mismoId, "Otro nombre", FamiliaOlfativa.CITRICA,
                Concentracion.EXTRAIT_DE_PARFUM, piramide);

        // Act & Assert
        assertEquals(original, otra); // Entidad: igual por IDENTIDAD (mismo id), datos distintos
    }

    @Test
    void noDebePermitirPublicarUnaFraganciaSinConcentracionDefinida() {
        // Arrange
        PiramideOlfativa piramide = new PiramideOlfativa("Bergamota", "Jazmín", "Almizcle");
        Fragancia fragancia = new Fragancia(UUID.randomUUID(), "Bleu", FamiliaOlfativa.AMADERADA,
                null, piramide);

        // Act & Assert
        assertThrows(ReglaDominioException.class, fragancia::publicar); // regla protegida
    }
}
