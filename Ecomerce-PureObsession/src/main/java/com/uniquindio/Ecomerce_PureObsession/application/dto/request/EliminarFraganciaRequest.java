package com.uniquindio.Ecomerce_PureObsession.application.dto.request;
import jakarta.validation.constraints.*;
import java.util.UUID;
public record EliminarFraganciaRequest(@NotNull UUID fraganciaId) {}
