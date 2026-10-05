package com.uniquindio.Ecomerce_PureObsession.application.usecase;
import com.uniquindio.Ecomerce_PureObsession.application.dto.request.CrearDecantRequest;
import com.uniquindio.Ecomerce_PureObsession.domain.entity.*;
import com.uniquindio.Ecomerce_PureObsession.domain.exception.ReglaDominioException;
import com.uniquindio.Ecomerce_PureObsession.infrastructure.persistence.BotellaMadreRepositoryEnMemoria;
import org.junit.jupiter.api.Test;
import java.util.UUID;
import static com.uniquindio.Ecomerce_PureObsession.DatosPrueba.*;
import static org.junit.jupiter.api.Assertions.*;

class CrearDecantUseCaseTest {
    @Test void debeCrearPersistirYHeredarLaFragancia() {
        // Arrange
        var repo = new BotellaMadreRepositoryEnMemoria(); var botella = botella(); repo.guardar(botella);
        var caso = new CrearDecantUseCase(repo);
        // Act
        var respuesta = caso.ejecutar(new CrearDecantRequest(botella.getId(), 10));
        // Assert
        var guardada = repo.buscarPorId(botella.getId()).orElseThrow();
        assertEquals(90, guardada.getVolumenDisponible().mililitros());
        assertEquals(1, guardada.getDecants().size());
        assertEquals(respuesta.decantId(), guardada.getDecants().get(0).getId());
        assertEquals(botella.getFragancia().getId(), respuesta.fraganciaId());
        assertEquals(botella.getFragancia().getConcentracion(), respuesta.concentracion());
    }
    @Test void noDebeConfirmarUnaBotellaInexistente() {
        // Arrange
        var caso = new CrearDecantUseCase(new BotellaMadreRepositoryEnMemoria());
        // Act & Assert
        assertThrows(ReglaDominioException.class, () -> caso.ejecutar(new CrearDecantRequest(UUID.randomUUID(), 10)));
    }
    @Test void unRechazoNoDebeModificarInventarioNiDecants() {
        // Arrange
        var repo = new BotellaMadreRepositoryEnMemoria(); var botella = botella(); repo.guardar(botella);
        var caso = new CrearDecantUseCase(repo);
        caso.ejecutar(new CrearDecantRequest(botella.getId(), 70));
        // Act & Assert
        assertThrows(ReglaDominioException.class, () -> caso.ejecutar(new CrearDecantRequest(botella.getId(), 40)));
        assertEquals(30, botella.getVolumenDisponible().mililitros());
        assertEquals(1, botella.getDecants().size());
    }
    @Test void debePoderAgotarExactamenteElInventario() {
        // Arrange
        var repo = new BotellaMadreRepositoryEnMemoria(); var botella = botella(); repo.guardar(botella);
        var caso = new CrearDecantUseCase(repo);
        caso.ejecutar(new CrearDecantRequest(botella.getId(), 70));
        // Act
        var respuesta = caso.ejecutar(new CrearDecantRequest(botella.getId(), 30));
        // Assert
        assertEquals(0, respuesta.volumenDisponibleMl());
        assertEquals(2, botella.getDecants().size());
        assertThrows(ReglaDominioException.class, () -> caso.ejecutar(new CrearDecantRequest(botella.getId(), 1)));
    }
}
