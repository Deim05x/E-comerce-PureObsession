package com.uniquindio.Ecomerce_PureObsession.application.dto.request;
import jakarta.validation.constraints.*;
import java.util.UUID;
import com.uniquindio.Ecomerce_PureObsession.domain.valueObject.*;
public record RegistrarBotellaMadreRequest(@NotBlank String nombreFragancia,
        @NotNull FamiliaOlfativa familiaOlfativa,
        Concentracion concentracion,
        @NotBlank String notasSalida,
        @NotBlank String notasCorazon,
        @NotBlank String notasFondo,
        @NotNull @Positive Integer volumenMl) {}
