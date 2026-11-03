package com.uniquindio.Ecomerce_PureObsession.infrastructure.persistence.jpa;
import jakarta.persistence.*;
import java.util.UUID;
@Entity @Table(name="seleccion_cliente",uniqueConstraints=@UniqueConstraint(columnNames={"clienteId","productoId"}))
public class SeleccionClienteJpa {
    @Id String id;
    UUID clienteId,productoId;
    int cantidad;
    boolean favorito;
    public SeleccionClienteJpa() {}
}
