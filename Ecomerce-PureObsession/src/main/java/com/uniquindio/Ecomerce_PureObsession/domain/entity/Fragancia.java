package com.uniquindio.Ecomerce_PureObsession.domain.entity;

import com.uniquindio.Ecomerce_PureObsession.domain.valueObject.Concentracion;
import com.uniquindio.Ecomerce_PureObsession.domain.valueObject.FamiliaOlfativa;
import com.uniquindio.Ecomerce_PureObsession.domain.exception.ReglaDominioException;

import java.util.Objects;
import java.util.UUID;

public class Fragancia {
    private final UUID id; // Identificador final
    private final String nombre;
    private final FamiliaOlfativa familiaOlfativa;
    private Concentracion concentracion;
    private boolean publicada;

    public Fragancia(UUID id, String nombre, FamiliaOlfativa familiaOlfativa, Concentracion concentracion) {
        this.id = id;
        this.nombre = nombre;
        this.familiaOlfativa = familiaOlfativa;
        this.concentracion = concentracion;
        this.publicada = false;
    }

    // Comportamiento del dominio que aplica la regla de negocio
    public void publicar() {
        if (this.concentracion == null) {
            throw new ReglaDominioException("Toda Fragancia debe tener una Concentración definida antes de ser publicada en el catálogo.");
        }
        this.publicada = true;
    }

    public UUID getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public FamiliaOlfativa getFamiliaOlfativa() {
        return familiaOlfativa;
    }

    public Concentracion getConcentracion() {
        return concentracion;
    }

    public boolean isPublicada() {
        return publicada;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Fragancia fragancia = (Fragancia) o;
        return Objects.equals(id, fragancia.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
