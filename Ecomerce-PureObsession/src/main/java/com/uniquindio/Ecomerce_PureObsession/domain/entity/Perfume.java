package com.uniquindio.Ecomerce_PureObsession.domain.entity;

import com.uniquindio.Ecomerce_PureObsession.domain.valueObject.Volumen;

import java.util.Objects;
import java.util.UUID;

public class Perfume {
    private final UUID id;
    private final Fragancia fragancia;
    private final Volumen volumen;
    private boolean envolturaRegalo;

    public Perfume(UUID id, Fragancia fragancia, Volumen volumen) {
        this.id = id;
        this.fragancia = fragancia;
        this.volumen = volumen;
        this.envolturaRegalo = false;
    }

    public void aplicarEnvolturaRegalo() {
        this.envolturaRegalo = true;
    }

    public UUID getId() { return id; }
    public Fragancia getFragancia() { return fragancia; }
    public Volumen getVolumen() { return volumen; }
    public boolean isEnvolturaRegalo() { return envolturaRegalo; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Perfume perfume = (Perfume) o;
        return Objects.equals(id, perfume.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}