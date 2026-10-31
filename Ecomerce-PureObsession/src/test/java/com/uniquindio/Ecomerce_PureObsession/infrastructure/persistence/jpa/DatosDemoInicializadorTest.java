package com.uniquindio.Ecomerce_PureObsession.infrastructure.persistence.jpa;

import com.uniquindio.Ecomerce_PureObsession.application.port.out.*;
import com.uniquindio.Ecomerce_PureObsession.domain.repository.*;
import com.uniquindio.Ecomerce_PureObsession.domain.valueObject.Rol;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("demo")
@SpringBootTest(properties={"spring.datasource.url=jdbc:h2:mem:demo-test;DB_CLOSE_DELAY=-1","spring.jpa.hibernate.ddl-auto=create-drop","app.admin.email=","app.admin.clave="})
class DatosDemoInicializadorTest {
    @Autowired DatosDemoInicializador semilla;
    @Autowired CuentaRepository cuentas;
    @Autowired BotellaMadreRepository botellas;
    @Autowired PedidoRepository pedidos;
    @Autowired ConsultaCatalogoRepository consultas;
    @Autowired InventarioVentaPort inventario;
    @Autowired PasswordEncoder encoder;
    @Autowired EntityManager em;

    @Test void arrancaConCuentasBCryptCatalogoYPedidosParaRanking() {
        assertEquals(Rol.ADMIN,cuentas.buscarPorEmail("admin@pureobsession.demo").orElseThrow().getRol());
        var vendedor=cuentas.buscarPorEmail("vendedor@pureobsession.demo").orElseThrow();
        assertTrue(encoder.matches("DemoPure2026!",vendedor.getClaveHash()));
        assertEquals(Rol.VENDEDOR,cuentas.buscarPorEmail("vendedor2@pureobsession.demo").orElseThrow().getRol());
        assertEquals(3,consultas.buscarFragancias(null,null,0,20).totalElementos());
        assertEquals(4,consultas.buscarPresentaciones(null,null,0,20).totalElementos());
        assertEquals(5,pedidos.listarPorCliente(DatosDemoInicializador.id("cliente")).size());
        var ranking=consultas.ranking(10);
        assertEquals(2,ranking.size()); assertEquals("Rosa de Jardin",ranking.getFirst().nombre());
        assertEquals(5,ranking.getFirst().cantidadVendida()); assertEquals(2,ranking.get(1).cantidadVendida());
    }

    @Test @Transactional void reiniciarNoDuplicaNiRestauraVolumenModificado() {
        var botellaId=DatosDemoInicializador.id("botella-rosa");
        int disponible=botellas.buscarPorId(botellaId).orElseThrow().getVolumenDisponible().mililitros();
        inventario.reservar(DatosDemoInicializador.id("oferta-rosa"),1);
        semilla.run(new DefaultApplicationArguments());
        em.flush(); em.clear();
        assertEquals(disponible-10,botellas.buscarPorId(botellaId).orElseThrow().getVolumenDisponible().mililitros());
        assertEquals(4L,em.createQuery("select count(c) from CuentaJpa c",Long.class).getSingleResult());
        assertEquals(5,pedidos.listarPorCliente(DatosDemoInicializador.id("cliente")).size());
    }
}
