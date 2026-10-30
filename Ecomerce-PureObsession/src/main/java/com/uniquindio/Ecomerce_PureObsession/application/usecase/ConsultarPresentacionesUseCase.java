package com.uniquindio.Ecomerce_PureObsession.application.usecase;

import com.uniquindio.Ecomerce_PureObsession.application.port.in.ConsultarPresentacionesPort;
import com.uniquindio.Ecomerce_PureObsession.application.port.out.PresentacionRepository;
import com.uniquindio.Ecomerce_PureObsession.domain.entity.Presentacion;
import com.uniquindio.Ecomerce_PureObsession.domain.exception.RecursoNoEncontradoException;
import com.uniquindio.Ecomerce_PureObsession.domain.exception.ReglaDominioException;
import com.uniquindio.Ecomerce_PureObsession.domain.valueObject.TipoEmpaque;
import java.util.List;
import java.util.UUID;

public final class ConsultarPresentacionesUseCase implements ConsultarPresentacionesPort {
    private final PresentacionRepository presentaciones;

    public ConsultarPresentacionesUseCase(PresentacionRepository presentaciones) {
        this.presentaciones = presentaciones;
    }

    @Override
    public List<Presentacion> listarCatalogo() {
        return presentaciones.listarPublicadas();
    }

    @Override
    public void validarEmpaque(UUID presentacionId, TipoEmpaque tipoEmpaque) {
        Presentacion presentacion = presentaciones.buscarPorId(presentacionId)
                .orElseThrow(() -> new RecursoNoEncontradoException("La presentación no existe."));
        if (!presentaciones.estaEnCatalogo(presentacionId)) {
            throw new ReglaDominioException("La presentación no está disponible en el catálogo.");
        }
        presentacion.getTipo().validarEmpaque(tipoEmpaque);
    }
}
