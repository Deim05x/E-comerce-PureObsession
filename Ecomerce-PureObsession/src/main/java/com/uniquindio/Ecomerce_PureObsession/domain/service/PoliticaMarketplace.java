package com.uniquindio.Ecomerce_PureObsession.domain.service;

import com.uniquindio.Ecomerce_PureObsession.domain.exception.ReglaDominioException;
import java.util.UUID;

public final class PoliticaMarketplace {
    private PoliticaMarketplace() {}
    public static void propietario(UUID propietario,UUID actor) {
        if(propietario==null || !propietario.equals(actor)) throw new com.uniquindio.Ecomerce_PureObsession.domain.exception.AccesoProhibidoException("El recurso no pertenece al vendedor.");
    }
    public static int reservar(int disponible,int solicitado) {
        if(solicitado<=0 || solicitado>disponible) throw new ReglaDominioException("Stock insuficiente para la cantidad solicitada.");
        return disponible-solicitado;
    }
    public static int liberar(int reservado,int cantidad) {
        if(cantidad<=0 || cantidad>reservado) throw new ReglaDominioException("La liberacion supera la reserva.");
        return reservado-cantidad;
    }
    public static void stock(int disponible) {
        if(disponible<0 || disponible>1000000) throw new ReglaDominioException("Stock entre 0 y 1000000.");
    }
}
