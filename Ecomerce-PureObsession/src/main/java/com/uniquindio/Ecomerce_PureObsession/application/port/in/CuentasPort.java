package com.uniquindio.Ecomerce_PureObsession.application.port.in;
import com.uniquindio.Ecomerce_PureObsession.application.dto.request.RegistrarCuentaRequest;
import com.uniquindio.Ecomerce_PureObsession.application.dto.response.CuentaResponse;
public interface CuentasPort {
    CuentaResponse registrar(RegistrarCuentaRequest request);
    CuentaResponse consultar(String email);
}

