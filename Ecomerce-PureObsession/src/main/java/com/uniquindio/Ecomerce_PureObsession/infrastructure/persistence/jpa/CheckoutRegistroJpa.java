package com.uniquindio.Ecomerce_PureObsession.infrastructure.persistence.jpa;
import jakarta.persistence.*;
import java.util.UUID;
@Entity @Table(name="checkout_registro")
public class CheckoutRegistroJpa {
    @Id @Column(length=64) String id;
    @Column(length=64) String huella;
    UUID pedidoId;
    public CheckoutRegistroJpa() {}
}
