package com.uniquindio.Ecomerce_PureObsession.infrastructure.persistence.jpa;
import com.uniquindio.Ecomerce_PureObsession.application.port.out.PresentacionRepository;
import com.uniquindio.Ecomerce_PureObsession.domain.entity.Presentacion;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.*;
import java.util.*;
@Repository
@Transactional
public class PresentacionJpaAdapter implements PresentacionRepository {
    @PersistenceContext private EntityManager em;
    public Presentacion guardar(Presentacion p) {
        if(p.getBotellaMadreId()!=null) {
            BotellaJpa b=em.find(BotellaJpa.class,p.getBotellaMadreId());
            if(b!=null && b.vendedorId!=null)
                com.uniquindio.Ecomerce_PureObsession.domain.service.PoliticaMarketplace.propietario(b.vendedorId,p.getVendedorId());
        }
        if(p.isPublicada()) {
            FraganciaJpa f=em.find(FraganciaJpa.class,p.getFraganciaId());
            if(f!=null) {
                if(f.eliminada || !f.activa) throw new com.uniquindio.Ecomerce_PureObsession.domain.exception.ReglaDominioException("La fragancia esta retirada.");
                f.publicada=true;
            }
        }
        em.merge(PresentacionPersistenceMapper.aJpa(p)); return p;
    }
    public Optional<Presentacion> buscarPorId(UUID id) {
        return Optional.ofNullable(em.find(PresentacionJpa.class,id,LockModeType.PESSIMISTIC_WRITE)).map(PresentacionPersistenceMapper::aDominio);
    }
    public List<Presentacion> listarPublicadas() {
        return em.createQuery("select p from PresentacionJpa p, FraganciaJpa f where f.id=p.fraganciaId and "
            + ConsultaCatalogoJpaAdapter.PRESENTACION_PUBLICA + " order by lower(f.nombre), f.id, p.id",PresentacionJpa.class)
            .getResultList().stream().map(PresentacionPersistenceMapper::aDominio).toList();
    }
    public boolean estaEnCatalogo(UUID id) {
        return em.createQuery("select count(p) from PresentacionJpa p, FraganciaJpa f where f.id=p.fraganciaId and "
            + ConsultaCatalogoJpaAdapter.PRESENTACION_PUBLICA + " and p.id=:id",Long.class)
            .setParameter("id",id).getSingleResult()>0;
    }
    public void validarPublicacion(Presentacion p) {
        FraganciaJpa f=em.find(FraganciaJpa.class,p.getFraganciaId());
        if(f==null || f.eliminada || !f.activa) throw new com.uniquindio.Ecomerce_PureObsession.domain.exception.ReglaDominioException("La fragancia no esta activa.");
        BotellaMadrePersistenceMapper.aDominio(f).publicar();
        if(p.getBotellaMadreId()!=null) {
            BotellaJpa b=em.find(BotellaJpa.class,p.getBotellaMadreId());
            if(b==null || b.eliminada || !b.fragancia.id.equals(f.id))
                throw new com.uniquindio.Ecomerce_PureObsession.domain.exception.ReglaDominioException("La botella no esta disponible.");
            if(b.vendedorId!=null) com.uniquindio.Ecomerce_PureObsession.domain.service.PoliticaMarketplace.propietario(b.vendedorId,p.getVendedorId());
        }
    }
}

