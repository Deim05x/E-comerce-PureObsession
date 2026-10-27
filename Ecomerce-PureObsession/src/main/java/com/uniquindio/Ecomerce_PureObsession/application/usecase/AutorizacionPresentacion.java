package com.uniquindio.Ecomerce_PureObsession.application.usecase;

import com.uniquindio.Ecomerce_PureObsession.application.port.out.CuentaRepository;
import com.uniquindio.Ecomerce_PureObsession.application.port.out.PresentacionRepository;
import com.uniquindio.Ecomerce_PureObsession.domain.entity.Cuenta;
import com.uniquindio.Ecomerce_PureObsession.domain.entity.Presentacion;
import com.uniquindio.Ecomerce_PureObsession.domain.exception.RecursoNoEncontradoException;
import com.uniquindio.Ecomerce_PureObsession.domain.exception.ReglaDominioException;
import com.uniquindio.Ecomerce_PureObsession.domain.valueObject.Rol;
import java.util.UUID;

final class AutorizacionPresentacion {
    private AutorizacionPresentacion() {
    }

    static Cuenta cuenta(String actor, CuentaRepository cuentas) {
        if (actor == null || actor.isBlank()) {
            throw new ReglaDominioException("Inicie sesión.");
        }
        return cuentas.buscarPorEmail(actor)
                .orElseThrow(() -> new ReglaDominioException("La cuenta no existe."));
    }

    static Cuenta vendedor(String actor, CuentaRepository cuentas) {
        Cuenta cuenta = cuenta(actor, cuentas);
        if (cuenta.getRol() != Rol.VENDEDOR) {
            throw new ReglaDominioException("Solo los vendedores administran presentaciones.");
        }
        return cuenta;
    }

    static Presentacion propia(String actor, UUID id, CuentaRepository cuentas,
            PresentacionRepository presentaciones) {
        Cuenta vendedor = vendedor(actor, cuentas);
        Presentacion presentacion = presentaciones.buscarPorId(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("La presentación no existe."));
        if (!presentacion.getVendedorId().equals(vendedor.getId())) {
            throw new ReglaDominioException("La presentación no pertenece al vendedor.");
        }
        return presentacion;
    }
}
