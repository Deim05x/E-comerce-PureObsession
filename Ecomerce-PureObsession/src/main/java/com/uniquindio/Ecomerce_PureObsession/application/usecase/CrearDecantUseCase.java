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

    /**
     * Confirma la creación una vez que el inventario haya reservado el volumen solicitado.
     */
    public DecantCreado confirmar(SolicitudDecant solicitud) {
        if (solicitud == null) {
            throw new IllegalArgumentException("La solicitud de decant es obligatoria.");
        }
        return new DecantCreado(UUID.randomUUID(), solicitud.botellaMadreId(), solicitud.mililitros());
    }

    public record SolicitudDecant(UUID botellaMadreId, int mililitros) {
    }

    public record DecantCreado(UUID id, UUID botellaMadreId, int mililitros) {
    }
}
