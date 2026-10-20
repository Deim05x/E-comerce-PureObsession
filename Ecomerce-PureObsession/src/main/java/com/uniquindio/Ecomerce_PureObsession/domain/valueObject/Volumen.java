package com.uniquindio.Ecomerce_PureObsession.domain.valueObject;
import com.uniquindio.Ecomerce_PureObsession.domain.exception.ReglaDominioException;
import java.util.Objects;

/** Value object inmutable; se construye únicamente a través de la fábrica validada. */
public final class Volumen {
    private final int mililitros;
    private Volumen(int mililitros) {
        this.mililitros = mililitros;
    }
    public static Volumen crear(int mililitros) {
        if (mililitros <= 0) throw new ReglaDominioException("El volumen debe ser mayor a 0 ml.");
        return new Volumen(mililitros);
    }
    public int mililitros() { return mililitros; }
    @Override public boolean equals(Object other) {
        return this == other || other instanceof Volumen value && Objects.equals(mililitros, value.mililitros);
    }
    @Override public int hashCode() { return Objects.hash(mililitros); }
}
