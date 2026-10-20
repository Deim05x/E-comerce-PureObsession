package com.uniquindio.Ecomerce_PureObsession.application.dto.response;
import com.uniquindio.Ecomerce_PureObsession.domain.entity.Cuenta;
import com.uniquindio.Ecomerce_PureObsession.domain.valueObject.Rol;
import java.util.UUID;
public record CuentaResponse(UUID id,String nombre,String email,Rol rol) {
    public static CuentaResponse desde(Cuenta c) {return new CuentaResponse(c.getId(),c.getNombre(),c.getEmail(),c.getRol());}
}

