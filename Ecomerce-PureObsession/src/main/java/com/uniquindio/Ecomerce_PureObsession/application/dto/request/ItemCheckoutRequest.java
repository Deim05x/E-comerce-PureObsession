package com.uniquindio.Ecomerce_PureObsession.application.dto.request;

import jakarta.validation.constraints.*;
import java.util.UUID;

public record ItemCheckoutRequest(@NotNull UUID presentacionId, @Min(1) @Max(1000) int cantidad,
        com.uniquindio.Ecomerce_PureObsession.domain.valueObject.TipoEmpaque empaque) {
    public ItemCheckoutRequest(UUID presentacionId,int cantidad) {this(presentacionId,cantidad,com.uniquindio.Ecomerce_PureObsession.domain.valueObject.TipoEmpaque.ESTANDAR);}
    public ItemCheckoutRequest {if(empaque==null)empaque=com.uniquindio.Ecomerce_PureObsession.domain.valueObject.TipoEmpaque.ESTANDAR;}
}
