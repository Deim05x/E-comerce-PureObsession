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
        List<BotellaMadre> asociadas = botellas.listarTodas().stream()
                .filter(b -> b.getFragancia().getId().equals(presentacion.getFraganciaId()))
                .toList();
        if (asociadas.isEmpty()) {
            throw new ReglaDominioException("La fragancia no existe.");
        }

        boolean certificadoAprobado = certificados.buscar(presentacionId)
                .map(c -> c.estado() == EstadoCertificado.APROBADO)
                .orElse(false);
        presentacion.publicar(certificadoAprobado);
        for (BotellaMadre botella : asociadas) {
            botella.getFragancia().publicar();
            botellas.guardar(botella);
        }
        return presentaciones.guardar(presentacion);
    }
}
