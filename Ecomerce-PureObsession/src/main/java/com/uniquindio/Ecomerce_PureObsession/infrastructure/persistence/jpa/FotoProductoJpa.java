package com.uniquindio.Ecomerce_PureObsession.infrastructure.persistence.jpa;
import jakarta.persistence.*;
import java.util.UUID;
@Entity @Table(name="foto_producto")
public class FotoProductoJpa {
    @Id UUID id;
    UUID productoId;
    String tipo;
    @Lob byte[] contenido;
    boolean eliminada;
    public FotoProductoJpa() {}
}
