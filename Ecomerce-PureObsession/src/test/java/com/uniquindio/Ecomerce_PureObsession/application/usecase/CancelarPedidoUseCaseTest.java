package com.uniquindio.Ecomerce_PureObsession.application.usecase;

import com.uniquindio.Ecomerce_PureObsession.application.port.out.*;
import com.uniquindio.Ecomerce_PureObsession.domain.entity.*;
import com.uniquindio.Ecomerce_PureObsession.domain.valueObject.*;
import com.uniquindio.Ecomerce_PureObsession.infrastructure.persistence.PedidoRepositoryEnMemoria;
import java.util.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CancelarPedidoUseCaseTest {
    @Test void cancelarPedidoCreadoLiberaCadaLineaUnaSolaVez() {
        String clave = "clave-de-prueba";
        var pedidos = new PedidoRepositoryEnMemoria();
        Pedido pedido = pedidoCreado(clave);
        pedidos.guardar(pedido);
        var inventario = new InventarioFalso();
        var respuesta = new CancelarPedidoUseCase(pedidos, cuentasVacias(), inventario)
                .ejecutar(pedido.getId(), null, clave);
        assertEquals(EstadoPedido.CANCELADO, respuesta.estado());
        assertEquals(2, inventario.unidadesLiberadas);
        assertThrows(RuntimeException.class, () -> new CancelarPedidoUseCase(pedidos, cuentasVacias(), inventario)
                .ejecutar(pedido.getId(), null, clave));
        assertEquals(2, inventario.unidadesLiberadas);
    }
    private static Pedido pedidoCreado(String clave) {
        LineaPedido linea = LineaPedido.crearConVendedor(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                UUID.randomUUID(), "Presentación PERFUME de 100 ml", Precio.crear(100000.0, "COP"), 2);
        return Pedido.crearParaCheckout(UUID.randomUUID(), null,
                new DatosEntrega("Ana", "ana@ejemplo.com", "3001234567", "Calle 20"),
                ClaveConsultaPedido.hash(clave), List.of(linea));
    }
    private static CuentaRepository cuentasVacias() {
        return new CuentaRepository() {
            public Cuenta guardar(Cuenta cuenta) { return cuenta; }
            public Optional<Cuenta> buscarPorEmail(String email) { return Optional.empty(); }
            public Optional<Cuenta> buscarPorId(UUID id) { return Optional.empty(); }
            public boolean existeCedula(String cedula) { return false; }
        };
    }
    private static final class InventarioFalso implements InventarioVentaPort {
        int unidadesLiberadas;
        public void reservar(UUID presentacionId, int cantidad) { }
        public void liberar(UUID presentacionId, int cantidad) { unidadesLiberadas += cantidad; }
    }
}
