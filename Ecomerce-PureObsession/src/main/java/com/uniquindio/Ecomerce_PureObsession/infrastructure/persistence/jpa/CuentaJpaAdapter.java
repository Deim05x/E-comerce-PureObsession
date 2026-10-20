package com.uniquindio.Ecomerce_PureObsession.infrastructure.persistence.jpa;
import com.uniquindio.Ecomerce_PureObsession.application.port.out.CuentaRepository;
import com.uniquindio.Ecomerce_PureObsession.domain.entity.Cuenta;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.*;
import java.util.*;
@Repository
@Transactional
public class CuentaJpaAdapter implements CuentaRepository {
    @PersistenceContext private EntityManager em;
    public Cuenta guardar(Cuenta c) {
        CuentaJpa j=new CuentaJpa(); j.id=c.getId(); j.nombre=c.getNombre(); j.cedula=c.getCedula();
        j.email=c.getEmail(); j.claveHash=c.getClaveHash(); j.rol=c.getRol(); em.persist(j); em.flush(); return c;
    }
    public Optional<Cuenta> buscarPorEmail(String email) {
        return em.createQuery("select c from CuentaJpa c where c.email=:email",CuentaJpa.class)
            .setParameter("email",email).getResultStream().findFirst().map(this::mapear);
    }
    public Optional<Cuenta> buscarPorId(UUID id) { return Optional.ofNullable(em.find(CuentaJpa.class,id)).map(this::mapear); }
    public boolean existeCedula(String cedula) {
        return em.createQuery("select count(c) from CuentaJpa c where c.cedula=:cedula",Long.class)
            .setParameter("cedula",cedula).getSingleResult()>0;
    }
    private Cuenta mapear(CuentaJpa c) { return Cuenta.crear(c.id,c.nombre,c.cedula,c.email,c.claveHash,c.rol); }
}

