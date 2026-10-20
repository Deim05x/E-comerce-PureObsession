package com.uniquindio.Ecomerce_PureObsession.application.dto.response;
import com.uniquindio.Ecomerce_PureObsession.domain.valueObject.TipoEmpaque;
import java.util.UUID;
public record EmpaqueResponse(UUID presentacionId,TipoEmpaque tipo,boolean permitido) {}

