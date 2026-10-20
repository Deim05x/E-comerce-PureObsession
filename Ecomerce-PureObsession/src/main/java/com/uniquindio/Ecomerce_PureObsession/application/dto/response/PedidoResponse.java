package com.uniquindio.Ecomerce_PureObsession.application.dto.response;

import com.uniquindio.Ecomerce_PureObsession.domain.entity.*;
import com.uniquindio.Ecomerce_PureObsession.domain.valueObject.*;
import java.util.*;

public record PedidoResponse(UUID id, UUID clienteId, DatosEntrega entrega, EstadoPedido estado,
                             Double total, String moneda, List<Linea> lineas, String claveConsulta) {
    public record Linea(UUID presentacionId, UUID fraganciaId, UUID vendedorId, String descripcion,
                        int cantidad, Double precioUnitario) {}
    public static PedidoResponse desde(Pedido pedido, String claveConsulta) {
        return new PedidoResponse(pedido.getId(), pedido.getClienteId(), pedido.getEntrega(), pedido.getEstado(),
                pedido.getTotal().precio(), pedido.getTotal().moneda(), pedido.getLineas().stream()
                .map(linea -> new Linea(linea.getItemId(), linea.getFraganciaId(), linea.getVendedorId(),
                        linea.getDescripcionFragancia(), linea.getCantidad(), linea.getPrecioUnitario().precio()))
                .toList(), claveConsulta);
    }
}
