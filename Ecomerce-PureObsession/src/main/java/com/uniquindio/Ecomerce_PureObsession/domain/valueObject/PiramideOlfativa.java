package com.uniquindio.Ecomerce_PureObsession.domain.valueObject;
import com.uniquindio.Ecomerce_PureObsession.domain.exception.ReglaDominioException;
import java.util.Objects;

/** Value object inmutable; se construye únicamente a través de la fábrica validada. */
public final class PiramideOlfativa {
    private final String notasSalida;
    private final String notasCorazon;
    private final String notasFondo;
    private PiramideOlfativa(String notasSalida, String notasCorazon, String notasFondo) {
        this.notasSalida = notasSalida;
        this.notasCorazon = notasCorazon;
        this.notasFondo = notasFondo;
    }
    public static PiramideOlfativa crear(String notasSalida, String notasCorazon, String notasFondo) {
        if (notasSalida == null || notasSalida.isBlank() || notasCorazon == null || notasCorazon.isBlank() || notasFondo == null || notasFondo.isBlank()) throw new ReglaDominioException("La pirámide requiere notas de salida, corazón y fondo.");
        return new PiramideOlfativa(notasSalida, notasCorazon, notasFondo);
    }
    public String notasSalida() { return notasSalida; }
    public String notasCorazon() { return notasCorazon; }
    public String notasFondo() { return notasFondo; }
    @Override public boolean equals(Object other) {
        return this == other || other instanceof PiramideOlfativa value && Objects.equals(notasSalida, value.notasSalida) && Objects.equals(notasCorazon, value.notasCorazon) && Objects.equals(notasFondo, value.notasFondo);
    }
    @Override public int hashCode() { return Objects.hash(notasSalida, notasCorazon, notasFondo); }
}
