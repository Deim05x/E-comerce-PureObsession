package com.uniquindio.Ecomerce_PureObsession.application.dto.request;

import jakarta.validation.constraints.*;
import java.util.UUID;

public record ItemCheckoutRequest(@NotNull UUID presentacionId, @Min(1) @Max(1000) int cantidad) {}
