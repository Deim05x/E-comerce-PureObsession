package com.uniquindio.Ecomerce_PureObsession.domain.entity;

import com.uniquindio.Ecomerce_PureObsession.domain.exception.ReglaDominioException;
import com.uniquindio.Ecomerce_PureObsession.domain.valueObject.Precio;
import java.math.BigDecimal;
import java.util.*;

public class LineaPedido {
    private final UUID id, fraganciaId, itemId, vendedorId;
    private final String descripcionFragrancia;
    private final Precio precioUnitario;
    private final int cantidad;

    private LineaPedido(UUID id, UUID itemId, UUID fraganciaId, UUID vendedorId, String descripcion,
                        Precio precioUnitario, int cantidad) {
        this.id = id; this.itemId = itemId; this.fraganciaId = fraganciaId; this.vendedorId = vendedorId;
        this.descripcionFragrancia = descripcion; this.precioUnitario = precioUnitario; this.cantidad = cantidad;
    }
    /** Fábrica existente, para pedidos históricos. */
    public static LineaPedido crear(UUID id, UUID itemId, UUID fraganciaId, String descripcion,
                                    Precio precioUnitario, int cantidad) {
        return crearInterno(id, itemId, fraganciaId, null, descripcion, precioUnitario, cantidad);
    }
    public static LineaPedido crearConVendedor(UUID id, UUID itemId, UUID fraganciaId, UUID vendedorId,
                                               String descripcion, Precio precioUnitario, int cantidad) {
        if (vendedorId == null) throw new ReglaDominioException("La línea necesita vendedor.");
        return crearInterno(id, itemId, fraganciaId, vendedorId, descripcion, precioUnitario, cantidad);
    }
    private static LineaPedido crearInterno(UUID id, UUID itemId, UUID fraganciaId, UUID vendedorId,
                                            String descripcion, Precio precioUnitario, int cantidad) {
        if (id == null || itemId == null || fraganciaId == null || precioUnitario == null
                || descripcion == null || descripcion.isBlank())
            throw new ReglaDominioException("La línea requiere identidades, descripción y precio.");
        if (cantidad <= 0) throw new ReglaDominioException("La cantidad debe ser mayor que cero.");
        return new LineaPedido(id, itemId, fraganciaId, vendedorId, descripcion, precioUnitario, cantidad);
    }
    public Precio calcularSubtotal() { return Precio.crear(BigDecimal.valueOf(precioUnitario.precio())
            .multiply(BigDecimal.valueOf(cantidad)).doubleValue(), precioUnitario.moneda()); }
    public UUID getId() { return id; }
    public UUID getFraganciaId() { return fraganciaId; }
    public UUID getItemId() { return itemId; }
    public UUID getVendedorId() { return vendedorId; }
    public String getDescripcionFragancia() { return descripcionFragrancia; }
    public Precio getPrecioUnitario() { return precioUnitario; }
    public int getCantidad() { return cantidad; }
    @Override public boolean equals(Object o) { return this == o || o instanceof LineaPedido l && Objects.equals(id, l.id); }
    @Override public int hashCode() { return Objects.hash(id); }
}
