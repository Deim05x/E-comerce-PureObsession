package com.uniquindio.Ecomerce_PureObsession.application.dto.request;
import jakarta.validation.constraints.*;
import com.uniquindio.Ecomerce_PureObsession.domain.valueObject.Rol;
public record RegistrarCuentaRequest(
    @NotBlank @Size(max=120) String nombre,
    @NotBlank @Pattern(regexp="[0-9]{5,15}") String cedula,
    @NotBlank @Email @Size(max=254) String email,
    @NotBlank @Size(min=10,max=72) String clave,
    @NotNull Rol rol) {}

