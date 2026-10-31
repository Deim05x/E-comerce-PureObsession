package com.uniquindio.Ecomerce_PureObsession.application.usecase;

import com.uniquindio.Ecomerce_PureObsession.application.port.in.ConsultarCatalogoPaginadoPort;
import com.uniquindio.Ecomerce_PureObsession.application.port.out.ConsultaCatalogoRepository;
import com.uniquindio.Ecomerce_PureObsession.domain.entity.*;
import com.uniquindio.Ecomerce_PureObsession.domain.repository.Pagina;
import com.uniquindio.Ecomerce_PureObsession.domain.valueObject.FamiliaOlfativa;

public final class ConsultarCatalogoPaginadoUseCase implements ConsultarCatalogoPaginadoPort {
    private final ConsultaCatalogoRepository consultas;
    public ConsultarCatalogoPaginadoUseCase(ConsultaCatalogoRepository consultas) { this.consultas=consultas; }
    public Pagina<Presentacion> presentaciones(String texto, FamiliaOlfativa familia, int pagina, int tamanio) {
        Pagina.validar(pagina,tamanio);
        return consultas.buscarPresentaciones(texto,familia,pagina,tamanio);
    }
    public Pagina<Fragancia> fragancias(String texto, FamiliaOlfativa familia, int pagina, int tamanio) {
        Pagina.validar(pagina,tamanio);
        return consultas.buscarFragancias(texto,familia,pagina,tamanio);
    }
}
