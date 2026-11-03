package com.uniquindio.Ecomerce_PureObsession.infrastructure.persistence.jpa;

import com.uniquindio.Ecomerce_PureObsession.application.model.Marketplace.*;
import com.uniquindio.Ecomerce_PureObsession.application.port.out.*;
import com.uniquindio.Ecomerce_PureObsession.application.port.out.GestionPedidosRepository.*;
import com.uniquindio.Ecomerce_PureObsession.application.dto.response.PedidoResponse;
import com.uniquindio.Ecomerce_PureObsession.domain.entity.Pedido;
import com.uniquindio.Ecomerce_PureObsession.domain.exception.*;
import com.uniquindio.Ecomerce_PureObsession.domain.repository.*;
import com.uniquindio.Ecomerce_PureObsession.domain.valueObject.EstadoPedido;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository @Transactional
public class GestionPedidosJpaAdapter implements GestionPedidosRepository {
    @PersistenceContext private EntityManager em;
    private final PedidoRepository pedidos;
    private final InventarioVentaPort inventario;
    public GestionPedidosJpaAdapter(PedidoRepository pedidos,InventarioVentaPort inventario) {this.pedidos=pedidos;this.inventario=inventario;}
    private PedidoJpa pedido(UUID id) {
        PedidoJpa p=em.find(PedidoJpa.class,id,LockModeType.PESSIMISTIC_WRITE);
        if(p==null) throw new RecursoNoEncontradoException("El pedido no existe.");return p;
    }
    private Envio vista(EnvioVendedorJpa e) {return new Envio(e.vendedorId,e.estado,e.transportadora,e.guia,e.enviadoEn,e.entregadoEn);}
    @Override public Detalle detalle(UUID id) {
        PedidoJpa p=pedido(id); PagoRegistroJpa pago=em.find(PagoRegistroJpa.class,id);
        var envios=em.createQuery("select e from EnvioVendedorJpa e where e.pedidoId=:id order by e.vendedorId",EnvioVendedorJpa.class)
                .setParameter("id",id).getResultList().stream().map(this::vista).toList();
        return new Detalle(PedidoResponse.desde(PedidoPersistenceMapper.aDominio(p),null),p.creadoEn,p.reservaVenceEn,p.motivoCancelacion,
                pago==null ? null : new Pago(pago.referencia,pago.importe,pago.simulado,pago.registradoEn),envios);
    }
    @Override public Detalle confirmar(UUID id,String referencia,boolean simulado) {
        PedidoJpa p=pedido(id);
        if(referencia==null || referencia.isBlank() || referencia.length()>120) throw new ReglaDominioException("Referencia de pago obligatoria, maximo 120 caracteres.");
        referencia=referencia.trim();
        PagoRegistroJpa previo=em.find(PagoRegistroJpa.class,id);
        if(previo!=null) {
            if(!previo.referencia.equals(referencia)) throw new ReglaDominioException("El pedido ya tiene otro pago registrado.");
            return detalle(id);
        }
        if(p.reservaVenceEn!=null && p.reservaVenceEn.isBefore(Instant.now())) throw new ReglaDominioException("La reserva vencio; cree un nuevo pedido.");
        Pedido dominio=PedidoPersistenceMapper.aDominio(p);dominio.confirmar();
        dominio.getLineas().stream().sorted(Comparator.comparing(l->l.getItemId().toString())).forEach(l->inventario.confirmar(l.getItemId(),l.getCantidad()));
        PagoRegistroJpa pago=new PagoRegistroJpa();pago.pedidoId=id;pago.referencia=referencia;
        pago.importe=dominio.getTotal().monto();pago.simulado=simulado;pago.registradoEn=Instant.now();em.persist(pago);
        for(UUID vendedor:dominio.getLineas().stream().map(l->l.getVendedorId()).filter(Objects::nonNull).distinct().toList()) {
            EnvioVendedorJpa envio=new EnvioVendedorJpa();envio.id=id+":"+vendedor;envio.pedidoId=id;envio.vendedorId=vendedor;envio.estado="PENDIENTE";em.persist(envio);
        }
        pedidos.guardar(dominio);em.flush();return detalle(id);
    }
    private EnvioVendedorJpa envio(UUID id,UUID vendedor) {
        EnvioVendedorJpa e=em.find(EnvioVendedorJpa.class,id+":"+vendedor,LockModeType.PESSIMISTIC_WRITE);
        if(e==null) {
            PedidoJpa p=pedido(id);
            if(p.estado==EstadoPedido.CREADO || p.estado==EstadoPedido.CANCELADO || p.lineas.stream().noneMatch(l->vendedor.equals(l.vendedorId)))
                throw new ReglaDominioException("No existe un envio confirmado para ese vendedor.");
            // Compatibilidad con pedidos confirmados anteriores a los envios por vendedor.
            e=new EnvioVendedorJpa();e.id=id+":"+vendedor;e.pedidoId=id;e.vendedorId=vendedor;
            e.estado=p.estado==EstadoPedido.ENTREGADO ? "ENTREGADO" : p.estado==EstadoPedido.ENVIADO ? "ENVIADO" : "PENDIENTE";em.persist(e);
        }return e;
    }
    @Override public Detalle enviar(UUID id,UUID vendedor,String transportadora,String guia) {
        PedidoJpa p=pedido(id);
        if(p.estado!=EstadoPedido.CONFIRMADO && p.estado!=EstadoPedido.ENVIADO) throw new ReglaDominioException("El pedido debe estar confirmado.");
        if(transportadora==null || transportadora.isBlank() || transportadora.length()>100 || guia==null || guia.isBlank() || guia.length()>100)
            throw new ReglaDominioException("Transportadora y guia obligatorias, maximo 100 caracteres.");
        EnvioVendedorJpa e=envio(id,vendedor);
        if(!"PENDIENTE".equals(e.estado)) throw new ReglaDominioException("El envio ya fue despachado.");
        e.estado="ENVIADO";e.transportadora=transportadora;e.guia=guia;e.enviadoEn=Instant.now();em.flush();
        if(em.createQuery("select count(e) from EnvioVendedorJpa e where e.pedidoId=:id and e.estado='PENDIENTE'",Long.class).setParameter("id",id).getSingleResult()==0 && p.estado==EstadoPedido.CONFIRMADO) {
            Pedido d=PedidoPersistenceMapper.aDominio(p);d.marcarEnviado();pedidos.guardar(d);
        }
        return detalle(id);
    }
    @Override public Detalle recibir(UUID id,UUID vendedor) {
        PedidoJpa p=pedido(id);EnvioVendedorJpa e=envio(id,vendedor);
        if(!"ENVIADO".equals(e.estado)) throw new ReglaDominioException("Solo puede recibir un envio despachado.");
        e.estado="ENTREGADO";e.entregadoEn=Instant.now();em.flush();
        if(em.createQuery("select count(e) from EnvioVendedorJpa e where e.pedidoId=:id and e.estado<>'ENTREGADO'",Long.class).setParameter("id",id).getSingleResult()==0) {
            Pedido d=PedidoPersistenceMapper.aDominio(p);if(d.getEstado()==EstadoPedido.CONFIRMADO)d.marcarEnviado();d.marcarEntregado();pedidos.guardar(d);
        }
        return detalle(id);
    }
    @Override public Pagina<Venta> ventas(UUID vendedor,int pagina,int tamanio) {
        String desde=" from PedidoJpa p where exists(select l.id from p.lineas l where l.vendedorId=:vendedor)";
        long total=em.createQuery("select count(p)"+desde,Long.class).setParameter("vendedor",vendedor).getSingleResult();
        var contenido=em.createQuery("select p"+desde+" order by p.creadoEn desc,p.id",PedidoJpa.class).setParameter("vendedor",vendedor)
                .setFirstResult(pagina*tamanio).setMaxResults(tamanio).getResultList().stream().map(p->{
                    var propias=PedidoResponse.desde(PedidoPersistenceMapper.aDominio(p),null).lineas().stream().filter(l->vendedor.equals(l.vendedorId())).toList();
                    BigDecimal subtotal=propias.stream().map(l->l.precioUnitario().multiply(BigDecimal.valueOf(l.cantidad()))).reduce(BigDecimal.ZERO,BigDecimal::add);
                    EnvioVendedorJpa e=em.find(EnvioVendedorJpa.class,p.id+":"+vendedor);
                    return new Venta(p.id,p.estado,PedidoPersistenceMapper.aDominio(p).getEntrega(),p.creadoEn,p.reservaVenceEn,subtotal,propias,e==null ? null : vista(e));
                }).toList();
        return new Pagina<>(contenido,total,(int)Math.ceil((double)total/tamanio),pagina,tamanio);
    }
    @Override public Pagina<Detalle> administrar(int pagina,int tamanio) {
        long total=em.createQuery("select count(p) from PedidoJpa p",Long.class).getSingleResult();
        var lista=em.createQuery("select p.id from PedidoJpa p order by p.creadoEn desc,p.id",UUID.class).setFirstResult(pagina*tamanio).setMaxResults(tamanio)
                .getResultList().stream().map(this::detalle).toList();
        return new Pagina<>(lista,total,(int)Math.ceil((double)total/tamanio),pagina,tamanio);
    }
    @Override public int expirar() {
        var ids=em.createQuery("select p.id from PedidoJpa p where p.estado=:estado and p.reservaVenceEn<:ahora order by p.id",UUID.class)
                .setParameter("estado",EstadoPedido.CREADO).setParameter("ahora",Instant.now()).setMaxResults(100).getResultList();
        int cancelados=0;
        for(UUID id:ids) {
            PedidoJpa p=pedido(id);
            if(p.estado!=EstadoPedido.CREADO || p.reservaVenceEn==null || !p.reservaVenceEn.isBefore(Instant.now())) continue;
            Pedido d=PedidoPersistenceMapper.aDominio(p);d.cancelarAntesDePago();
            d.getLineas().stream().sorted(Comparator.comparing(l->l.getItemId().toString())).forEach(l->inventario.liberar(l.getItemId(),l.getCantidad()));
            p.motivoCancelacion="RESERVA_VENCIDA";pedidos.guardar(d);cancelados++;
        }
        return cancelados;
    }
}
