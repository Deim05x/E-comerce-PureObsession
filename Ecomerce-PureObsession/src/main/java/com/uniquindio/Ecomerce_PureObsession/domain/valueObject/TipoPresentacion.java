package com.uniquindio.Ecomerce_PureObsession.domain.valueObject;
public enum TipoPresentacion {
    PERFUME, DECANT, TESTER;
    public void validarEmpaque(TipoEmpaque empaque) {
        if(empaque == null)
            throw new com.uniquindio.Ecomerce_PureObsession.domain.exception.ReglaDominioException("El tipo de empaque es obligatorio.");
        if(this == TESTER && empaque == TipoEmpaque.REGALO)
            throw new com.uniquindio.Ecomerce_PureObsession.domain.exception.ReglaDominioException("Los testers no admiten empaque de regalo.");
    }
}

