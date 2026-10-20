package com.uniquindio.Ecomerce_PureObsession.application.port.in;
import com.uniquindio.Ecomerce_PureObsession.application.dto.request.EliminarFraganciaRequest;
import com.uniquindio.Ecomerce_PureObsession.application.dto.response.*;
public interface EliminarFraganciaPort {
    EliminarFraganciaResponse ejecutar(EliminarFraganciaRequest request);
}
