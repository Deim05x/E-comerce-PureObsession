package com.uniquindio.Ecomerce_PureObsession.application.usecase;

import com.uniquindio.Ecomerce_PureObsession.application.dto.request.ConsultarCatalogoRequest;
import com.uniquindio.Ecomerce_PureObsession.application.dto.response.FraganciaCatalogoResponse;
import com.uniquindio.Ecomerce_PureObsession.domain.entity.BotellaMadre;
import com.uniquindio.Ecomerce_PureObsession.domain.entity.Fragancia;
import com.uniquindio.Ecomerce_PureObsession.domain.repository.BotellaMadreRepository;
import com.uniquindio.Ecomerce_PureObsession.domain.valueObject.Concentracion;
import com.uniquindio.Ecomerce_PureObsession.domain.valueObject.FamiliaOlfativa;
import com.uniquindio.Ecomerce_PureObsession.domain.valueObject.PiramideOlfativa;
import com.uniquindio.Ecomerce_PureObsession.domain.valueObject.Volumen;
import com.uniquindio.Ecomerce_PureObsession.infrastructure.persistence.BotellaMadreRepositoryEnMemoria;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ConsultarCatalogoUseCaseTest {

    @Test
    void debeListarSoloFraganciasPublicadasActivasYDeLaFamiliaSolicitada() {
        BotellaMadreRepository repositorio = new BotellaMadreRepositoryEnMemoria();
        Fragancia publicada = fragancia("Sauvage", FamiliaOlfativa.AMADERADA);
        publicada.publicar();
        Fragancia sinPublicar = fragancia("Light Blue", FamiliaOlfativa.CITRICA);
        Fragancia inactiva = fragancia("Bleu", FamiliaOlfativa.AMADERADA);
        inactiva.publicar();
        inactiva.desactivar();
        repositorio.guardar(BotellaMadre.crear(UUID.randomUUID(), publicada, new Volumen(100)));
        repositorio.guardar(BotellaMadre.crear(UUID.randomUUID(), sinPublicar, new Volumen(100)));
        repositorio.guardar(BotellaMadre.crear(UUID.randomUUID(), inactiva, new Volumen(100)));
        ConsultarCatalogoUseCase useCase = new ConsultarCatalogoUseCase(repositorio);

        List<FraganciaCatalogoResponse> resultado = useCase.ejecutar(
                new ConsultarCatalogoRequest(FamiliaOlfativa.AMADERADA));

        assertEquals(1, resultado.size());
        assertEquals("Sauvage", resultado.get(0).nombre());
    }

    private Fragancia fragancia(String nombre, FamiliaOlfativa familiaOlfativa) {
        return Fragancia.crear(UUID.randomUUID(), nombre, familiaOlfativa,
                Concentracion.EAU_DE_PARFUM, new PiramideOlfativa("Bergamota", "Jazmín", "Almizcle"));
    }
    @Test
    void sinFiltroDebeListarSinDuplicarLaMismaFragancia() {
        // Arrange
        BotellaMadreRepository repo = new BotellaMadreRepositoryEnMemoria();
        Fragancia fragancia = fragancia("Sauvage", FamiliaOlfativa.AMADERADA); fragancia.publicar();
        repo.guardar(BotellaMadre.crear(UUID.randomUUID(), fragancia, new Volumen(100)));
        repo.guardar(BotellaMadre.crear(UUID.randomUUID(), fragancia, new Volumen(100)));
        // Act
        var resultado = new ConsultarCatalogoUseCase(repo).ejecutar(new ConsultarCatalogoRequest(null));
        // Assert
        assertEquals(1, resultado.size());
    }
}
