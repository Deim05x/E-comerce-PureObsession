package com.uniquindio.Ecomerce_PureObsession.infrastructure.persistence.jpa;

import com.uniquindio.Ecomerce_PureObsession.domain.entity.Presentacion;

public final class PresentacionPersistenceMapper {
    private PresentacionPersistenceMapper() {}

    public static PresentacionJpa aJpa(Presentacion p) {
        PresentacionJpa j = new PresentacionJpa();
        j.id = p.getId(); j.vendedorId = p.getVendedorId();
        j.fraganciaId = p.getFraganciaId(); j.botellaMadreId = p.getBotellaMadreId();
        j.tipo = p.getTipo(); j.volumenMl = p.getVolumenMl(); j.precio = p.getPrecio();
        j.origen = p.getOrigen(); j.paisOrigen = p.getPaisOrigen();
        j.original = p.isOriginal(); j.publicada = p.isPublicada();
        return j;
    }

    public static Presentacion aDominio(PresentacionJpa j) {
        Presentacion p = Presentacion.crear(j.id, j.vendedorId, j.fraganciaId, j.botellaMadreId,
                j.tipo, j.volumenMl, j.precio, j.origen, j.paisOrigen, j.original);
        // Reconstituir el estado persistido no vuelve a revisar el certificado.
        if (j.publicada) p.publicar(true);
        return p;
    }
}
