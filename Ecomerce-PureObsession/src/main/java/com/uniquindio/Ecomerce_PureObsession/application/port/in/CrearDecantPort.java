package com.uniquindio.Ecomerce_PureObsession.application.port.in;
import com.uniquindio.Ecomerce_PureObsession.application.dto.request.CrearDecantRequest;
import com.uniquindio.Ecomerce_PureObsession.application.dto.response.*;
public interface CrearDecantPort {
    DecantResponse ejecutar(CrearDecantRequest request);
}
