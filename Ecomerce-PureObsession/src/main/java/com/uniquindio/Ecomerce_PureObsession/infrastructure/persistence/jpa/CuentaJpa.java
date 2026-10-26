package com.uniquindio.Ecomerce_PureObsession.infrastructure.persistence.jpa;
import jakarta.persistence.*;
import java.util.*;
import com.uniquindio.Ecomerce_PureObsession.domain.valueObject.*;
@Entity
@Table(name="cuenta")
public class CuentaJpa {
    @Id UUID id;
    String nombre;
    @Column(nullable=false,unique=true) String cedula;
    @Column(nullable=false,unique=true) String email;
    @Column(nullable=false) String claveHash;
    @Enumerated(EnumType.STRING) Rol rol;
    public CuentaJpa() {}
}

