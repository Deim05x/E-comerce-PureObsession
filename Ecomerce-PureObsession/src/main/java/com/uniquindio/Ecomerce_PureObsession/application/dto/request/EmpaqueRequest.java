package com.uniquindio.Ecomerce_PureObsession.application.dto.request;
import com.uniquindio.Ecomerce_PureObsession.domain.valueObject.TipoEmpaque;
import jakarta.validation.constraints.NotNull;
public record EmpaqueRequest(@NotNull TipoEmpaque tipo) {}

