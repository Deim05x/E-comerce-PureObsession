package com.uniquindio.Ecomerce_PureObsession.application.dto.request;
import jakarta.validation.constraints.*;
import java.util.UUID;
public record CrearDecantRequest(@NotNull UUID botellaMadreId, @NotNull @Positive Integer volumenMl) {}
