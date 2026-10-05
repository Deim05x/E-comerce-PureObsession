package com.uniquindio.Ecomerce_PureObsession.domain.entity;

import com.uniquindio.Ecomerce_PureObsession.domain.valueObject.Volumen;
import com.uniquindio.Ecomerce_PureObsession.domain.valueObject.VolumenDisponible;
import com.uniquindio.Ecomerce_PureObsession.domain.exception.ReglaDominioException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/** Raíz del agregado de inventario: conserva también los decants extraídos. */
public class BotellaMadre {
    private final UUID id;
    private final Fragancia fragancia;
    private final Volumen volumenOriginal;
    private VolumenDisponible volumenDisponible;
    private final List<Decant> decants = new ArrayList<>();

    private BotellaMadre(UUID id, Fragancia fragancia, Volumen volumenOriginal) {
        if (id == null || fragancia == null || volumenOriginal == null) {
            throw new ReglaDominioException("La botella requiere identidad, fragancia y volumen original.");
        }
        this.id = id;
        this.fragancia = fragancia;
        this.volumenOriginal = volumenOriginal;
        this.volumenDisponible = new VolumenDisponible(volumenOriginal.mililitros());
    }

    public static BotellaMadre crear(UUID id, Fragancia fragancia, Volumen volumenOriginal) {
        return new BotellaMadre(id, fragancia, volumenOriginal);
    }

    public Decant crearDecant(UUID decantId, Volumen solicitado) {
        if (decantId == null || decants.stream().anyMatch(d -> d.getId().equals(decantId))) {
            throw new ReglaDominioException("El decant necesita una identidad nueva y no nula.");
        }
        validarExtraccion(solicitado);
        Decant decant = Decant.crearInterno(decantId, fragancia, solicitado);
        extraerParaDecant(solicitado);
        decants.add(decant);
        return decant;
    }

    private void validarExtraccion(Volumen solicitado) {
        if (solicitado == null) {
            throw new ReglaDominioException("El volumen solicitado es obligatorio.");
        }
        if (!fragancia.isActiva()) {
            throw new ReglaDominioException("No se pueden extraer decants de una fragancia inactiva.");
        }
        if (solicitado.mililitros() >= volumenOriginal.mililitros()) {
            throw new ReglaDominioException("Un decant debe ser menor que la presentación original.");
        }
        if (solicitado.mililitros() > volumenDisponible.mililitros()) {
            throw new ReglaDominioException("El volumen solicitado supera el disponible.");
        }
    }

    /** Operación de inventario; para crear y conservar un decant usar crearDecant. */
    public void extraerParaDecant(Volumen solicitado) {
        validarExtraccion(solicitado);
        volumenDisponible = new VolumenDisponible(volumenDisponible.mililitros() - solicitado.mililitros());
    }

    public UUID getId() { return id; }
    public Fragancia getFragancia() { return fragancia; }
    public Volumen getVolumenOriginal() { return volumenOriginal; }
    public VolumenDisponible getVolumenDisponible() { return volumenDisponible; }
    public List<Decant> getDecants() { return List.copyOf(decants); }

    @Override public boolean equals(Object o) {
        return this == o || o != null && getClass() == o.getClass() && id.equals(((BotellaMadre) o).id);
    }
    @Override public int hashCode() { return Objects.hash(id); }
}
