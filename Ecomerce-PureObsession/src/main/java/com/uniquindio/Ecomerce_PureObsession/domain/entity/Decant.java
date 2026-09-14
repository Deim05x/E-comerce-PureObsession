package com.uniquindio.Ecomerce_PureObsession.domain.entity;

import com.uniquindio.Ecomerce_PureObsession.domain.valueObject.Volumen;
import com.uniquindio.Ecomerce_PureObsession.domain.exception.ReglaDominioException;

import java.util.Objects;
import java.util.UUID;

public class Decant {
    private final UUID id;
    private final Fragancia fragancia;
    private final Volumen volumen;

    private Decant(UUID id, Fragancia fragancia, Volumen volumen) {
        this.id = id;
        this.fragancia = fragancia;
        this.volumen = volumen;
    }

    // Factory Method que garantiza las reglas al momento de crear
    public static Decant crearDesdeBotellaMadre(UUID idDecant, BotellaMadre botella, Volumen volumenSolicitado) {
        // Regla 2: Un Decant debe tener un volumen menor al de la presentación original
        if (volumenSolicitado.mililitros() >= botella.getVolumenOriginal().mililitros()) {
            throw new ReglaDominioException("Un Decant debe tener un volumen menor al de la presentación original de la fragancia.");
        }

        // Regla 1: Validamos stock y restamos de la botella madre
        botella.extraerParaDecant(volumenSolicitado);

        // Regla 6: El decant conserva la misma fragancia (y por ende, la misma concentración) de la botella madre
        return new Decant(idDecant, botella.getFragancia(), volumenSolicitado);
    }

    public UUID getId() { return id; }
    public Fragancia getFragancia() { return fragancia; }
    public Volumen getVolumen() { return volumen; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Decant decant = (Decant) o;
        return Objects.equals(id, decant.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}