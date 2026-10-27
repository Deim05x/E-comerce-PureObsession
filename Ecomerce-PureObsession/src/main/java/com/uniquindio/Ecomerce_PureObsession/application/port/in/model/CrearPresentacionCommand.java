package com.uniquindio.Ecomerce_PureObsession.application.port.in.model;

import com.uniquindio.Ecomerce_PureObsession.domain.valueObject.OrigenProducto;
import com.uniquindio.Ecomerce_PureObsession.domain.valueObject.TipoPresentacion;
import java.math.BigDecimal;
import java.util.UUID;

public record CrearPresentacionCommand(
        UUID fraganciaId,
        UUID botellaMadreId,
        TipoPresentacion tipo,
        Integer volumenMl,
        BigDecimal precio,
        OrigenProducto origen,
        String paisOrigen,
        Boolean original) {
}