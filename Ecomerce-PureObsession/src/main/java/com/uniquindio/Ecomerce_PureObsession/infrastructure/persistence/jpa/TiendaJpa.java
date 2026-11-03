package com.uniquindio.Ecomerce_PureObsession.infrastructure.persistence.jpa;

import jakarta.persistence.*;
import java.util.UUID;

@Entity @Table(name="tienda")
public class TiendaJpa {
    @Id UUID id;
    @Column(length=120) String nombre;
    @Column(length=2000) String descripcion;
    @Column(length=100) String ciudad;
    @Column(length=22) String telefono;
    @Column(length=2000) String logoUrl;
    public TiendaJpa() {}
}
