package com.uniquindio.Ecomerce_PureObsession.infrastructure.persistence.jpa;
import com.uniquindio.Ecomerce_PureObsession.domain.repository.PedidoRepository;
import com.uniquindio.Ecomerce_PureObsession.domain.entity.*;
import com.uniquindio.Ecomerce_PureObsession.domain.valueObject.*;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.*;
import java.util.*;
@Repository
@Transactional
public class PedidoJpaAdapter implements PedidoRepository {
    @PersistenceContext private EntityManager em;
    public Pedido guardar(Pedido p) {
        PedidoJpa j=new PedidoJpa(); j.id=p.getId(); j.clienteId=p.getClienteId(); j.estado=p.getEstado();
        for(LineaPedido l:p.getLineas()) {
            LineaJpa x=new LineaJpa(); x.id=l.getId(); x.itemId=l.getItemId(); x.fraganciaId=l.getFraganciaId();
            x.descripcion=l.getDescripcionFragancia(); x.moneda=l.getPrecioUnitario().moneda();
            x.precio=l.getPrecioUnitario().precio(); x.cantidad=l.getCantidad(); j.lineas.add(x);
        }
        em.merge(j); return p;
    }
    public Optional<Pedido> buscarPorId(UUID id) { return Optional.ofNullable(em.find(PedidoJpa.class,id)).map(this::mapear); }
    public List<Pedido> listarPorCliente(UUID id) {
        return em.createQuery("select p from PedidoJpa p where p.clienteId=:id",PedidoJpa.class)
            .setParameter("id",id).getResultList().stream().map(this::mapear).toList();
    }
    public boolean existePedidoActivoConItem(UUID id) { return existe("itemId",id); }
    public boolean existePedidoActivoConFragancia(UUID id) { return existe("fraganciaId",id); }
    private boolean existe(String campo,UUID id) {
        return em.createQuery("select count(p) from PedidoJpa p join p.lineas l where l."+campo+"=:id and p.estado not in (:entregado,:cancelado)",Long.class)
            .setParameter("id",id).setParameter("entregado",EstadoPedido.ENTREGADO)
            .setParameter("cancelado",EstadoPedido.CANCELADO).getSingleResult()>0;
    }
    private Pedido mapear(PedidoJpa p) {
        return Pedido.reconstituir(p.id,p.clienteId,p.lineas.stream()
            .map(l->LineaPedido.crear(l.id,l.itemId,l.fraganciaId,l.descripcion,Precio.crear(l.precio,l.moneda),l.cantidad)).toList(),p.estado);
    }
}

