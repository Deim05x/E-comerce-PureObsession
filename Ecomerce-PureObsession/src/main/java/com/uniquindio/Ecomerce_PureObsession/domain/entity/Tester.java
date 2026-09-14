package com.uniquindio.Ecomerce_PureObsession.domain.entity;

import com.uniquindio.Ecomerce_PureObsession.domain.exception.ReglaDominioException;
import java.util.Objects;
import java.util.UUID;

public class Tester {
    private final UUID id;
    private final Fragancia fragancia;
    private boolean envolturaRegalo;

    public Tester(UUID id, Fragancia fragancia) {
        this.id = id;
        this.fragancia = fragancia;
        this.envolturaRegalo = false;
    }

    public void aplicarEnvolturaRegalo() {
        throw new ReglaDominioException("Un Tester no puede seleccionarse para el servicio de envoltura de regalo debido a que su presentación no corresponde al empaque comercial tradicional.");
    }

    public UUID getId() { return id; }
    public Fragancia getFragancia() { return fragancia; }
    public boolean isEnvolturaRegalo() { return envolturaRegalo; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Tester tester = (Tester) o;
        return Objects.equals(id, tester.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
