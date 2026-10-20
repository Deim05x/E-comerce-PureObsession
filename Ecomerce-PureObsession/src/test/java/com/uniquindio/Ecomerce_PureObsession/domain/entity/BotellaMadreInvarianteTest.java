package com.uniquindio.Ecomerce_PureObsession.domain.entity;

import com.uniquindio.Ecomerce_PureObsession.domain.exception.ReglaDominioException;
import com.uniquindio.Ecomerce_PureObsession.domain.valueObject.Concentracion;
import com.uniquindio.Ecomerce_PureObsession.domain.valueObject.FamiliaOlfativa;
import com.uniquindio.Ecomerce_PureObsession.domain.valueObject.PiramideOlfativa;
import com.uniquindio.Ecomerce_PureObsession.domain.valueObject.Volumen;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BotellaMadreInvarianteTest {

    private Fragancia unaFragancia() {
        return Fragancia.crear(UUID.randomUUID(), "One Million", FamiliaOlfativa.AMADERADA,
                Concentracion.EAU_DE_PARFUM, PiramideOlfativa.crear("Naranja", "Canela", "Cuero"));
    }

    @Test
    void noDebePermitirExtraerUnDecantSiElVolumenSolicitadoSuperaElDisponible() {
        // Arrange
        BotellaMadre botella = BotellaMadre.crear(UUID.randomUUID(), unaFragancia(), Volumen.crear(100));
        botella.extraerParaDecant(Volumen.crear(70)); // deja 30 ml disponibles

        // Act & Assert
        assertThrows(ReglaDominioException.class, () -> botella.extraerParaDecant(Volumen.crear(50)));
        assertEquals(30, botella.getVolumenDisponible().mililitros()); // el estado no cambió tras el rechazo
    }

    @Test
    void noDebePermitirCrearUnDecantConVolumenIgualOMayorAlDeLaBotellaMadre() {
        // Arrange
        BotellaMadre botella = BotellaMadre.crear(UUID.randomUUID(), unaFragancia(), Volumen.crear(100));

        // Act & Assert
        assertThrows(ReglaDominioException.class, () ->
                Decant.crearDesdeBotellaMadre(UUID.randomUUID(), botella, Volumen.crear(100)));
        assertEquals(100, botella.getVolumenDisponible().mililitros()); // el estado no cambió tras el rechazo
    }
}
