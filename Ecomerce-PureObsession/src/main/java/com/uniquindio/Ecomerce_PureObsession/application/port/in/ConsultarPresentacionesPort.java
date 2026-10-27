package com.uniquindio.Ecomerce_PureObsession.application.port.in;

import com.uniquindio.Ecomerce_PureObsession.domain.entity.Presentacion;
import com.uniquindio.Ecomerce_PureObsession.domain.valueObject.TipoEmpaque;
import java.util.List;
import java.util.UUID;

public interface ConsultarPresentacionesPort {
    List<Presentacion> listarCatalogo();
    void validarEmpaque(UUID presentacionId, TipoEmpaque tipoEmpaque);
}
