package com.uniquindio.Ecomerce_PureObsession.infrastructure.persistence.jpa;

import com.uniquindio.Ecomerce_PureObsession.domain.entity.*;
import com.uniquindio.Ecomerce_PureObsession.domain.repository.PedidoRepository;
import com.uniquindio.Ecomerce_PureObsession.domain.valueObject.*;
import com.uniquindio.Ecomerce_PureObsession.support.InventarioVentaTestConfig;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties={"spring.datasource.url=jdbc:h2:mem:pedido-jpa;DB_CLOSE_DELAY=-1","spring.jpa.hibernate.ddl-auto=create-drop","app.admin.email=","app.admin.clave="})
@Import(InventarioVentaTestConfig.class)
@Transactional
class PedidoRepositoryPruebasAvanzadasTest {
    @Autowired PedidoRepository pedidos;

    @Test void deberiaGuardarYRecuperarPedidoConEntregaPrecioYEstado() {
        UUID cliente=UUID.randomUUID();
        Pedido pedido=pedido(UUID.randomUUID(),cliente,UUID.randomUUID(),2);
        pedidos.guardar(pedido);

        Pedido recuperado=pedidos.buscarPorId(pedido.getId()).orElseThrow();

        assertEquals("Ana",recuperado.getEntrega().nombre());
        assertEquals(250000.0,recuperado.getLineas().getFirst().getPrecioUnitario().precio());
        assertEquals(EstadoPedido.CREADO,recuperado.getEstado());
    }

    @Test void deberiaListarSoloLosPedidosDelClienteSolicitado() {
        UUID cliente=UUID.randomUUID();
        pedidos.guardar(pedido(UUID.randomUUID(),cliente,UUID.randomUUID(),1));
        pedidos.guardar(pedido(UUID.randomUUID(),cliente,UUID.randomUUID(),3));
        pedidos.guardar(pedido(UUID.randomUUID(),UUID.randomUUID(),UUID.randomUUID(),1));

        List<Pedido> resultado=pedidos.listarPorCliente(cliente);

        assertEquals(2,resultado.size());
        assertTrue(resultado.stream().allMatch(p->cliente.equals(p.getClienteId())));
        assertEquals(4,resultado.stream().mapToInt(p->p.getLineas().getFirst().getCantidad()).sum());
    }

    private static Pedido pedido(UUID id,UUID cliente,UUID fragancia,int cantidad) {
        LineaPedido linea=LineaPedido.crearConVendedor(UUID.randomUUID(),UUID.randomUUID(),fragancia,
                UUID.randomUUID(),"Presentación de prueba",Precio.crear(250000.0,"COP"),cantidad);
        return Pedido.crearParaCheckout(id,cliente,new DatosEntrega("Ana","ana@prueba.com","3001234567","Calle 20"),null,List.of(linea));
    }
}

@DataJpaTest
@Import(PedidoJpaAdapter.class)
class PedidoRepositoryDataJpaPruebaAvanzadaTest {
    @Autowired PedidoRepository pedidos;

    @Test void deberiaConsultarPedidosActivosPorFraganciaConElSliceJPA() {
        UUID fragancia=UUID.randomUUID();
        Pedido activo=Pedido.crear(UUID.randomUUID(),UUID.randomUUID(),List.of(linea(fragancia)));
        Pedido entregado=Pedido.crear(UUID.randomUUID(),UUID.randomUUID(),List.of(linea(fragancia)));
        entregado.confirmar(); entregado.marcarEnviado(); entregado.marcarEntregado();
        pedidos.guardar(activo); pedidos.guardar(entregado);

        assertTrue(pedidos.existePedidoActivoConFragancia(fragancia));
        activo.cancelar(); pedidos.guardar(activo);
        assertFalse(pedidos.existePedidoActivoConFragancia(fragancia));
    }

    private static LineaPedido linea(UUID fragancia) {
        return LineaPedido.crear(UUID.randomUUID(),UUID.randomUUID(),fragancia,"Línea",Precio.crear(50000.0,"COP"),1);
    }
}
