package com.uniquindio.Ecomerce_PureObsession.domain.entity;

import com.uniquindio.Ecomerce_PureObsession.domain.exception.ReglaDominioException;
import com.uniquindio.Ecomerce_PureObsession.domain.valueObject.EstadoPedido;
import com.uniquindio.Ecomerce_PureObsession.domain.valueObject.Precio;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;


public class Pedido {

    private final UUID id;
    private final UUID clienteId;
    private final List<LineaPedido> lineas;
    private EstadoPedido estado;
    private Precio total;

    private Pedido(UUID id, UUID clienteId, List<LineaPedido> lineasIniciales) {
        if (id == null || clienteId == null) {
            throw new ReglaDominioException("El pedido y el cliente requieren identidad.");
        }
        validarLineas(lineasIniciales);
        this.id = id;
        this.clienteId = clienteId;
        this.lineas = new ArrayList<>(lineasIniciales);
        this.estado = EstadoPedido.CREADO;
        this.total = recalcularTotal();
    }

    //Invariante 1: nunca se crea un pedido sin al menos una linea
    public static Pedido crear(UUID id, UUID clienteId, List<LineaPedido> lineasIniciales) {
        if (lineasIniciales == null || lineasIniciales.isEmpty()){
            throw new ReglaDominioException("Un pedido no puede crearse sin al menos una linea.");
        }
        return new Pedido(id,clienteId,lineasIniciales);
    }

    //Invariante 3: no se editan lineas si el pedido ya no está en CREADO
    public void agregarLinea(LineaPedido linea) {
        if ( this.estado != EstadoPedido.CREADO) {
            throw new ReglaDominioException("No se puede agregar líneas a un pedido que ya fue confirmado.");
        }
        List<LineaPedido> candidatas = new ArrayList<>(lineas);
        candidatas.add(linea);
        validarLineas(candidatas);
        Precio nuevoTotal = calcularTotal(candidatas);
        this.lineas.add(linea);
        this.total = nuevoTotal;
    }

    // Invariante 4: transiciones válidas de estado
    public void confirmar() {
        if (this.estado != EstadoPedido.CREADO) {
            throw new ReglaDominioException("Solo un pedido en estado CREADO puede confirmarse.");
        }
        this.estado = EstadoPedido.CONFIRMADO;
    }

    public void marcarEnviado() {
        if (this.estado != EstadoPedido.CONFIRMADO) {
            throw new ReglaDominioException("Solo un pedido CONFIRMADO puede marcarse como ENVIADO.");
        }
        this.estado = EstadoPedido.ENVIADO;
    }

    public void marcarEntregado() {
        if (this.estado != EstadoPedido.ENVIADO) {
            throw new ReglaDominioException("Solo un pedido ENVIADO puede marcarse como ENTREGADO.");
        }
        this.estado = EstadoPedido.ENTREGADO;
    }

    public void cancelar() {
        if (this.estado != EstadoPedido.CREADO && this.estado != EstadoPedido.CONFIRMADO) {
            throw new ReglaDominioException("Solo un pedido creado o confirmado puede cancelarse.");
        }
        this.estado = EstadoPedido.CANCELADO;
    }

    // Regla de negocio 7 (Reseñas verificadas)
    public boolean permiteResena() {
        return this.estado == EstadoPedido.ENTREGADO;
    }

    public boolean estaActivo() {
        return this.estado != EstadoPedido.ENTREGADO && this.estado != EstadoPedido.CANCELADO;
    }

    public boolean contieneItem(UUID itemId) {
        return this.lineas.stream().anyMatch(l -> l.getItemId().equals(itemId));
    }

    public boolean contieneFragancia(UUID fraganciaId) {
        return lineas.stream().anyMatch(l -> l.getFraganciaId().equals(fraganciaId));
    }

    private static void validarLineas(List<LineaPedido> lineas) {
        if (lineas == null || lineas.isEmpty() || lineas.stream().anyMatch(Objects::isNull)) {
            throw new ReglaDominioException("El pedido requiere al menos una línea válida.");
        }
        String moneda = lineas.get(0).getPrecioUnitario().moneda();
        if (lineas.stream().anyMatch(l -> !moneda.equals(l.getPrecioUnitario().moneda()))) {
            throw new ReglaDominioException("Todas las líneas deben usar la misma moneda.");
        }
        if (lineas.stream().map(LineaPedido::getId).distinct().count() != lineas.size()) {
            throw new ReglaDominioException("No se puede agregar dos veces la misma línea.");
        }
    }

    private Precio recalcularTotal() { return calcularTotal(lineas); }

    private static Precio calcularTotal(List<LineaPedido> lineas) {
        java.math.BigDecimal suma = java.math.BigDecimal.ZERO;
        for (LineaPedido linea : lineas) {
            suma = suma.add(java.math.BigDecimal.valueOf(linea.getPrecioUnitario().precio())
                    .multiply(java.math.BigDecimal.valueOf(linea.getCantidad())));
        }
        return new Precio(suma.doubleValue(), lineas.get(0).getPrecioUnitario().moneda());
    }

    public UUID getId() { return id; }
    public UUID getClienteId() { return clienteId; }
    public List<LineaPedido> getLineas() { return Collections.unmodifiableList(lineas); }
    public EstadoPedido getEstado() { return estado; }
    public Precio getTotal() { return total; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Pedido pedido = (Pedido) o;
        return Objects.equals(id, pedido.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}

