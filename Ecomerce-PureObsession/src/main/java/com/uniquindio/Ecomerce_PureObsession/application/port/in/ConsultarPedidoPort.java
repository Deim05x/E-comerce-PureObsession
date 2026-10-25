package com.uniquindio.Ecomerce_PureObsession.application.port.in;

import com.uniquindio.Ecomerce_PureObsession.application.dto.response.PedidoResponse;
import java.util.List;
import java.util.UUID;

public interface ConsultarPedidoPort {
    PedidoResponse consultar(UUID pedidoId, String emailAutenticado, String claveInvitado);
    List<PedidoResponse> listarMios(String emailAutenticado);
}
