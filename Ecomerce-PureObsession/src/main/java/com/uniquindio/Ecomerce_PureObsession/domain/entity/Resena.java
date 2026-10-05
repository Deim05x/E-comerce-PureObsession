package com.uniquindio.Ecomerce_PureObsession.domain.entity;

import com.uniquindio.Ecomerce_PureObsession.domain.exception.ReglaDominioException;

import java.util.Objects;
import java.util.UUID;

public class Resena {
    private final UUID id;
    private final UUID pedidoId;
    private final UUID clienteId;
    private final String comentario;

    private Resena(UUID id, UUID pedidoId, UUID clienteId, String comentario) {
        if (id == null) throw new ReglaDominioException("La identidad es obligatoria.");
        if (comentario == null || comentario.isBlank()) {
            throw new ReglaDominioException("El comentario de la reseña es obligatorio.");
        }
        this.id = id;
        this.pedidoId = pedidoId;
        this.clienteId = clienteId;
        this.comentario = comentario;
    }

    public static Resena crear(UUID id, Pedido pedido, UUID clienteId, String comentario) {
        if (pedido == null || clienteId == null) {
            throw new ReglaDominioException("El pedido y el cliente son obligatorios.");
        }
        if (!pedido.getClienteId().equals(clienteId)) {
            throw new ReglaDominioException("Solo el cliente que realizó el pedido puede crear una reseña.");
        }
        if (!pedido.permiteResena()) {
            throw new ReglaDominioException("Solo se puede crear una reseña para un pedido entregado.");
        }
        return new Resena(id, pedido.getId(), clienteId, comentario);
    }

    public UUID getId() { return id; }
    public UUID getPedidoId() { return pedidoId; }
    public UUID getClienteId() { return clienteId; }
    public String getComentario() { return comentario; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Resena resena = (Resena) o;
        return Objects.equals(id, resena.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
