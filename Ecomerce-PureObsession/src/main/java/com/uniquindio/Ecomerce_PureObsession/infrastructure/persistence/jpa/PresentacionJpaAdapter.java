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
        em.merge(PresentacionPersistenceMapper.aJpa(p)); return p;
    }
    public Optional<Presentacion> buscarPorId(UUID id) {
        return Optional.ofNullable(em.find(PresentacionJpa.class,id,LockModeType.PESSIMISTIC_WRITE)).map(PresentacionPersistenceMapper::aDominio);
    }
    public List<Presentacion> listarPublicadas() {
        return em.createQuery("select p from PresentacionJpa p where p.publicada=true",PresentacionJpa.class)
            .getResultList().stream().map(PresentacionPersistenceMapper::aDominio).toList();
    }
}

