package com.uniquindio.Ecomerce_PureObsession.application.dto.request;
import jakarta.validation.constraints.*;
import com.uniquindio.Ecomerce_PureObsession.domain.valueObject.*;
import java.util.UUID;
import java.math.BigDecimal;
public record CrearPresentacionRequest(
    @NotNull UUID fraganciaId, UUID botellaMadreId,
    @NotNull TipoPresentacion tipo,
    @NotNull @Positive Integer volumenMl,
    @NotNull @DecimalMin("0.01") @Digits(integer=12,fraction=2) BigDecimal precio,
    @NotNull OrigenProducto origen,
    @NotBlank @Pattern(regexp="(?i)[a-z]{2}") String paisOrigen,
    @NotNull Boolean original) {}

