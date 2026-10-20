package com.uniquindio.Ecomerce_PureObsession.application.port.in;

import com.uniquindio.Ecomerce_PureObsession.application.dto.response.PedidoResponse;
import java.util.UUID;

public interface CancelarPedidoPort {
    PedidoResponse ejecutar(UUID pedidoId, String emailAutenticado, String claveInvitado);
}
