package com.uniquindio.Ecomerce_PureObsession.infrastructure.persistence.jpa;
import jakarta.persistence.*;
import java.util.UUID;
import java.time.Instant;
@Entity @Table(name="envio_vendedor",uniqueConstraints=@UniqueConstraint(columnNames={"pedidoId","vendedorId"}))
public class EnvioVendedorJpa {
    @Id String id;
    UUID pedidoId,vendedorId;
    @Column(length=30) String estado;
    @Column(length=100) String transportadora,guia;
    Instant enviadoEn,entregadoEn;
    public EnvioVendedorJpa() {}
}
