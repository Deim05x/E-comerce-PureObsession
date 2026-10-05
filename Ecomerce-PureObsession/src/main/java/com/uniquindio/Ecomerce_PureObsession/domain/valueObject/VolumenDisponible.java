package com.uniquindio.Ecomerce_PureObsession.domain.valueObject;

import com.uniquindio.Ecomerce_PureObsession.domain.exception.ReglaDominioException;

/** El inventario puede agotarse; el volumen original y el solicitado usan Volumen (> 0). */
public record VolumenDisponible(int mililitros) {
    public VolumenDisponible {
        if (mililitros < 0) {
            throw new ReglaDominioException("El volumen disponible no puede ser negativo.");
        }
    }
}
