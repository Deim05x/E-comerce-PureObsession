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
        return em.createQuery("select p from PresentacionJpa p, FraganciaJpa f where f.id=p.fraganciaId and "
            + ConsultaCatalogoJpaAdapter.PRESENTACION_PUBLICA + " order by lower(f.nombre), f.id, p.id",PresentacionJpa.class)
            .getResultList().stream().map(PresentacionPersistenceMapper::aDominio).toList();
    }
    public boolean estaEnCatalogo(UUID id) {
        return em.createQuery("select count(p) from PresentacionJpa p, FraganciaJpa f where f.id=p.fraganciaId and "
            + ConsultaCatalogoJpaAdapter.PRESENTACION_PUBLICA + " and p.id=:id",Long.class)
            .setParameter("id",id).getSingleResult()>0;
    }
}

