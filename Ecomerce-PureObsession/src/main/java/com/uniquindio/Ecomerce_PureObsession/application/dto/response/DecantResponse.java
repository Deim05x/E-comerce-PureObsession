package com.uniquindio.Ecomerce_PureObsession.application.dto.response;
import java.util.UUID;
import com.uniquindio.Ecomerce_PureObsession.domain.valueObject.Concentracion;
public record DecantResponse(UUID decantId, UUID botellaMadreId, UUID fraganciaId,
        String nombreFragancia, Concentracion concentracion, int volumenMl, int volumenDisponibleMl) {}
