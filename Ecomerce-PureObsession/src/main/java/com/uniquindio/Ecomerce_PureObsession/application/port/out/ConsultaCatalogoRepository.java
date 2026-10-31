package com.uniquindio.Ecomerce_PureObsession.application.port.out;

import com.uniquindio.Ecomerce_PureObsession.domain.entity.*;
import com.uniquindio.Ecomerce_PureObsession.domain.repository.*;
import com.uniquindio.Ecomerce_PureObsession.domain.valueObject.FamiliaOlfativa;
import java.util.List;

public interface ConsultaCatalogoRepository {
    Pagina<Presentacion> buscarPresentaciones(String texto, FamiliaOlfativa familia, int pagina, int tamanio);
    Pagina<Fragancia> buscarFragancias(String texto, FamiliaOlfativa familia, int pagina, int tamanio);
    List<RankingFragancia> ranking(int limite);
}
