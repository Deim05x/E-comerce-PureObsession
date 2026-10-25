package com.uniquindio.Ecomerce_PureObsession.infrastructure.persistence.jpa;

import com.uniquindio.Ecomerce_PureObsession.domain.entity.*;
import com.uniquindio.Ecomerce_PureObsession.domain.repository.PedidoRepository;
import com.uniquindio.Ecomerce_PureObsession.domain.valueObject.*;
import com.uniquindio.Ecomerce_PureObsession.support.InventarioVentaTestConfig;
import jakarta.persistence.EntityManager;
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
    @Autowired EntityManager entityManager;

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

    @Test void deberiaOrdenarElRankingDeFraganciasPorCantidadVendida() {
        UUID masVendida=UUID.randomUUID();
        UUID menosVendida=UUID.randomUUID();
        pedidos.guardar(Pedido.crear(UUID.randomUUID(),UUID.randomUUID(),List.of(linea(masVendida,5))));
        pedidos.guardar(Pedido.crear(UUID.randomUUID(),UUID.randomUUID(),List.of(linea(masVendida,2))));
        pedidos.guardar(Pedido.crear(UUID.randomUUID(),UUID.randomUUID(),List.of(linea(menosVendida,3))));
        entityManager.flush();
        entityManager.clear();

        List<Object[]> ranking=entityManager.createQuery(
                "select l.fraganciaId, sum(l.cantidad) from PedidoJpa p join p.lineas l "
                        + "group by l.fraganciaId order by sum(l.cantidad) desc",Object[].class).getResultList();

        assertEquals(2,ranking.size());
        assertEquals(masVendida,ranking.getFirst()[0]);
        assertEquals(7L,((Number)ranking.getFirst()[1]).longValue());
    }

    private static LineaPedido linea(UUID fragancia) {
        return linea(fragancia,1);
    }
    private static LineaPedido linea(UUID fragancia,int cantidad) {
        return LineaPedido.crear(UUID.randomUUID(),UUID.randomUUID(),fragancia,"Línea",Precio.crear(50000.0,"COP"),cantidad);
    }
}
