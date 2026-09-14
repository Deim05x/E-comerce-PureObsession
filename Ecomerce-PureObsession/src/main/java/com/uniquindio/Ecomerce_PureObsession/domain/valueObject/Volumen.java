package com.uniquindio.Ecomerce_PureObsession.domain.valueObject;

import com.uniquindio.Ecomerce_PureObsession.domain.exception.ReglaDominioException;

public record Volumen(int mililitros) {
    public Volumen {
        if (mililitros <= 0) {
            throw new ReglaDominioException("El volumen debe ser mayor a 0 ml.");

        }
    }
}
