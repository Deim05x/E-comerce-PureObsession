package com.uniquindio.Ecomerce_PureObsession.application.dto.response;

import com.uniquindio.Ecomerce_PureObsession.domain.valueObject.Concentracion;
import com.uniquindio.Ecomerce_PureObsession.domain.valueObject.FamiliaOlfativa;

import java.util.UUID;

public record BotellaMadreResponse(
        UUID botellaMadreId,
        String nombreFragancia,
        FamiliaOlfativa familiaOlfativa,
        Concentracion concentracion,
        int volumenDisponibleMl
) {
}