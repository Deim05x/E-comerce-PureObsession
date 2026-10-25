package com.uniquindio.Ecomerce_PureObsession.infrastructure.persistence.jpa;

import jakarta.persistence.Embeddable;
import java.util.UUID;

@Embeddable
public class LineaJpa {
    UUID id, itemId, fraganciaId, vendedorId;
    String descripcion, moneda;
    double precio;
    int cantidad;
    public LineaJpa() {}
}
