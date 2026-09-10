package com.uniquindio.Ecomerce_PureObsession.domain.valueObject;

import com.uniquindio.Ecomerce_PureObsession.domain.exception.ReglaDominioException;

public record Precio(Double precio, String moneda) {
    public Precio{if (precio < 0){
        throw new ReglaDominioException("el precio no puede ser negativo");
    }}
}
