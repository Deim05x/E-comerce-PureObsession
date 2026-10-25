package com.uniquindio.Ecomerce_PureObsession.infrastructure.persistence;

import com.uniquindio.Ecomerce_PureObsession.domain.entity.Pedido;
import com.uniquindio.Ecomerce_PureObsession.domain.repository.PedidoRepository;
import java.util.*;

/** Repositorio para pruebas aisladas, también soporta clientes invitados. */
public class PedidoRepositoryEnMemoria implements PedidoRepository {
    private final Map<UUID, Pedido> almacen = new HashMap<>();
    public Pedido guardar(Pedido pedido) { almacen.put(pedido.getId(), pedido); return pedido; }
    public Optional<Pedido> buscarPorId(UUID id) { return Optional.ofNullable(almacen.get(id)); }
    public List<Pedido> listarPorCliente(UUID id) { return almacen.values().stream().filter(p -> Objects.equals(p.getClienteId(), id)).toList(); }
    public boolean existePedidoActivoConItem(UUID id) { return almacen.values().stream().anyMatch(p -> p.estaActivo() && p.contieneItem(id)); }
    public boolean existePedidoActivoConFragancia(UUID id) { return almacen.values().stream().anyMatch(p -> p.estaActivo() && p.contieneFragancia(id)); }
}
