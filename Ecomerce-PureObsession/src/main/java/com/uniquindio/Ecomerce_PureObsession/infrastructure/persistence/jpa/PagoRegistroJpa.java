package com.uniquindio.Ecomerce_PureObsession.infrastructure.persistence.jpa;
import jakarta.persistence.*;
import java.util.UUID;
import java.time.Instant;
import java.math.BigDecimal;
@Entity @Table(name="pago_registro")
public class PagoRegistroJpa {
    @Id UUID pedidoId;
    @Column(nullable=false,unique=true,length=120) String referencia;
    @Column(precision=14,scale=2,nullable=false) BigDecimal importe;
    boolean simulado;
    Instant registradoEn;
    public PagoRegistroJpa() {}
}
