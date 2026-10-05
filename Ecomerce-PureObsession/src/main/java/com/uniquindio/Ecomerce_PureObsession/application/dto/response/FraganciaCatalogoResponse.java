package com.uniquindio.Ecomerce_PureObsession.application.dto.response;

import com.uniquindio.Ecomerce_PureObsession.domain.valueObject.Concentracion;
import com.uniquindio.Ecomerce_PureObsession.domain.valueObject.FamiliaOlfativa;

import java.util.UUID;

public record FraganciaCatalogoResponse(
        UUID fraganciaId,
        String nombre,
        FamiliaOlfativa familiaOlfativa,
        Concentracion concentracion) {
}
