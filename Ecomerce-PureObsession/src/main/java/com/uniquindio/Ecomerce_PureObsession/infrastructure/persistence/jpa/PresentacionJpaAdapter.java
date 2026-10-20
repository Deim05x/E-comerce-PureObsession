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
        PresentacionJpa j=new PresentacionJpa(); j.id=p.getId(); j.vendedorId=p.getVendedorId();
        j.fraganciaId=p.getFraganciaId(); j.botellaMadreId=p.getBotellaMadreId(); j.tipo=p.getTipo();
        j.volumenMl=p.getVolumenMl(); j.precio=p.getPrecio(); j.origen=p.getOrigen();
        j.paisOrigen=p.getPaisOrigen(); j.original=p.isOriginal(); j.publicada=p.isPublicada();
        em.merge(j); return p;
    }
    public Optional<Presentacion> buscarPorId(UUID id) {
        return Optional.ofNullable(em.find(PresentacionJpa.class,id,LockModeType.PESSIMISTIC_WRITE)).map(this::mapear);
    }
    public List<Presentacion> listarPublicadas() {
        return em.createQuery("select p from PresentacionJpa p where p.publicada=true",PresentacionJpa.class)
            .getResultList().stream().map(this::mapear).toList();
    }
    private Presentacion mapear(PresentacionJpa p) {
        Presentacion d=Presentacion.crear(p.id,p.vendedorId,p.fraganciaId,p.botellaMadreId,p.tipo,p.volumenMl,p.precio,p.origen,p.paisOrigen,p.original);
        if(p.publicada) d.publicar(true);
        return d;
    }
}

