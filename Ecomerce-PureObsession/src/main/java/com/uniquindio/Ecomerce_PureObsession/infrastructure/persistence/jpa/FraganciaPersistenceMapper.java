package com.uniquindio.Ecomerce_PureObsession.infrastructure.persistence.jpa;
import com.uniquindio.Ecomerce_PureObsession.domain.entity.Fragancia;
public final class FraganciaPersistenceMapper {
    private FraganciaPersistenceMapper() {}
    public static FraganciaJpa aJpa(Fragancia f) {
        FraganciaJpa j=new FraganciaJpa();j.id=f.getId();j.nombre=f.getNombre();j.familia=f.getFamiliaOlfativa();j.concentracion=f.getConcentracion();
        j.activa=f.isActiva();j.publicada=f.isPublicada();
        if(f.getPiramideOlfativa()!=null) {j.salida=f.getPiramideOlfativa().notasSalida();j.corazon=f.getPiramideOlfativa().notasCorazon();j.fondo=f.getPiramideOlfativa().notasFondo();}
        return j;
    }
}
