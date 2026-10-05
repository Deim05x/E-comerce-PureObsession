package com.uniquindio.Ecomerce_PureObsession.application.usecase;

import com.uniquindio.Ecomerce_PureObsession.application.dto.request.RegistrarBotellaMadreRequest;
import com.uniquindio.Ecomerce_PureObsession.application.dto.response.BotellaMadreResponse;
import com.uniquindio.Ecomerce_PureObsession.domain.exception.ReglaDominioException;
import com.uniquindio.Ecomerce_PureObsession.domain.valueObject.Concentracion;
import com.uniquindio.Ecomerce_PureObsession.domain.valueObject.FamiliaOlfativa;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RegistrarBotellaMadreUseCaseTest {

    private RegistrarBotellaMadreRequest requestValido(String nombre) {
        return new RegistrarBotellaMadreRequest(nombre, FamiliaOlfativa.AMADERADA,
                Concentracion.EAU_DE_PARFUM, "Bergamota", "Lavanda", "Ámbar", 100);
    }

    @Test
    void debeGuardarLaBotellaMadreEnElRepositorio() {
        // Arrange
        BotellaMadreRepository repositorio = new BotellaMadreRepositoryEnMemoria();
        RegistrarBotellaMadreUseCase useCase = new RegistrarBotellaMadreUseCase(repositorio);

        // Act
        BotellaMadreResponse respuesta = useCase.ejecutar(requestValido("Dior Sauvage"));

        // Assert
        assertTrue(repositorio.buscarPorId(respuesta.botellaMadreId()).isPresent());
        assertEquals(100, respuesta.volumenDisponibleMl());
    }

    @Test
    void debeRechazarUnaFraganciaSinNombreConReglaDeDominio() {
        // Arrange
        RegistrarBotellaMadreUseCase useCase =
                new RegistrarBotellaMadreUseCase(new BotellaMadreRepositoryEnMemoria());

        // Act & Assert
        assertThrows(ReglaDominioException.class, () -> useCase.ejecutar(requestValido("  ")));
    }
}