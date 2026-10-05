package com.uniquindio.Ecomerce_PureObsession.application.usecase;

import com.uniquindio.Ecomerce_PureObsession.application.dto.request.ConsultarCatalogoRequest;
import com.uniquindio.Ecomerce_PureObsession.application.dto.response.FraganciaCatalogoResponse;
import com.uniquindio.Ecomerce_PureObsession.domain.entity.BotellaMadre;
import com.uniquindio.Ecomerce_PureObsession.domain.entity.Fragancia;
import com.uniquindio.Ecomerce_PureObsession.domain.repository.BotellaMadreRepository;

import java.util.List;

/**
 * Caso de uso para consultar las fragancias publicadas por familia olfativa.
 */
public class ConsultarCatalogoUseCase {

    private final BotellaMadreRepository botellaMadreRepository;

    public ConsultarCatalogoUseCase(BotellaMadreRepository botellaMadreRepository) {
        this.botellaMadreRepository = botellaMadreRepository;
    }

    public List<FraganciaCatalogoResponse> ejecutar(ConsultarCatalogoRequest request) {
        return botellaMadreRepository.listarTodas().stream()
                .map(BotellaMadre::getFragancia)
                .filter(Fragancia::isActiva)
                .filter(Fragancia::isPublicada)
                .filter(fragancia -> request.familiaOlfativa() == null
                        || fragancia.getFamiliaOlfativa() == request.familiaOlfativa())
                .map(fragancia -> new FraganciaCatalogoResponse(
                        fragancia.getId(),
                        fragancia.getNombre(),
                        fragancia.getFamiliaOlfativa(),
                        fragancia.getConcentracion()))
                .toList();
    }
}
