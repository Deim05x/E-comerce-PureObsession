package com.uniquindio.Ecomerce_PureObsession.application.usecase;

import com.uniquindio.Ecomerce_PureObsession.application.port.in.PublicarPresentacionPort;
import com.uniquindio.Ecomerce_PureObsession.application.port.out.CertificadoRepository;
import com.uniquindio.Ecomerce_PureObsession.application.port.out.CuentaRepository;
import com.uniquindio.Ecomerce_PureObsession.application.port.out.PresentacionRepository;
import com.uniquindio.Ecomerce_PureObsession.domain.entity.BotellaMadre;
import com.uniquindio.Ecomerce_PureObsession.domain.entity.Presentacion;
import com.uniquindio.Ecomerce_PureObsession.domain.exception.ReglaDominioException;
import com.uniquindio.Ecomerce_PureObsession.domain.repository.BotellaMadreRepository;
import com.uniquindio.Ecomerce_PureObsession.domain.valueObject.EstadoCertificado;
import java.util.List;
import java.util.UUID;

public final class PublicarPresentacionUseCase implements PublicarPresentacionPort {
    private final PresentacionRepository presentaciones;
    private final CuentaRepository cuentas;
    private final CertificadoRepository certificados;
    private final BotellaMadreRepository botellas;

    public PublicarPresentacionUseCase(PresentacionRepository presentaciones, CuentaRepository cuentas,
            CertificadoRepository certificados, BotellaMadreRepository botellas) {
        this.presentaciones = presentaciones;
        this.cuentas = cuentas;
        this.certificados = certificados;
        this.botellas = botellas;
    }

    @Override
    public Presentacion ejecutar(String actor, UUID presentacionId) {
        Presentacion presentacion = AutorizacionPresentacion.propia(
                actor, presentacionId, cuentas, presentaciones);
        presentaciones.validarPublicacion(presentacion);

        boolean certificadoAprobado = certificados.buscar(presentacionId)
                .map(c -> c.estado() == EstadoCertificado.APROBADO)
                .orElse(false);
        presentacion.publicar(certificadoAprobado);
        // El adaptador actualiza la fragancia; no se reescriben snapshots de inventario.
        return presentaciones.guardar(presentacion);
    }
}
