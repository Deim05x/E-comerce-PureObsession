package com.uniquindio.Ecomerce_PureObsession.domain.service;

import com.uniquindio.Ecomerce_PureObsession.domain.entity.BotellaMadre;
import com.uniquindio.Ecomerce_PureObsession.domain.exception.ReglaDominioException;
import java.util.UUID;

public final class PoliticaCreacionPresentacion {
    private PoliticaCreacionPresentacion() {
    }

    public static void validarDecant(UUID fraganciaId, int volumenMl, BotellaMadre botella) {
        if (botella == null) {
            throw new ReglaDominioException("El decant requiere una botella madre existente.");
        }
        if (!botella.getFragancia().isActiva()) {
            throw new ReglaDominioException("La fragancia de la botella está inactiva.");
        }
        if (!botella.getFragancia().getId().equals(fraganciaId)) {
            throw new ReglaDominioException("El decant debe corresponder a la fragancia de su botella madre.");
        }
        if (volumenMl >= botella.getVolumenOriginal().mililitros()) {
            throw new ReglaDominioException("El decant debe ser menor que la botella madre.");
        }
    }
}
