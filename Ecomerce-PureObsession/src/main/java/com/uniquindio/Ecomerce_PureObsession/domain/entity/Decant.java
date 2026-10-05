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

    // Compatibilidad con la API existente: la raíz administra inventario y colección.
    public static Decant crearDesdeBotellaMadre(UUID idDecant, BotellaMadre botella, Volumen solicitado) {
        if (botella == null) {
            throw new ReglaDominioException("La botella madre es obligatoria.");
        }
        return botella.crearDecant(idDecant, solicitado);
    }

    static Decant crearInterno(UUID id, Fragancia fragancia, Volumen volumen) {
        if (id == null || fragancia == null || volumen == null) {
            throw new ReglaDominioException("El decant requiere identidad, fragancia y volumen.");
        }
        return new Decant(id, fragancia, volumen);
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