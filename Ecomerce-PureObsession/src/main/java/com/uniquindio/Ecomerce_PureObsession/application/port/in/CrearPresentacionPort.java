package com.uniquindio.Ecomerce_PureObsession.application.port.in;

import com.uniquindio.Ecomerce_PureObsession.application.port.in.model.CrearPresentacionCommand;
import com.uniquindio.Ecomerce_PureObsession.domain.entity.Presentacion;

public interface CrearPresentacionPort {
    Presentacion ejecutar(String actor, CrearPresentacionCommand comando);
}
