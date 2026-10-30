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
        em.merge(BotellaMadrePersistenceMapper.aJpa(b)); return b;
    }
    public Optional<BotellaMadre> buscarPorId(UUID id) {
        return Optional.ofNullable(em.find(BotellaJpa.class,id,LockModeType.PESSIMISTIC_WRITE)).map(BotellaMadrePersistenceMapper::aDominio);
    }
    public List<BotellaMadre> listarTodas() {
        return em.createQuery("select b from BotellaJpa b",BotellaJpa.class).getResultList().stream().map(BotellaMadrePersistenceMapper::aDominio).toList();
    }
    public void eliminar(UUID id) { BotellaJpa j=em.find(BotellaJpa.class,id); if(j!=null) em.remove(j); }
}

