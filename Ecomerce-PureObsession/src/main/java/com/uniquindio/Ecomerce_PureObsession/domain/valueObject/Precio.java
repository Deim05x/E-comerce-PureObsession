package com.uniquindio.Ecomerce_PureObsession.domain.valueObject;
import com.uniquindio.Ecomerce_PureObsession.domain.exception.ReglaDominioException;
import java.util.Objects;

/** Value object inmutable; se construye únicamente a través de la fábrica validada. */
public final class Precio {
    private final Double precio;
    private final String moneda;
    private Precio(Double precio, String moneda) {
        this.precio = precio;
        this.moneda = moneda;
    }
    public static Precio crear(Double precio, String moneda) {
        if (precio == null || !Double.isFinite(precio) || precio < 0) throw new ReglaDominioException("El precio debe ser finito y no negativo.");
        if (moneda == null || moneda.isBlank()) throw new ReglaDominioException("La moneda es obligatoria.");
        moneda = moneda.trim().toUpperCase(java.util.Locale.ROOT);
        try { java.util.Currency.getInstance(moneda); }
        catch (IllegalArgumentException e) { throw new ReglaDominioException("La moneda debe ser un código ISO 4217 válido."); }
        if (precio == 0.0) precio = 0.0;
        return new Precio(precio, moneda);
    }
    public Double precio() { return precio; }
    public String moneda() { return moneda; }
    @Override public boolean equals(Object other) {
        return this == other || other instanceof Precio value && Objects.equals(precio, value.precio) && Objects.equals(moneda, value.moneda);
    }
    @Override public int hashCode() { return Objects.hash(precio, moneda); }
}
