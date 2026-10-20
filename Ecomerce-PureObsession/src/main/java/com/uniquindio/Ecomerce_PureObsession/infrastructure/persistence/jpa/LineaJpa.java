package com.uniquindio.Ecomerce_PureObsession.infrastructure.persistence.jpa;
import jakarta.persistence.*;
import java.util.UUID;
@Embeddable
public class LineaJpa {
    UUID id, itemId, fraganciaId;
    String descripcion, moneda;
    double precio;
    int cantidad;
    public LineaJpa() {}
}

