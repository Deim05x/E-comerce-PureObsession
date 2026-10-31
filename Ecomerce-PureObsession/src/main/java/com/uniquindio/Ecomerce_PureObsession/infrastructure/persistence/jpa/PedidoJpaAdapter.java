package com.uniquindio.Ecomerce_PureObsession.infrastructure.persistence.jpa;

import com.uniquindio.Ecomerce_PureObsession.domain.entity.*;
import com.uniquindio.Ecomerce_PureObsession.domain.repository.PedidoRepository;
import com.uniquindio.Ecomerce_PureObsession.domain.valueObject.*;
import jakarta.persistence.*;
import java.util.*;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public class PedidoJpaAdapter implements PedidoRepository {
    @PersistenceContext private EntityManager entityManager;
    @Override public Pedido guardar(Pedido pedido) { entityManager.merge(PedidoPersistenceMapper.aJpa(pedido)); return pedido; }
    @Override public Optional<Pedido> buscarPorId(UUID id) {
        return Optional.ofNullable(entityManager.find(PedidoJpa.class, id, LockModeType.PESSIMISTIC_WRITE)).map(PedidoPersistenceMapper::aDominio);
    }
    @Override public List<Pedido> listarPorCliente(UUID clienteId) {
        return entityManager.createQuery("select p from PedidoJpa p where p.clienteId = :id", PedidoJpa.class)
                .setParameter("id", clienteId).getResultList().stream().map(PedidoPersistenceMapper::aDominio).toList();
    }
    @Override public boolean existePedidoActivoConItem(UUID id) { return existe("itemId", id); }
    @Override public boolean existePedidoActivoConFragancia(UUID id) { return existe("fraganciaId", id); }
    private boolean existe(String campo, UUID id) {
        return entityManager.createQuery("select count(p) from PedidoJpa p join p.lineas l where l." + campo + " = :id and p.estado not in (:entregado, :cancelado)", Long.class)
                .setParameter("id", id).setParameter("entregado", EstadoPedido.ENTREGADO)
                .setParameter("cancelado", EstadoPedido.CANCELADO).getSingleResult() > 0;
    }
}
