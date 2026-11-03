package com.uniquindio.Ecomerce_PureObsession.infrastructure.persistence.jpa;
import jakarta.persistence.*;
import java.util.UUID;
@Entity @Table(name="direccion")
public class DireccionJpa {
    @Id UUID id;
    UUID cuentaId;
    @Column(length=60) String etiqueta;
    @Column(length=120) String receptor;
    @Column(length=22) String telefono;
    @Column(length=100) String departamento,ciudad;
    @Column(length=350) String direccion;
    @Column(length=500) String indicaciones;
    boolean predeterminada,eliminada;
    public DireccionJpa() {}
}
