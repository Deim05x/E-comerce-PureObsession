package com.uniquindio.Ecomerce_PureObsession.domain.entity;

import com.uniquindio.Ecomerce_PureObsession.domain.exception.ReglaDominioException;
import com.uniquindio.Ecomerce_PureObsession.domain.valueObject.Precio;

import java.util.Objects;
import java.util.UUID;

public class LineaPedido {
    private final UUID id;
    private final UUID fraganciaId;
    private final UUID itemId; //referencia por ID  a Decant, perfume o tester
    private final String descripcionFragrancia; // foto del momento de la compra
    private final Precio precioUnitario;
    private final int cantidad;

    private LineaPedido(UUID id, UUID itemId, UUID fraganciaId, String descripcionFragancia, Precio precioUnitario, int cantidad){
        if (id == null || itemId == null || fraganciaId == null || precioUnitario == null
                || descripcionFragancia == null || descripcionFragancia.isBlank()) {
            throw new ReglaDominioException("La línea requiere identidades, descripción y precio.");
        }
        if (cantidad <= 0){
            throw new ReglaDominioException("La cantidad de una linea debe ser mayor a 0.");
        }
        this.id = id;
        this.itemId = itemId;
        this.fraganciaId = fraganciaId;
        this.descripcionFragrancia = descripcionFragancia;
        this.precioUnitario = precioUnitario;
        this.cantidad = cantidad;
    }

    public static LineaPedido crear(UUID id, UUID itemId, UUID fraganciaId, String descripcionFragancia, Precio precioUnitario, int cantidad) {
        return new LineaPedido(id, itemId, fraganciaId, descripcionFragancia, precioUnitario, cantidad);
    }

    public Precio calcularSubtotal() {
        return new Precio(java.math.BigDecimal.valueOf(precioUnitario.precio()).multiply(java.math.BigDecimal.valueOf(cantidad)).doubleValue(), precioUnitario.moneda());
    }

    public UUID getId() { return id; }
    public UUID getFraganciaId() { return fraganciaId; }
    public UUID getItemId() { return itemId; }
    public String getDescripcionFragancia() { return descripcionFragrancia; }
    public Precio getPrecioUnitario() { return precioUnitario; }
    public int getCantidad() { return cantidad; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LineaPedido that = (LineaPedido) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
