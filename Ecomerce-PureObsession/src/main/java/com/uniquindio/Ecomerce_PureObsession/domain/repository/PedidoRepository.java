package com.uniquindio.Ecomerce_PureObsession.domain.repository;

import com.uniquindio.Ecomerce_PureObsession.domain.entity.Pedido;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
public interface PedidoRepository {
    Pedido guardar(Pedido pedido);
    Optional<Pedido> buscarPorId(UUID id);
    List<Pedido> listarPorCliente(UUID clienteId);
    // Soporte para la Regla 8: ¿hay pedidos activos que contengan este item?
    boolean existePedidoActivoConItem(UUID itemId);
}
