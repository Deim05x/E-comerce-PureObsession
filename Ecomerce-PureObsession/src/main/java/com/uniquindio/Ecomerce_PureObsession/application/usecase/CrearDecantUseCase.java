package com.uniquindio.Ecomerce_PureObsession.application.usecase;

import java.util.UUID;

/**
 * Caso de uso para registrar la solicitud de un decant de una botella madre.
 */
public class CrearDecantUseCase {

    public SolicitudDecant solicitar(UUID botellaMadreId, int mililitros) {
        if (botellaMadreId == null) {
            throw new IllegalArgumentException("La botella madre es obligatoria.");
        }
        if (mililitros <= 0) {
            throw new IllegalArgumentException("El volumen solicitado debe ser mayor que cero.");
        }
        return new SolicitudDecant(botellaMadreId, mililitros);
    }

    public record SolicitudDecant(UUID botellaMadreId, int mililitros) {
    }
}
