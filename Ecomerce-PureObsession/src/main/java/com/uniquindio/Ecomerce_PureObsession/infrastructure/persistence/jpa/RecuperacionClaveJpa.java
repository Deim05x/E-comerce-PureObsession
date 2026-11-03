package com.uniquindio.Ecomerce_PureObsession.infrastructure.persistence.jpa;
import jakarta.persistence.*;
import java.util.UUID;
import java.time.Instant;
@Entity @Table(name="recuperacion_clave")
public class RecuperacionClaveJpa {
    @Id String hash;
    UUID cuentaId;
    Instant venceEn;
    boolean utilizada;
    public RecuperacionClaveJpa() {}
}
