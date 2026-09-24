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
        return new Fragancia(UUID.randomUUID(), "One Million", FamiliaOlfativa.AMADERADA,
                Concentracion.EAU_DE_PARFUM, new PiramideOlfativa("Naranja", "Canela", "Cuero"));
    }

    @Test
    void noDebePermitirExtraerUnDecantSiElVolumenSolicitadoSuperaElDisponible() {
        // Arrange
        BotellaMadre botella = new BotellaMadre(UUID.randomUUID(), unaFragancia(), new Volumen(100));
        botella.extraerParaDecant(new Volumen(70)); // deja 30 ml disponibles

        // Act & Assert
        assertThrows(ReglaDominioException.class, () -> botella.extraerParaDecant(new Volumen(50)));
        assertEquals(30, botella.getVolumenDisponible().mililitros()); // el estado no cambió tras el rechazo
    }

    @Test
    void noDebePermitirCrearUnDecantConVolumenIgualOMayorAlDeLaBotellaMadre() {
        // Arrange
        BotellaMadre botella = new BotellaMadre(UUID.randomUUID(), unaFragancia(), new Volumen(100));

        // Act & Assert
        assertThrows(ReglaDominioException.class, () ->
                Decant.crearDesdeBotellaMadre(UUID.randomUUID(), botella, new Volumen(100)));
        assertEquals(100, botella.getVolumenDisponible().mililitros()); // el estado no cambió tras el rechazo
    }
}
