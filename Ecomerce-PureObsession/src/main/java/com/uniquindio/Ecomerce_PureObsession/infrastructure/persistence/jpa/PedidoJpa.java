package com.uniquindio.Ecomerce_PureObsession.infrastructure.persistence.jpa;
import jakarta.persistence.*;
import java.util.*;
import java.math.BigDecimal;
import com.uniquindio.Ecomerce_PureObsession.domain.valueObject.*;
@Entity
@Table(name="pedido")
public class PedidoJpa {
    @Id UUID id;
    UUID clienteId;
    @Enumerated(EnumType.STRING) EstadoPedido estado;
    @ElementCollection
    @CollectionTable(name="linea_pedido",joinColumns=@JoinColumn(name="pedido_id"))
    @OrderColumn(name="posicion")
    List<LineaJpa> lineas = new ArrayList<>();
    public PedidoJpa() {}
}

