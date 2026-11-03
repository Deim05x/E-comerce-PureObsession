package com.uniquindio.Ecomerce_PureObsession.infrastructure.persistence.jpa;

import jakarta.persistence.Embeddable;
import java.util.UUID;

@Embeddable
public class LineaJpa {
    UUID id, itemId, fraganciaId, vendedorId;
    String descripcion, moneda;
    @jakarta.persistence.Column(precision=14,scale=2)
    java.math.BigDecimal precio;
    int cantidad;
    @jakarta.persistence.Enumerated(jakarta.persistence.EnumType.STRING)
    com.uniquindio.Ecomerce_PureObsession.domain.valueObject.TipoEmpaque empaque;
    public LineaJpa() {}
}
