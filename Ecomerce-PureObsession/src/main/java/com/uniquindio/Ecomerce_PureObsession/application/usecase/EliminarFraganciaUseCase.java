package com.uniquindio.Ecomerce_PureObsession.application.usecase;

import com.uniquindio.Ecomerce_PureObsession.application.dto.request.EliminarFraganciaRequest;
import com.uniquindio.Ecomerce_PureObsession.application.dto.response.EliminarFraganciaResponse;
import com.uniquindio.Ecomerce_PureObsession.domain.entity.BotellaMadre;
import com.uniquindio.Ecomerce_PureObsession.domain.exception.ReglaDominioException;
import com.uniquindio.Ecomerce_PureObsession.domain.repository.BotellaMadreRepository;
import com.uniquindio.Ecomerce_PureObsession.domain.repository.PedidoRepository;
import java.util.List;
import java.util.Objects;

/** La política está en Fragancia; aquí se reúnen los datos y se persiste el resultado. */
public class EliminarFraganciaUseCase {
    private final BotellaMadreRepository botellas;
    private final PedidoRepository pedidos;
    public EliminarFraganciaUseCase(BotellaMadreRepository botellas, PedidoRepository pedidos) {
        this.botellas = Objects.requireNonNull(botellas);
        this.pedidos = Objects.requireNonNull(pedidos);
    }
    public EliminarFraganciaResponse ejecutar(EliminarFraganciaRequest request) {
        if (request == null || request.fraganciaId() == null) {
            throw new ReglaDominioException("La fragancia es obligatoria.");
        }
        List<BotellaMadre> encontradas = botellas.listarTodas().stream()
                .filter(b -> b.getFragancia().getId().equals(request.fraganciaId())).toList();
        if (encontradas.isEmpty()) throw new ReglaDominioException("La fragancia no existe.");
        boolean activaEnPedidos = pedidos.existePedidoActivoConFragancia(request.fraganciaId());
        // Validar todas antes de modificar: un rechazo conserva el inventario.
        encontradas.forEach(b -> b.getFragancia().validarEliminacion(activaEnPedidos));
        encontradas.forEach(b -> botellas.eliminar(b.getId()));
        return new EliminarFraganciaResponse(request.fraganciaId(), encontradas.size());
    }
}
