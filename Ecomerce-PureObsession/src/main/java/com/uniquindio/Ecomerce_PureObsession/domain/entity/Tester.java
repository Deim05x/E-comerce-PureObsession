package com.uniquindio.Ecomerce_PureObsession.domain.entity;

import com.uniquindio.Ecomerce_PureObsession.domain.exception.ReglaDominioException;
import java.util.Objects;
import java.util.UUID;

public class Tester {
    private final UUID id;
    private final Fragancia fragancia;
    private boolean envolturaRegalo;

    private Tester(UUID id, Fragancia fragancia) {
        this.id = id;
        this.fragancia = fragancia;
        this.envolturaRegalo = false;
    }

    public static Tester crear(UUID id, Fragancia fragancia) {
        if (id == null) throw new ReglaDominioException("La identidad es obligatoria.");
        if (fragancia == null) throw new ReglaDominioException("La fragancia es obligatoria.");
        return new Tester(id, fragancia);
    }

    public void aplicarEnvolturaRegalo() {
        com.uniquindio.Ecomerce_PureObsession.domain.valueObject.TipoPresentacion.TESTER.validarEmpaque(
                com.uniquindio.Ecomerce_PureObsession.domain.valueObject.TipoEmpaque.REGALO);
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
