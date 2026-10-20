package com.uniquindio.Ecomerce_PureObsession.application.port.in;
import com.uniquindio.Ecomerce_PureObsession.application.dto.request.RegistrarBotellaMadreRequest;
import com.uniquindio.Ecomerce_PureObsession.application.dto.response.*;
public interface RegistrarBotellaMadrePort {
    BotellaMadreResponse ejecutar(RegistrarBotellaMadreRequest request);
}
