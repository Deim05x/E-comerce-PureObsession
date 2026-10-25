package com.uniquindio.Ecomerce_PureObsession.domain.entity;

import com.uniquindio.Ecomerce_PureObsession.domain.exception.ReglaDominioException;
import com.uniquindio.Ecomerce_PureObsession.domain.valueObject.*;
import java.math.BigDecimal;
import java.util.*;

public class Pedido {
    private final UUID id, clienteId;
    private final DatosEntrega entrega;
    private final String claveConsultaHash;
    private final List<LineaPedido> lineas;
    private EstadoPedido estado;
    private Precio total;
    private Pedido(UUID id, UUID clienteId, DatosEntrega entrega, String claveConsultaHash,
                   List<LineaPedido> lineas, EstadoPedido estado) {
        this.id = id; this.clienteId = clienteId; this.entrega = entrega; this.claveConsultaHash = claveConsultaHash;
        this.lineas = new ArrayList<>(lineas); this.estado = estado; this.total = calcularTotal(lineas);
    }
    /** Conserva el contrato anterior. */
    public static Pedido crear(UUID id, UUID clienteId, List<LineaPedido> lineas) {
        if (id == null || clienteId == null) throw new ReglaDominioException("El pedido y el cliente requieren identidad.");
        validarLineas(lineas);
        return new Pedido(id, clienteId, null, null, lineas, EstadoPedido.CREADO);
    }
    public static Pedido crearParaCheckout(UUID id, UUID clienteId, DatosEntrega entrega, String claveConsultaHash,
                                           List<LineaPedido> lineas) {
        if (id == null || entrega == null) throw new ReglaDominioException("Faltan datos del pedido.");
        if (clienteId == null && (claveConsultaHash == null || !claveConsultaHash.matches("[0-9a-f]{64}")))
            throw new ReglaDominioException("La compra invitada necesita una clave segura.");
        validarLineas(lineas);
        if (lineas.stream().anyMatch(l -> l.getVendedorId() == null))
            throw new ReglaDominioException("Cada artículo debe pertenecer a un vendedor.");
        if (lineas.stream().map(LineaPedido::getItemId).distinct().count() != lineas.size())
            throw new ReglaDominioException("No repita una presentación en el checkout.");
        return new Pedido(id, clienteId, entrega, claveConsultaHash, lineas, EstadoPedido.CREADO);
    }
    public static Pedido reconstituir(UUID id, UUID clienteId, List<LineaPedido> lineas, EstadoPedido estado) {
        if (estado == null) throw new ReglaDominioException("El estado es obligatorio.");
        Pedido pedido = crear(id, clienteId, lineas); pedido.estado = estado; return pedido;
    }
    public static Pedido reconstituir(UUID id, UUID clienteId, DatosEntrega entrega, String claveConsultaHash,
                                      List<LineaPedido> lineas, EstadoPedido estado) {
        if (estado == null) throw new ReglaDominioException("El estado es obligatorio.");
        Pedido pedido = crearParaCheckout(id, clienteId, entrega, claveConsultaHash, lineas);
        pedido.estado = estado; return pedido;
    }
    public void agregarLinea(LineaPedido linea) {
        if (estado != EstadoPedido.CREADO) throw new ReglaDominioException("El pedido ya no admite líneas.");
        List<LineaPedido> candidatas = new ArrayList<>(lineas); candidatas.add(linea); validarLineas(candidatas);
        lineas.add(linea); total = calcularTotal(candidatas);
    }
    public void confirmar() { if (estado != EstadoPedido.CREADO) throw new ReglaDominioException("Solo se confirma un pedido CREADO."); estado = EstadoPedido.CONFIRMADO; }
    public void marcarEnviado() { if (estado != EstadoPedido.CONFIRMADO) throw new ReglaDominioException("Solo un pedido CONFIRMADO puede enviarse."); estado = EstadoPedido.ENVIADO; }
    public void marcarEntregado() { if (estado != EstadoPedido.ENVIADO) throw new ReglaDominioException("Solo un pedido ENVIADO puede entregarse."); estado = EstadoPedido.ENTREGADO; }
    /** Operación histórica que conserva la regla anterior. */
    public void cancelar() { if (estado != EstadoPedido.CREADO && estado != EstadoPedido.CONFIRMADO) throw new ReglaDominioException("Solo un pedido creado o confirmado puede cancelarse."); estado = EstadoPedido.CANCELADO; }
    /** Solo el pedido aún impago puede liberar la reserva automáticamente. */
    public void cancelarAntesDePago() {
        if (estado != EstadoPedido.CREADO)
            throw new ReglaDominioException("El pedido confirmado requiere un proceso de devolución.");
        estado = EstadoPedido.CANCELADO;
    }
    public boolean permiteResena() { return estado == EstadoPedido.ENTREGADO; }
    public boolean estaActivo() { return estado != EstadoPedido.ENTREGADO && estado != EstadoPedido.CANCELADO; }
    public boolean contieneItem(UUID itemId) { return lineas.stream().anyMatch(l -> l.getItemId().equals(itemId)); }
    public boolean contieneFragancia(UUID fraganciaId) { return lineas.stream().anyMatch(l -> l.getFraganciaId().equals(fraganciaId)); }
    private static void validarLineas(List<LineaPedido> lineas) {
        if (lineas == null || lineas.isEmpty() || lineas.stream().anyMatch(Objects::isNull)) throw new ReglaDominioException("El pedido requiere al menos una línea válida.");
        String moneda = lineas.get(0).getPrecioUnitario().moneda();
        if (lineas.stream().anyMatch(l -> !moneda.equals(l.getPrecioUnitario().moneda()))) throw new ReglaDominioException("Todas las líneas deben usar la misma moneda.");
        if (lineas.stream().map(LineaPedido::getId).distinct().count() != lineas.size()) throw new ReglaDominioException("No se puede agregar dos veces la misma línea.");
    }
    private static Precio calcularTotal(List<LineaPedido> lineas) {
        BigDecimal suma = BigDecimal.ZERO;
        for (LineaPedido linea : lineas) suma = suma.add(BigDecimal.valueOf(linea.getPrecioUnitario().precio()).multiply(BigDecimal.valueOf(linea.getCantidad())));
        return Precio.crear(suma.doubleValue(), lineas.get(0).getPrecioUnitario().moneda());
    }
    public UUID getId() { return id; }
    public UUID getClienteId() { return clienteId; }
    public DatosEntrega getEntrega() { return entrega; }
    public String getClaveConsultaHash() { return claveConsultaHash; }
    public boolean esInvitado() { return clienteId == null; }
    public List<LineaPedido> getLineas() { return Collections.unmodifiableList(lineas); }
    public EstadoPedido getEstado() { return estado; }
    public Precio getTotal() { return total; }
    @Override public boolean equals(Object o) { return this == o || o instanceof Pedido p && Objects.equals(id, p.id); }
    @Override public int hashCode() { return Objects.hash(id); }
}
