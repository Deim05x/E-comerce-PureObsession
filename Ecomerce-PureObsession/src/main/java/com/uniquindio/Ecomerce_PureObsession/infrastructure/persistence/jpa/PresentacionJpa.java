package com.uniquindio.Ecomerce_PureObsession.infrastructure.persistence.jpa;
import jakarta.persistence.*;
import java.util.*;
import java.math.BigDecimal;
import com.uniquindio.Ecomerce_PureObsession.domain.valueObject.*;
@Entity
@Table(name="presentacion")
public class PresentacionJpa {
    @Id UUID id;
    UUID vendedorId, fraganciaId, botellaMadreId;
    @Enumerated(EnumType.STRING) TipoPresentacion tipo;
    int volumenMl;
    @Column(precision=14,scale=2,nullable=false) BigDecimal precio;
    @Enumerated(EnumType.STRING) OrigenProducto origen;
    String paisOrigen;
    boolean original, publicada;
    public PresentacionJpa() {}
}

