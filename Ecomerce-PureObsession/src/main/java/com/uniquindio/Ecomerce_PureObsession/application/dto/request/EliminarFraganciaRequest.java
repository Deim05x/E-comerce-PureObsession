package com.uniquindio.Ecomerce_PureObsession.application.dto.request;
import jakarta.validation.constraints.*;
import java.util.UUID;
import com.uniquindio.Ecomerce_PureObsession.domain.valueObject.*;
public record EliminarFraganciaRequest(@NotNull UUID fraganciaId) {}
