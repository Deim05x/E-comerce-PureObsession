package com.uniquindio.Ecomerce_PureObsession.infrastructure.persistence.jpa;
import jakarta.persistence.*;
import java.util.*;
import java.math.BigDecimal;
import com.uniquindio.Ecomerce_PureObsession.domain.valueObject.*;
@Entity
@Table(name="fragancia")
public class FraganciaJpa {
    @Id UUID id;
    String nombre;
    @Enumerated(EnumType.STRING) FamiliaOlfativa familia;
    @Enumerated(EnumType.STRING) Concentracion concentracion;
    String salida, corazon, fondo;
    boolean publicada, activa;
    public FraganciaJpa() {}
}

