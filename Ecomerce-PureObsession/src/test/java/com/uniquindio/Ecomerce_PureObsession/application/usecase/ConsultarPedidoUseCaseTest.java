package com.uniquindio.Ecomerce_PureObsession.application.usecase;

import com.uniquindio.Ecomerce_PureObsession.application.port.out.CuentaRepository;
import com.uniquindio.Ecomerce_PureObsession.domain.entity.*;
import com.uniquindio.Ecomerce_PureObsession.domain.valueObject.*;
import com.uniquindio.Ecomerce_PureObsession.infrastructure.persistence.PedidoRepositoryEnMemoria;
import java.util.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ConsultarPedidoUseCaseTest {
    @Test void invitadoConsultaSoloConClaveYEstaNoSeVuelveAExponer() {
        String clave = "secreto-de-prueba";
        var pedidos = new PedidoRepositoryEnMemoria();
        Pedido pedido = Pedido.crearParaCheckout(UUID.randomUUID(), null,
                new DatosEntrega("Ana", "ana@ejemplo.com", "3001234567", "Calle 20"),
                ClaveConsultaPedido.hash(clave), List.of(linea()));
        pedidos.guardar(pedido);
        var caso = new ConsultarPedidoUseCase(pedidos, cuentasVacias());
        var respuesta = caso.consultar(pedido.getId(), null, clave);
        assertEquals(pedido.getId(), respuesta.id());
        assertNull(respuesta.claveConsulta());
        assertThrows(RuntimeException.class, () -> caso.consultar(pedido.getId(), null, "clave-incorrecta"));
    }
    private static LineaPedido linea() {
        return LineaPedido.crearConVendedor(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                "Presentación PERFUME de 100 ml", Precio.crear(100000.0, "COP"), 1);
    }
    private static CuentaRepository cuentasVacias() {
        return new CuentaRepository() {
            public Cuenta guardar(Cuenta cuenta) { return cuenta; }
            public Optional<Cuenta> buscarPorEmail(String email) { return Optional.empty(); }
            public Optional<Cuenta> buscarPorId(UUID id) { return Optional.empty(); }
            public boolean existeCedula(String cedula) { return false; }
        };
    }
}
