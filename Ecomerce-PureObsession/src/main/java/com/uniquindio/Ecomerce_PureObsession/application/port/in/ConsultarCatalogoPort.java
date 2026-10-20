package com.uniquindio.Ecomerce_PureObsession.application.port.in;
import com.uniquindio.Ecomerce_PureObsession.application.dto.request.ConsultarCatalogoRequest;
import com.uniquindio.Ecomerce_PureObsession.application.dto.response.*;
public interface ConsultarCatalogoPort {
    java.util.List<FraganciaCatalogoResponse> ejecutar(ConsultarCatalogoRequest request);
}
