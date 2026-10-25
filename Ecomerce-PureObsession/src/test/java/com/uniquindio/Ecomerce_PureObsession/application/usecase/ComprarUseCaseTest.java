package com.uniquindio.Ecomerce_PureObsession.application.usecase;

import com.uniquindio.Ecomerce_PureObsession.application.dto.request.*;
import com.uniquindio.Ecomerce_PureObsession.application.port.out.*;
import com.uniquindio.Ecomerce_PureObsession.domain.entity.*;
import com.uniquindio.Ecomerce_PureObsession.domain.valueObject.*;
import com.uniquindio.Ecomerce_PureObsession.infrastructure.persistence.PedidoRepositoryEnMemoria;
import java.math.BigDecimal;
import java.util.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ComprarUseCaseTest {
    private final Presentacion oferta = oferta();
    private final PedidoRepositoryEnMemoria pedidos = new PedidoRepositoryEnMemoria();
    private final StockFalso stock = new StockFalso();
    private final PresentacionRepository publicaciones = new PresentacionRepository() {
        public Presentacion guardar(Presentacion presentacion) { return presentacion; }
        public Optional<Presentacion> buscarPorId(UUID id) { return oferta.getId().equals(id) ? Optional.of(oferta) : Optional.empty(); }
        public List<Presentacion> listarPublicadas() { return List.of(oferta); }
    };
    private final CuentaRepository cuentas = new CuentaRepository() {
        public Cuenta guardar(Cuenta cuenta) { return cuenta; }
        public Optional<Cuenta> buscarPorEmail(String email) { return Optional.empty(); }
        public Optional<Cuenta> buscarPorId(UUID id) { return Optional.empty(); }
        public boolean existeCedula(String cedula) { return false; }
    };
    private static Presentacion oferta() {
        Presentacion presentacion = Presentacion.crear(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), null,
                TipoPresentacion.PERFUME, 100, new BigDecimal("150000.00"), OrigenProducto.NACIONAL, "CO", false);
        presentacion.publicar(false);
        return presentacion;
    }
    private CrearPedidoRequest solicitud(int cantidad) {
        return new CrearPedidoRequest("Invitado", "invitado@ejemplo.com", "3001234567", "Calle 20",
                List.of(new ItemCheckoutRequest(oferta.getId(), cantidad)));
    }
    @Test void invitadoCompraConPrecioServidorYReservaStock() {
        var respuesta = new ComprarUseCase(cuentas, publicaciones, pedidos, stock).ejecutar(null, solicitud(2));
        assertEquals(300000.0, respuesta.total());
        assertNull(respuesta.clienteId());
        assertNotNull(respuesta.claveConsulta());
        assertEquals(2, stock.disponible);
        assertEquals(EstadoPedido.CREADO, pedidos.buscarPorId(respuesta.id()).orElseThrow().getEstado());
    }
    private static final class StockFalso implements InventarioVentaPort {
        int disponible = 4;
        public void reservar(UUID id, int cantidad) { if (disponible < cantidad) throw new IllegalStateException("Stock insuficiente"); disponible -= cantidad; }
        public void liberar(UUID id, int cantidad) { disponible += cantidad; }
    }
}
