package com.uniquindio.Ecomerce_PureObsession.infrastructure.persistence.jpa;
import jakarta.persistence.*;
import java.util.*;
import java.math.BigDecimal;
import com.uniquindio.Ecomerce_PureObsession.domain.valueObject.*;
@Entity
@Table(name="botella")
public class BotellaJpa {
    @Id UUID id;
    @ManyToOne(optional=false, cascade={CascadeType.PERSIST,CascadeType.MERGE}) FraganciaJpa fragancia;
    int originalMl, disponibleMl;
    @ElementCollection
    @CollectionTable(name="decant", joinColumns=@JoinColumn(name="botella_id"))
    @MapKeyColumn(name="decant_id")
    @Column(name="volumen_ml")
    Map<UUID,Integer> decants = new LinkedHashMap<>();
    public BotellaJpa() {}
}

