package com.uniquindio.Ecomerce_PureObsession.infrastructure.web.mapper;

import com.uniquindio.Ecomerce_PureObsession.application.dto.request.CrearPresentacionRequest;
import com.uniquindio.Ecomerce_PureObsession.application.dto.response.PresentacionResponse;
import com.uniquindio.Ecomerce_PureObsession.application.port.in.model.CrearPresentacionCommand;
import com.uniquindio.Ecomerce_PureObsession.domain.entity.Presentacion;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public final class PresentacionWebMapper {
    public CrearPresentacionCommand aComando(CrearPresentacionRequest request) {
        return new CrearPresentacionCommand(
                request.fraganciaId(), request.botellaMadreId(), request.tipo(), request.volumenMl(),
                request.precio(), request.origen(), request.paisOrigen(), request.original());
    }

    public PresentacionResponse aRespuesta(Presentacion presentacion) {
        return new PresentacionResponse(
                presentacion.getId(), presentacion.getFraganciaId(), presentacion.getVendedorId(),
                presentacion.getTipo(), presentacion.getVolumenMl(), presentacion.getPrecio(), "COP",
                presentacion.getOrigen(), presentacion.getPaisOrigen(), presentacion.isOriginal(),
                presentacion.isPublicada());
    }

    public List<PresentacionResponse> aRespuestas(List<Presentacion> presentaciones) {
        return presentaciones.stream().map(this::aRespuesta).toList();
    }
}
