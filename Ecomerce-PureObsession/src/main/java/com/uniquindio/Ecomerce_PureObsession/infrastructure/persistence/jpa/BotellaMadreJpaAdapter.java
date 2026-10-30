package com.uniquindio.Ecomerce_PureObsession.infrastructure.persistence.jpa;
import com.uniquindio.Ecomerce_PureObsession.domain.repository.BotellaMadreRepository;
import com.uniquindio.Ecomerce_PureObsession.domain.entity.*;
import com.uniquindio.Ecomerce_PureObsession.domain.valueObject.*;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.*;
import java.util.*;
@Repository
@Transactional
public class BotellaMadreJpaAdapter implements BotellaMadreRepository {
    @PersistenceContext private EntityManager em;
    public BotellaMadre guardar(BotellaMadre b) {
        BotellaJpa anterior=em.find(BotellaJpa.class,b.getId());
        FraganciaJpa fragancia=em.find(FraganciaJpa.class,b.getFragancia().getId());
        if((anterior!=null && anterior.eliminada) || (fragancia!=null && fragancia.eliminada))
            throw new com.uniquindio.Ecomerce_PureObsession.domain.exception.ReglaDominioException("No se puede restaurar inventario eliminado al guardar.");
        em.merge(BotellaMadrePersistenceMapper.aJpa(b)); return b;
    }
    public Optional<BotellaMadre> buscarPorId(UUID id) {
        return Optional.ofNullable(em.find(BotellaJpa.class,id,LockModeType.PESSIMISTIC_WRITE))
            .filter(b->!b.eliminada && !b.fragancia.eliminada).map(BotellaMadrePersistenceMapper::aDominio);
    }
    public List<BotellaMadre> listarTodas() {
        return em.createQuery("select b from BotellaJpa b where b.eliminada=false and b.fragancia.eliminada=false",BotellaJpa.class)
            .getResultList().stream().map(BotellaMadrePersistenceMapper::aDominio).toList();
    }
    public void eliminar(UUID id) {
        BotellaJpa botella=em.find(BotellaJpa.class,id,LockModeType.PESSIMISTIC_WRITE);
        if(botella==null || botella.eliminada) return;
        botella.eliminada=true;
        em.flush();
        long restantes=em.createQuery("select count(b) from BotellaJpa b where b.fragancia.id=:id and b.eliminada=false",Long.class)
            .setParameter("id",botella.fragancia.id).getSingleResult();
        if(restantes==0) {
            botella.fragancia.eliminada=true;
            botella.fragancia.activa=false;
            botella.fragancia.publicada=false;
        }
    }
}

