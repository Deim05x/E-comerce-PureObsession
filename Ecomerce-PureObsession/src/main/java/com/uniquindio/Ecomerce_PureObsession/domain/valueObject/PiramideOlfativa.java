package com.uniquindio.Ecomerce_PureObsession.domain.valueObject;

import com.uniquindio.Ecomerce_PureObsession.domain.exception.ReglaDominioException;

public record PiramideOlfativa(String notasSalida, String notasCorazon, String notasFondo) {
    public PiramideOlfativa {
        if (notasSalida == null || notasSalida.isBlank() ||
                notasCorazon == null || notasCorazon.isBlank() ||
                notasFondo == null || notasFondo.isBlank()) {
            throw new ReglaDominioException("La pirámide olfativa debe identificar sus fases de salida, corazón y fondo.");
        }
    }
}
