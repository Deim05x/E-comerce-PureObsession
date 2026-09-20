package com.uniquindio.Ecomerce_PureObsession.domain.entity;

import com.uniquindio.Ecomerce_PureObsession.domain.exception.ReglaDominioException;
import com.uniquindio.Ecomerce_PureObsession.domain.valueObject.Concentracion;
import com.uniquindio.Ecomerce_PureObsession.domain.valueObject.Volumen;

public class Perfume {
    private final String fragancia;
    private final Concentracion concentracion;
    private final Volumen volumenOriginal;
    private int volumenDisponibleEnMl;

    public Perfume(String fragancia, Concentracion concentracion, Volumen volumenOriginal) {
        if (fragancia == null || fragancia.isBlank()) {
            throw new ReglaDominioException("La fragancia es obligatoria");
        }
        if (concentracion == null) {
            throw new ReglaDominioException("La concentración es obligatoria");
        }

        this.fragancia = fragancia;
        this.concentracion = concentracion;
        this.volumenOriginal = volumenOriginal;
        this.volumenDisponibleEnMl = volumenOriginal.mililitros();
    }

    public Decant crearDecant(Volumen volumenSolicitado) {
        if (volumenSolicitado.mililitros() >= volumenOriginal.mililitros()) {
            throw new ReglaDominioException("El Decant debe tener un volumen menor al de la botella original");
        }
        if (volumenSolicitado.mililitros() > volumenDisponibleEnMl) {
            throw new ReglaDominioException("No hay volumen disponible suficiente en la Botella Madre");
        }

        volumenDisponibleEnMl -= volumenSolicitado.mililitros();
        return new Decant(fragancia, concentracion, volumenSolicitado);
    }

    public int getVolumenDisponibleEnMl() {
        return volumenDisponibleEnMl;
    }
}
