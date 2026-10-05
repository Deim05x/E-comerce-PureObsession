package com.uniquindio.Ecomerce_PureObsession.application.usecase;

import com.uniquindio.Ecomerce_PureObsession.domain.exception.ReglaDominioException;
import com.uniquindio.Ecomerce_PureObsession.application.dto.request.RegistrarBotellaMadreRequest;
import com.uniquindio.Ecomerce_PureObsession.application.dto.response.BotellaMadreResponse;
import com.uniquindio.Ecomerce_PureObsession.domain.entity.BotellaMadre;
import com.uniquindio.Ecomerce_PureObsession.domain.entity.Fragancia;
import com.uniquindio.Ecomerce_PureObsession.domain.repository.BotellaMadreRepository;
import com.uniquindio.Ecomerce_PureObsession.domain.valueObject.PiramideOlfativa;
import com.uniquindio.Ecomerce_PureObsession.domain.valueObject.Volumen;

import java.util.UUID;

public class RegistrarBotellaMadreUseCase {

    private final BotellaMadreRepository botellaMadreRepository;

    public RegistrarBotellaMadreUseCase(BotellaMadreRepository botellaMadreRepository) {
        this.botellaMadreRepository = botellaMadreRepository;
    }

    public BotellaMadreResponse ejecutar(RegistrarBotellaMadreRequest request) {
        if (request == null) {
            throw new ReglaDominioException("La solicitud es obligatoria.");
        }
        PiramideOlfativa piramide = new PiramideOlfativa(
                request.notasSalida(), request.notasCorazon(), request.notasFondo());

        Fragancia fragancia = Fragancia.crear(UUID.randomUUID(), request.nombreFragancia(),
                request.familiaOlfativa(), request.concentracion(), piramide);

        BotellaMadre botella = BotellaMadre.crear(UUID.randomUUID(), fragancia, new Volumen(request.volumenMl()));

        botellaMadreRepository.guardar(botella);

        return new BotellaMadreResponse(botella.getId(), fragancia.getNombre(),
                fragancia.getFamiliaOlfativa(), fragancia.getConcentracion(),
                botella.getVolumenDisponible().mililitros());
    }
}
