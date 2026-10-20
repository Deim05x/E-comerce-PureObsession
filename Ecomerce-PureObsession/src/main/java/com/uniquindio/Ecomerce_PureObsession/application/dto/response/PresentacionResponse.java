package com.uniquindio.Ecomerce_PureObsession.application.dto.response;
import com.uniquindio.Ecomerce_PureObsession.domain.entity.Presentacion;
import com.uniquindio.Ecomerce_PureObsession.domain.valueObject.*;
import java.util.UUID;
import java.math.BigDecimal;
public record PresentacionResponse(UUID id,UUID fraganciaId,UUID vendedorId,TipoPresentacion tipo,
    int volumenMl,BigDecimal precio,String moneda,OrigenProducto origen,String paisOrigen,
    boolean original,boolean publicada) {
    public static PresentacionResponse desde(Presentacion p) {
        return new PresentacionResponse(p.getId(),p.getFraganciaId(),p.getVendedorId(),p.getTipo(),
            p.getVolumenMl(),p.getPrecio(),"COP",p.getOrigen(),p.getPaisOrigen(),p.isOriginal(),p.isPublicada());
    }
}

