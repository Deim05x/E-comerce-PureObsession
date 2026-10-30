package com.uniquindio.Ecomerce_PureObsession.application.usecase;

import com.uniquindio.Ecomerce_PureObsession.domain.exception.ReglaDominioException;
import com.uniquindio.Ecomerce_PureObsession.application.dto.request.ConsultarCatalogoRequest;
import com.uniquindio.Ecomerce_PureObsession.application.dto.response.FraganciaCatalogoResponse;
import com.uniquindio.Ecomerce_PureObsession.domain.entity.BotellaMadre;
import com.uniquindio.Ecomerce_PureObsession.domain.entity.Fragancia;
import com.uniquindio.Ecomerce_PureObsession.domain.repository.BotellaMadreRepository;

import java.util.List;

/**
 * Caso de uso para consultar las fragancias publicadas por familia olfativa.
 */
public class ConsultarCatalogoUseCase implements com.uniquindio.Ecomerce_PureObsession.application.port.in.ConsultarCatalogoPort {

    private final BotellaMadreRepository botellaMadreRepository;

    public ConsultarCatalogoUseCase(BotellaMadreRepository botellaMadreRepository) {
        this.botellaMadreRepository = botellaMadreRepository;
    }

    public List<FraganciaCatalogoResponse> ejecutar(ConsultarCatalogoRequest request) {
        if (request == null) {
            throw new ReglaDominioException("La solicitud es obligatoria.");
        }
        return botellaMadreRepository.listarFraganciasPublicadas(request.familiaOlfativa()).stream()
                .map(fragancia -> new FraganciaCatalogoResponse(
                        fragancia.getId(),
                        fragancia.getNombre(),
                        fragancia.getFamiliaOlfativa(),
                        fragancia.getConcentracion()))
                .toList();
    }
}
