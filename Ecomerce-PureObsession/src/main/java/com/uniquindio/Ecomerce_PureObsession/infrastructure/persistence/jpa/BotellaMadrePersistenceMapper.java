package com.uniquindio.Ecomerce_PureObsession.infrastructure.persistence.jpa;

import com.uniquindio.Ecomerce_PureObsession.domain.entity.*;
import com.uniquindio.Ecomerce_PureObsession.domain.valueObject.PiramideOlfativa;

/** Traduce el agregado completo, incluidos el volumen restante y sus decants. */
public final class BotellaMadrePersistenceMapper {
    private BotellaMadrePersistenceMapper() {}

    public static BotellaJpa aJpa(BotellaMadre botella) {
        BotellaJpa jpa = new BotellaJpa();
        jpa.id = botella.getId();
        jpa.originalMl = botella.getVolumenOriginal().mililitros();
        jpa.disponibleMl = botella.getVolumenDisponible().mililitros();
        Fragancia fragancia = botella.getFragancia();
        FraganciaJpa f = new FraganciaJpa();
        f.id = fragancia.getId(); f.nombre = fragancia.getNombre();
        f.familia = fragancia.getFamiliaOlfativa(); f.concentracion = fragancia.getConcentracion();
        f.activa = fragancia.isActiva(); f.publicada = fragancia.isPublicada();
        if (fragancia.getPiramideOlfativa() != null) {
            f.salida = fragancia.getPiramideOlfativa().notasSalida();
            f.corazon = fragancia.getPiramideOlfativa().notasCorazon();
            f.fondo = fragancia.getPiramideOlfativa().notasFondo();
        }
        jpa.fragancia = f;
        botella.getDecants().forEach(d -> jpa.decants.put(d.getId(), d.getVolumen().mililitros()));
        return jpa;
    }

    public static Fragancia aDominio(FraganciaJpa f) {
        Fragancia fragancia = Fragancia.crear(f.id, f.nombre, f.familia, f.concentracion,
                f.salida == null ? null : PiramideOlfativa.crear(f.salida, f.corazon, f.fondo));
        if (f.publicada) fragancia.publicar();
        if (!f.activa) fragancia.desactivar();
        return fragancia;
    }

    public static BotellaMadre aDominio(BotellaJpa jpa) {
        return BotellaMadre.reconstituir(jpa.id, aDominio(jpa.fragancia), jpa.originalMl, jpa.disponibleMl,
                jpa.decants.entrySet().stream()
                        .map(d -> new BotellaMadre.DecantGuardado(d.getKey(), d.getValue())).toList());
    }
}
