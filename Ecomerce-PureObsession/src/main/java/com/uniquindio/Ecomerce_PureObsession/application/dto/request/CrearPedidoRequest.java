package com.uniquindio.Ecomerce_PureObsession.application.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.List;

public record CrearPedidoRequest(String nombre, String correo, @NotBlank String telefono,
                                 @NotBlank String direccion,
                                 @NotEmpty List<@Valid ItemCheckoutRequest> items) {}
