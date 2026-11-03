package com.uniquindio.Ecomerce_PureObsession.infrastructure.persistence.jpa;

import jakarta.persistence.*;
import java.util.UUID;

@Entity @Table(name="ficha_producto")
public class FichaProductoJpa {
    @Id UUID id;
    @Column(length=120) String marca;
    @Column(length=4000) String descripcion;
    @Column(length=30) String categoria;
    @Column(nullable=false) int disponibles;
    @Column(nullable=false) int reservadas;
    @Column(length=2000) String imagenUrl;
    public FichaProductoJpa() {}
}
