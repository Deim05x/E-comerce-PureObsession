package com.uniquindio.Ecomerce_PureObsession.domain.entity;

import com.uniquindio.Ecomerce_PureObsession.domain.valueObject.Volumen;
import com.uniquindio.Ecomerce_PureObsession.domain.exception.ReglaDominioException;

import java.util.Objects;
import java.util.UUID;

public class BotellaMadre {
    private final UUID id; // Identificador final
    private final Fragancia fragancia;
    private Volumen volumenDisponible;

    public BotellaMadre(UUID id, Fragancia fragancia, Volumen volumenInicial) {
        this.id = id;
        this.fragancia = fragancia;
        this.volumenDisponible = volumenInicial;
    }

    // Comportamiento del dominio que aplica la regla de negocio para los Decants
    public void extraerParaDecant(Volumen volumenSolicitado) {
        if (volumenSolicitado.mililitros() > this.volumenDisponible.mililitros()) {
            throw new ReglaDominioException("Un Decant no puede crearse si el volumen solicitado (" + volumenSolicitado.mililitros() + "ml) es mayor al volumen disponible en la Botella Madre (" + this.volumenDisponible.mililitros() + "ml).");
        }
        // Se crea un nuevo Value Object de volumen ya que son inmutables
        this.volumenDisponible = new Volumen(this.volumenDisponible.mililitros() - volumenSolicitado.mililitros());
    }

    public UUID getId() { return id; }
    public Fragancia getFragancia() { return fragancia; }
    public Volumen getVolumenDisponible() { return volumenDisponible; }



    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BotellaMadre that = (BotellaMadre) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}