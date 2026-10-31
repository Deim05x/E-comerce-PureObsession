package com.uniquindio.Ecomerce_PureObsession.application.port.in;

import com.uniquindio.Ecomerce_PureObsession.domain.entity.*;
import com.uniquindio.Ecomerce_PureObsession.domain.repository.Pagina;
import com.uniquindio.Ecomerce_PureObsession.domain.valueObject.FamiliaOlfativa;

public interface ConsultarCatalogoPaginadoPort {
    Pagina<Presentacion> presentaciones(String texto, FamiliaOlfativa familia, int pagina, int tamanio);
    Pagina<Fragancia> fragancias(String texto, FamiliaOlfativa familia, int pagina, int tamanio);
}
