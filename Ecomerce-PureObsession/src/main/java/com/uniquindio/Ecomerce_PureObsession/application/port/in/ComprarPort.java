package com.uniquindio.Ecomerce_PureObsession.application.port.in;

import com.uniquindio.Ecomerce_PureObsession.application.dto.request.CrearPedidoRequest;
import com.uniquindio.Ecomerce_PureObsession.application.dto.response.PedidoResponse;

public interface ComprarPort {
    PedidoResponse ejecutar(String emailAutenticado, CrearPedidoRequest solicitud);
}
