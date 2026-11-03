package com.uniquindio.Ecomerce_PureObsession.domain.valueObject;
import com.uniquindio.Ecomerce_PureObsession.domain.exception.ReglaDominioException;
import java.util.Objects;

/** Value object inmutable; se construye únicamente a través de la fábrica validada. */
public final class Precio {
    private final java.math.BigDecimal precio;
    private final String moneda;
    private Precio(java.math.BigDecimal precio, String moneda) {
        this.precio = precio;
        this.moneda = moneda;
    }
    public static Precio crear(Double precio, String moneda) {
        if (precio == null || !Double.isFinite(precio) || precio < 0) throw new ReglaDominioException("El precio debe ser finito y no negativo.");
        return de(java.math.BigDecimal.valueOf(precio),moneda);
    }
    public static Precio de(java.math.BigDecimal precio, String moneda) {
        if(precio==null || precio.signum()<0 || precio.precision()-precio.scale()>12)
            throw new ReglaDominioException("Importe invalido.");
        try { precio=precio.setScale(2,java.math.RoundingMode.UNNECESSARY); }
        catch(ArithmeticException e) { throw new ReglaDominioException("El importe admite dos decimales."); }
        if (moneda == null || moneda.isBlank()) throw new ReglaDominioException("La moneda es obligatoria.");
        moneda = moneda.trim().toUpperCase(java.util.Locale.ROOT);
        try { java.util.Currency.getInstance(moneda); }
        catch (IllegalArgumentException e) { throw new ReglaDominioException("La moneda debe ser un código ISO 4217 válido."); }
        return new Precio(precio, moneda);
    }
    /** Compatibilidad con clientes Java anteriores; calculos y persistencia usan monto(). */
    public Double precio() { return precio.doubleValue(); }
    public java.math.BigDecimal monto() { return precio; }
    public String moneda() { return moneda; }
    @Override public boolean equals(Object other) {
        return this == other || other instanceof Precio value && Objects.equals(precio, value.precio) && Objects.equals(moneda, value.moneda);
    }
    @Override public int hashCode() { return Objects.hash(precio, moneda); }
}
