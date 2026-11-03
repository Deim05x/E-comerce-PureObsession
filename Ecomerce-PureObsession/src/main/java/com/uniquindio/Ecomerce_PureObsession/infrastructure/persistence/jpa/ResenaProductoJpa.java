package com.uniquindio.Ecomerce_PureObsession.infrastructure.persistence.jpa;
import jakarta.persistence.*;
import java.util.UUID;
import java.time.Instant;
@Entity @Table(name="resena_producto",uniqueConstraints=@UniqueConstraint(columnNames={"clienteId","productoId"}))
public class ResenaProductoJpa {
    @Id UUID id;
    UUID clienteId,productoId;
    int puntuacion;
    @Column(length=2000) String comentario;
    Instant creadaEn;
    public ResenaProductoJpa() {}
}
