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

class FraganciaTest {

    @Test
    void dosFraganciasConLaMismaIdentidadSonLaMisma() {
        // Arrange
        UUID mismoId = UUID.randomUUID();
        PiramideOlfativa piramide = new PiramideOlfativa("Bergamota", "Jazmín", "Almizcle");

        Fragancia original = Fragancia.crear(mismoId, "Sauvage", FamiliaOlfativa.AMADERADA,
                Concentracion.EAU_DE_TOILETTE, piramide);
        Fragancia otra = Fragancia.crear(mismoId, "Otro nombre", FamiliaOlfativa.CITRICA,
                Concentracion.EXTRAIT_DE_PARFUM, piramide);

        // Act & Assert
        assertEquals(original, otra); // Entidad: igual por IDENTIDAD (mismo id), datos distintos
    }

    @Test
    void noDebePermitirPublicarUnaFraganciaSinConcentracionDefinida() {
        // Arrange
        PiramideOlfativa piramide = new PiramideOlfativa("Bergamota", "Jazmín", "Almizcle");
        Fragancia fragancia = Fragancia.crear(UUID.randomUUID(), "Bleu", FamiliaOlfativa.AMADERADA,
                null, piramide);

        // Act & Assert
        assertThrows(ReglaDominioException.class, fragancia::publicar); // regla protegida
    }

    @Test
    void desactivarDebeRetirarLaFraganciaDelCatalogo() {
        PiramideOlfativa piramide = new PiramideOlfativa("Bergamota", "Jazmín", "Almizcle");
        Fragancia fragancia = Fragancia.crear(UUID.randomUUID(), "Bleu", FamiliaOlfativa.AMADERADA,
                Concentracion.EAU_DE_PARFUM, piramide);
        fragancia.publicar();

        fragancia.desactivar();

        assertFalse(fragancia.isActiva());
        assertFalse(fragancia.isPublicada());
        assertThrows(ReglaDominioException.class, fragancia::publicar);
    }
    @Test
    void noDebePublicarSinPiramide() {
        // Arrange
        Fragancia fragancia = Fragancia.crear(UUID.randomUUID(), "Sauvage", FamiliaOlfativa.AMADERADA,
                Concentracion.EAU_DE_PARFUM, null);
        // Act & Assert
        assertThrows(ReglaDominioException.class, fragancia::publicar);
        assertFalse(fragancia.isPublicada());
    }

    @Test
    void piramideDebeTenerLasTresFases() {
        // Arrange / Act & Assert
        assertThrows(ReglaDominioException.class, () -> new PiramideOlfativa(null, "Jazmin", "Ambar"));
        assertThrows(ReglaDominioException.class, () -> new PiramideOlfativa("Bergamota", " ", "Ambar"));
        assertThrows(ReglaDominioException.class, () -> new PiramideOlfativa("Bergamota", "Jazmin", ""));
    }
}
