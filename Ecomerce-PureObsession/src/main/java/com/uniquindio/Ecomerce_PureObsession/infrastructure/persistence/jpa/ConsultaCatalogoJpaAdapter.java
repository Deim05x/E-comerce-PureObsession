package com.uniquindio.Ecomerce_PureObsession.infrastructure.persistence.jpa;

import com.uniquindio.Ecomerce_PureObsession.application.port.out.ConsultaCatalogoRepository;
import com.uniquindio.Ecomerce_PureObsession.domain.entity.*;
import com.uniquindio.Ecomerce_PureObsession.domain.repository.*;
import com.uniquindio.Ecomerce_PureObsession.domain.valueObject.*;
import jakarta.persistence.*;
import java.util.*;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional(readOnly=true)
public class ConsultaCatalogoJpaAdapter implements ConsultaCatalogoRepository {
    @PersistenceContext private EntityManager em;
    static final String FRAGANCIA_PUBLICA = "f.eliminada=false and f.activa=true and f.publicada=true "
            + "and exists (select b.id from BotellaJpa b where b.fragancia.id=f.id and b.eliminada=false)";
    static final String PRESENTACION_PUBLICA = "p.publicada=true and " + FRAGANCIA_PUBLICA
            + " and (p.botellaMadreId is null or exists (select b.id from BotellaJpa b "
            + "where b.id=p.botellaMadreId and b.fragancia.id=f.id and b.eliminada=false))";

    @Override public Pagina<Presentacion> buscarPresentaciones(String texto, FamiliaOlfativa familia, int pagina, int tamanio) {
        Pagina.validar(pagina,tamanio);
        String desde = " from PresentacionJpa p, FraganciaJpa f where f.id=p.fraganciaId and "
                + PRESENTACION_PUBLICA + filtros(familia);
        long total = parametros(em.createQuery("select count(p)" + desde,Long.class),texto,familia).getSingleResult();
        List<Presentacion> contenido = parametros(em.createQuery("select p" + desde + " order by lower(f.nombre), f.id, p.id",PresentacionJpa.class),texto,familia)
                .setFirstResult(pagina*tamanio).setMaxResults(tamanio).getResultList().stream()
                .map(PresentacionPersistenceMapper::aDominio).toList();
        return new Pagina<>(contenido,total,(int)Math.ceil((double)total/tamanio),pagina,tamanio);
    }

    @Override public Pagina<Fragancia> buscarFragancias(String texto, FamiliaOlfativa familia, int pagina, int tamanio) {
        Pagina.validar(pagina,tamanio);
        String desde = " from FraganciaJpa f where " + FRAGANCIA_PUBLICA + filtros(familia);
        long total = parametros(em.createQuery("select count(f)" + desde,Long.class),texto,familia).getSingleResult();
        List<Fragancia> contenido = parametros(em.createQuery("select f" + desde + " order by lower(f.nombre), f.id",FraganciaJpa.class),texto,familia)
                .setFirstResult(pagina*tamanio).setMaxResults(tamanio).getResultList().stream()
                .map(BotellaMadrePersistenceMapper::aDominio).toList();
        return new Pagina<>(contenido,total,(int)Math.ceil((double)total/tamanio),pagina,tamanio);
    }

    @Override public List<RankingFragancia> ranking(int limite) {
        if(limite<1 || limite>100) throw new com.uniquindio.Ecomerce_PureObsession.domain.exception.ReglaDominioException("Limite invalido.");
        return em.createQuery("select new com.uniquindio.Ecomerce_PureObsession.domain.repository.RankingFragancia(f.id,f.nombre,sum(l.cantidad)) "
                + "from PedidoJpa p join p.lineas l, FraganciaJpa f "
                + "where f.id=l.fraganciaId and f.eliminada=false and p.estado in :estados "
                + "group by f.id,f.nombre order by sum(l.cantidad) desc, lower(f.nombre), f.id",RankingFragancia.class)
                .setParameter("estados",List.of(EstadoPedido.CONFIRMADO,EstadoPedido.ENVIADO,EstadoPedido.ENTREGADO))
                .setMaxResults(limite).getResultList();
    }

    private String filtros(FamiliaOlfativa familia) {
        return " and lower(f.nombre) like :texto escape '!'" + (familia==null ? "" : " and f.familia=:familia");
    }
    private <T> TypedQuery<T> parametros(TypedQuery<T> query, String texto, FamiliaOlfativa familia) {
        String literal = texto==null ? "" : texto.trim().toLowerCase(Locale.ROOT);
        literal=literal.replace("!","!!").replace("%","!%").replace("_","!_");
        query.setParameter("texto","%"+literal+"%");
        if(familia!=null) query.setParameter("familia",familia);
        return query;
    }
}
