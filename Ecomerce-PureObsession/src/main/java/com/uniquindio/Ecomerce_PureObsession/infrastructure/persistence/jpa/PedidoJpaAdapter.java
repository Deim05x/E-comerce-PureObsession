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
    @Override public Pedido guardar(Pedido pedido) { entityManager.merge(aJpa(pedido)); return pedido; }
    @Override public Optional<Pedido> buscarPorId(UUID id) {
        return Optional.ofNullable(entityManager.find(PedidoJpa.class, id, LockModeType.PESSIMISTIC_WRITE)).map(this::aDominio);
    }
    @Override public List<Pedido> listarPorCliente(UUID clienteId) {
        return entityManager.createQuery("select p from PedidoJpa p where p.clienteId = :id", PedidoJpa.class)
                .setParameter("id", clienteId).getResultList().stream().map(this::aDominio).toList();
    }
    @Override public boolean existePedidoActivoConItem(UUID id) { return existe("itemId", id); }
    @Override public boolean existePedidoActivoConFragancia(UUID id) { return existe("fraganciaId", id); }
    private boolean existe(String campo, UUID id) {
        return entityManager.createQuery("select count(p) from PedidoJpa p join p.lineas l where l." + campo + " = :id and p.estado not in (:entregado, :cancelado)", Long.class)
                .setParameter("id", id).setParameter("entregado", EstadoPedido.ENTREGADO)
                .setParameter("cancelado", EstadoPedido.CANCELADO).getSingleResult() > 0;
    }
    private PedidoJpa aJpa(Pedido pedido) {
        PedidoJpa jpa = new PedidoJpa(); jpa.id = pedido.getId(); jpa.clienteId = pedido.getClienteId();
        jpa.estado = pedido.getEstado(); jpa.claveConsultaHash = pedido.getClaveConsultaHash();
        if (pedido.getEntrega() != null) {
            jpa.nombreEntrega = pedido.getEntrega().nombre(); jpa.correoEntrega = pedido.getEntrega().correo();
            jpa.telefonoEntrega = pedido.getEntrega().telefono(); jpa.direccionEntrega = pedido.getEntrega().direccion();
        }
        for (LineaPedido linea : pedido.getLineas()) {
            LineaJpa item = new LineaJpa(); item.id = linea.getId(); item.itemId = linea.getItemId(); item.fraganciaId = linea.getFraganciaId(); item.vendedorId = linea.getVendedorId();
            item.descripcion = linea.getDescripcionFragancia(); item.moneda = linea.getPrecioUnitario().moneda(); item.precio = linea.getPrecioUnitario().precio(); item.cantidad = linea.getCantidad(); jpa.lineas.add(item);
        }
        return jpa;
    }
    private Pedido aDominio(PedidoJpa pedido) {
        List<LineaPedido> lineas = pedido.lineas.stream().map(linea -> linea.vendedorId == null
                ? LineaPedido.crear(linea.id, linea.itemId, linea.fraganciaId, linea.descripcion, Precio.crear(linea.precio, linea.moneda), linea.cantidad)
                : LineaPedido.crearConVendedor(linea.id, linea.itemId, linea.fraganciaId, linea.vendedorId, linea.descripcion, Precio.crear(linea.precio, linea.moneda), linea.cantidad)).toList();
        if (pedido.nombreEntrega == null) return Pedido.reconstituir(pedido.id, pedido.clienteId, lineas, pedido.estado);
        DatosEntrega entrega = new DatosEntrega(pedido.nombreEntrega, pedido.correoEntrega, pedido.telefonoEntrega, pedido.direccionEntrega);
        return Pedido.reconstituir(pedido.id, pedido.clienteId, entrega, pedido.claveConsultaHash, lineas, pedido.estado);
    }
}
