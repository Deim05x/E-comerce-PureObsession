package com.uniquindio.Ecomerce_PureObsession.application.dto.request;

import com.uniquindio.Ecomerce_PureObsession.domain.valueObject.Concentracion;
import com.uniquindio.Ecomerce_PureObsession.domain.valueObject.FamiliaOlfativa;

public record RegistrarBotellaMadreRequest(
        String nombreFragancia,
        FamiliaOlfativa familiaOlfativa,
        Concentracion concentracion,
        String notasSalida,
        String notasCorazon,
        String notasFondo,
        int volumenMl
) {
}
