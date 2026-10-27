package com.uniquindio.Ecomerce_PureObsession.application.usecase;

import com.uniquindio.Ecomerce_PureObsession.application.port.in.ConsultarPresentacionesPort;
import com.uniquindio.Ecomerce_PureObsession.application.port.out.PresentacionRepository;
import com.uniquindio.Ecomerce_PureObsession.domain.entity.BotellaMadre;
import com.uniquindio.Ecomerce_PureObsession.domain.entity.Presentacion;
import com.uniquindio.Ecomerce_PureObsession.domain.exception.RecursoNoEncontradoException;
import com.uniquindio.Ecomerce_PureObsession.domain.exception.ReglaDominioException;
import com.uniquindio.Ecomerce_PureObsession.domain.repository.BotellaMadreRepository;
import com.uniquindio.Ecomerce_PureObsession.domain.valueObject.TipoEmpaque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public final class ConsultarPresentacionesUseCase implements ConsultarPresentacionesPort {
    private final PresentacionRepository presentaciones;
    private final BotellaMadreRepository botellas;

    public ConsultarPresentacionesUseCase(PresentacionRepository presentaciones,
            BotellaMadreRepository botellas) {
        this.presentaciones = presentaciones;
        this.botellas = botellas;
    }

    @Override
    public List<Presentacion> listarCatalogo() {
        Set<UUID> fraganciasDisponibles = new HashSet<>();
        botellas.listarTodas().stream()
                .map(BotellaMadre::getFragancia)
                .filter(f -> f.isActiva() && f.isPublicada())
                .forEach(f -> fraganciasDisponibles.add(f.getId()));
        return presentaciones.listarPublicadas().stream()
                .filter(p -> fraganciasDisponibles.contains(p.getFraganciaId()))
                .toList();
    }

    @Override
    public void validarEmpaque(UUID presentacionId, TipoEmpaque tipoEmpaque) {
        Presentacion presentacion = presentaciones.buscarPorId(presentacionId)
                .orElseThrow(() -> new RecursoNoEncontradoException("La presentación no existe."));
        if (listarCatalogo().stream().noneMatch(p -> p.getId().equals(presentacionId))) {
            throw new ReglaDominioException("La presentación no está disponible en el catálogo.");
        }
        presentacion.getTipo().validarEmpaque(tipoEmpaque);
    }
}
