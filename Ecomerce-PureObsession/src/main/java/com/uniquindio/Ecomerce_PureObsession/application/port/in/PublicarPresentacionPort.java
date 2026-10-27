package com.uniquindio.Ecomerce_PureObsession.application.port.in;

import com.uniquindio.Ecomerce_PureObsession.domain.entity.Presentacion;
import java.util.UUID;

public interface PublicarPresentacionPort {
    Presentacion ejecutar(String actor, UUID presentacionId);
}
