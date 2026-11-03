package com.uniquindio.Ecomerce_PureObsession.infrastructure.persistence.jpa;

import com.uniquindio.Ecomerce_PureObsession.domain.valueObject.EstadoPedido;
import jakarta.persistence.*;
import java.util.*;

@Entity
@Table(name = "pedido")
public class PedidoJpa {
    @Id UUID id;
    java.time.Instant creadoEn,reservaVenceEn;
    @Column(precision=14,scale=2) java.math.BigDecimal envio,regalo;
    @Column(length=120) String motivoCancelacion;
    @Column(nullable = true) UUID clienteId;
    @Column(length = 120) String nombreEntrega;
    @Column(length = 254) String correoEntrega;
    @Column(length = 22) String telefonoEntrega;
    @Column(length = 350) String direccionEntrega;
    @Column(length = 64) String claveConsultaHash;
    @Enumerated(EnumType.STRING) EstadoPedido estado;
    @ElementCollection
    @CollectionTable(name = "linea_pedido", joinColumns = @JoinColumn(name = "pedido_id"))
    @OrderColumn(name = "posicion")
    List<LineaJpa> lineas = new ArrayList<>();
    public PedidoJpa() {}
}
