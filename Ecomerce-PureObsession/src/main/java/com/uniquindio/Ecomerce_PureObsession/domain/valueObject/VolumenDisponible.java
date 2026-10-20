package com.uniquindio.Ecomerce_PureObsession.domain.valueObject;
import com.uniquindio.Ecomerce_PureObsession.domain.exception.ReglaDominioException;
import java.util.Objects;

/** Value object inmutable; se construye únicamente a través de la fábrica validada. */
public final class VolumenDisponible {
    private final int mililitros;
    private VolumenDisponible(int mililitros) {
        this.mililitros = mililitros;
    }
    public static VolumenDisponible crear(int mililitros) {
        if (mililitros < 0) throw new ReglaDominioException("El volumen disponible no puede ser negativo.");
        return new VolumenDisponible(mililitros);
    }
    public int mililitros() { return mililitros; }
    @Override public boolean equals(Object other) {
        return this == other || other instanceof VolumenDisponible value && Objects.equals(mililitros, value.mililitros);
    }
    @Override public int hashCode() { return Objects.hash(mililitros); }
}
