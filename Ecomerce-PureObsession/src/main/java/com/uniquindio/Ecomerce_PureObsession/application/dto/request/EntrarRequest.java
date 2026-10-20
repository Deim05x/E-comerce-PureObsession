package com.uniquindio.Ecomerce_PureObsession.application.dto.request;
import jakarta.validation.constraints.*;
public record EntrarRequest(@NotBlank @Email String email, @NotBlank String clave) {}

