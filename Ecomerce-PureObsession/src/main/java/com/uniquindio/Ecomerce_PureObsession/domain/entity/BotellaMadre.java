package com.uniquindio.Ecomerce_PureObsession.domain.entity;

import com.uniquindio.Ecomerce_PureObsession.domain.valueObject.Volumen;
import com.uniquindio.Ecomerce_PureObsession.domain.exception.ReglaDominioException;

import java.util.Objects;
import java.util.UUID;

public class BotellaMadre {
    private final UUID id;
    private final Fragancia fragancia;
    private final Volumen volumenOriginal;
    private Volumen volumenDisponible;

    public BotellaMadre(UUID id, Fragancia fragancia, Volumen volumenOriginal) {
        this.id = id;
        this.fragancia = fragancia;
        this.volumenOriginal = volumenOriginal;
        this.volumenDisponible = volumenOriginal;
    }

    public void extraerParaDecant(Volumen volumenSolicitado) {
        if (volumenSolicitado.mililitros() > this.volumenDisponible.mililitros()) {
            throw new ReglaDominioException("Un Decant no puede crearse si el volumen solicitado es mayor al volumen disponible en la Botella Madre.");
        }
        this.volumenDisponible = new Volumen(this.volumenDisponible.mililitros() - volumenSolicitado.mililitros());
    }

    public UUID getId() { return id; }
    public Fragancia getFragancia() { return fragancia; }
    public Volumen getVolumenOriginal() { return volumenOriginal; }
    public Volumen getVolumenDisponible() { return volumenDisponible; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BotellaMadre that = (BotellaMadre) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}