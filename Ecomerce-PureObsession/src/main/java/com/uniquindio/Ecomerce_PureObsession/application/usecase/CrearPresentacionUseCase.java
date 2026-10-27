package com.uniquindio.Ecomerce_PureObsession.application.usecase;

import com.uniquindio.Ecomerce_PureObsession.application.port.in.CrearPresentacionPort;
import com.uniquindio.Ecomerce_PureObsession.application.port.in.model.CrearPresentacionCommand;
import com.uniquindio.Ecomerce_PureObsession.application.port.out.CuentaRepository;
import com.uniquindio.Ecomerce_PureObsession.application.port.out.PresentacionRepository;
import com.uniquindio.Ecomerce_PureObsession.domain.entity.BotellaMadre;
import com.uniquindio.Ecomerce_PureObsession.domain.entity.Cuenta;
import com.uniquindio.Ecomerce_PureObsession.domain.entity.Presentacion;
import com.uniquindio.Ecomerce_PureObsession.domain.exception.ReglaDominioException;
import com.uniquindio.Ecomerce_PureObsession.domain.repository.BotellaMadreRepository;
import com.uniquindio.Ecomerce_PureObsession.domain.service.PoliticaCreacionPresentacion;
import com.uniquindio.Ecomerce_PureObsession.domain.valueObject.TipoPresentacion;
import java.util.List;
import java.util.UUID;

public final class CrearPresentacionUseCase implements CrearPresentacionPort {
    private final PresentacionRepository presentaciones;
    private final CuentaRepository cuentas;
    private final BotellaMadreRepository botellas;

    public CrearPresentacionUseCase(PresentacionRepository presentaciones, CuentaRepository cuentas,
            BotellaMadreRepository botellas) {
        this.presentaciones = presentaciones;
        this.cuentas = cuentas;
        this.botellas = botellas;
    }

    @Override
    public Presentacion ejecutar(String actor, CrearPresentacionCommand comando) {
        Cuenta vendedor = AutorizacionPresentacion.vendedor(actor, cuentas);
        validarSolicitud(comando);

        List<BotellaMadre> inventario = botellas.listarTodas();
        boolean fraganciaActiva = inventario.stream()
                .map(BotellaMadre::getFragancia)
                .anyMatch(f -> f.getId().equals(comando.fraganciaId()) && f.isActiva());
        if (!fraganciaActiva) {
            throw new ReglaDominioException("La fragancia debe existir y estar activa.");
        }

        if (comando.tipo() == TipoPresentacion.DECANT) {
            BotellaMadre botella = botellas.buscarPorId(comando.botellaMadreId())
                    .orElseThrow(() -> new ReglaDominioException("La botella madre no existe."));
            PoliticaCreacionPresentacion.validarDecant(
                    comando.fraganciaId(), comando.volumenMl(), botella);
        }

        Presentacion nueva = Presentacion.crear(
                UUID.randomUUID(), vendedor.getId(), comando.fraganciaId(), comando.botellaMadreId(),
                comando.tipo(), comando.volumenMl(), comando.precio(), comando.origen(),
                comando.paisOrigen(), comando.original());
        return presentaciones.guardar(nueva);
    }

    private void validarSolicitud(CrearPresentacionCommand comando) {
        if (comando == null || comando.fraganciaId() == null || comando.tipo() == null
                || comando.volumenMl() == null || comando.precio() == null
                || comando.origen() == null || comando.paisOrigen() == null
                || comando.original() == null) {
            throw new ReglaDominioException("La solicitud está incompleta.");
        }
        if (comando.tipo() == TipoPresentacion.DECANT && comando.botellaMadreId() == null) {
            throw new ReglaDominioException("El decant requiere botella madre.");
        }
    }
}
