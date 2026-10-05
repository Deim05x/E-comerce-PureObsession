package com.uniquindio.Ecomerce_PureObsession.infrastructure.persistence;

import com.uniquindio.Ecomerce_PureObsession.domain.entity.Pedido;
import com.uniquindio.Ecomerce_PureObsession.domain.repository.PedidoRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class PedidoRepositoryEnMemoria implements PedidoRepository {

    private final Map<UUID, Pedido> almacen = new HashMap<>();

    @Override
    public Pedido guardar(Pedido pedido) {
        almacen.put(pedido.getId(), pedido);
        return pedido;
    }

    @Override
    public Optional<Pedido> buscarPorId(UUID id) {
        return Optional.ofNullable(almacen.get(id));
    }

    @Override
    public List<Pedido> listarPorCliente(UUID clienteId) {
        return almacen.values().stream()
                .filter(p -> p.getClienteId().equals(clienteId))
                .toList();
    }

    @Override
    public boolean existePedidoActivoConItem(UUID itemId) {
        return almacen.values().stream()
                .anyMatch(p -> p.estaActivo() && p.contieneItem(itemId));
    }
}
