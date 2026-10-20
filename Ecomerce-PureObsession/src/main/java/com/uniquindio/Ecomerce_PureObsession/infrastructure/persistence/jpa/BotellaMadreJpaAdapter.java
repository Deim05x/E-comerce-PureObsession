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
        BotellaJpa j=new BotellaJpa(); j.id=b.getId(); j.originalMl=b.getVolumenOriginal().mililitros();
        j.disponibleMl=b.getVolumenDisponible().mililitros();
        Fragancia f=b.getFragancia(); FraganciaJpa jf=new FraganciaJpa();
        jf.id=f.getId(); jf.nombre=f.getNombre(); jf.familia=f.getFamiliaOlfativa(); jf.concentracion=f.getConcentracion();
        jf.activa=f.isActiva(); jf.publicada=f.isPublicada();
        if(f.getPiramideOlfativa()!=null) {
            jf.salida=f.getPiramideOlfativa().notasSalida(); jf.corazon=f.getPiramideOlfativa().notasCorazon();
            jf.fondo=f.getPiramideOlfativa().notasFondo();
        }
        j.fragancia=jf; b.getDecants().forEach(d->j.decants.put(d.getId(),d.getVolumen().mililitros()));
        em.merge(j); return b;
    }
    public Optional<BotellaMadre> buscarPorId(UUID id) {
        return Optional.ofNullable(em.find(BotellaJpa.class,id,LockModeType.PESSIMISTIC_WRITE)).map(this::mapear);
    }
    public List<BotellaMadre> listarTodas() {
        return em.createQuery("select b from BotellaJpa b",BotellaJpa.class).getResultList().stream().map(this::mapear).toList();
    }
    public void eliminar(UUID id) { BotellaJpa j=em.find(BotellaJpa.class,id); if(j!=null) em.remove(j); }
    private BotellaMadre mapear(BotellaJpa j) {
        FraganciaJpa f=j.fragancia;
        Fragancia frag=Fragancia.crear(f.id,f.nombre,f.familia,f.concentracion,
            f.salida==null ? null : PiramideOlfativa.crear(f.salida,f.corazon,f.fondo));
        if(f.publicada) frag.publicar();
        if(!f.activa) frag.desactivar();
        return BotellaMadre.reconstituir(j.id,frag,j.originalMl,j.disponibleMl,
            j.decants.entrySet().stream().map(e->new BotellaMadre.DecantGuardado(e.getKey(),e.getValue())).toList());
    }
}

