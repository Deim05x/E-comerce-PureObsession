package com.uniquindio.Ecomerce_PureObsession.infrastructure.persistence.jpa;

import com.uniquindio.Ecomerce_PureObsession.domain.entity.*;
import com.uniquindio.Ecomerce_PureObsession.domain.repository.*;
import com.uniquindio.Ecomerce_PureObsession.application.port.out.*;
import com.uniquindio.Ecomerce_PureObsession.domain.exception.ReglaDominioException;
import com.uniquindio.Ecomerce_PureObsession.domain.valueObject.*;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import({BotellaMadreJpaAdapter.class, PresentacionJpaAdapter.class, PedidoJpaAdapter.class, InventarioVentaJpaAdapter.class})
class BorradoLogicoJpaTest {
    @Autowired EntityManager em;
    @Autowired BotellaMadreRepository botellas;
    @Autowired PresentacionRepository presentaciones;
    @Autowired PedidoRepository pedidos;
    @Autowired InventarioVentaPort inventario;

    @Test void conservaFilasEHistorialPeroOcultaInventarioYOfertas() {
        BotellaMadre botella = botella();
        botellas.guardar(botella);
        Presentacion oferta = oferta(botella);
        presentaciones.guardar(oferta);
        Pedido pedido = Pedido.crear(UUID.randomUUID(), UUID.randomUUID(), List.of(
                LineaPedido.crear(UUID.randomUUID(), oferta.getId(), botella.getFragancia().getId(), "Decant", Precio.crear(20000.0,"COP"), 2)));
        pedido.cancelar(); pedidos.guardar(pedido);
        assertEquals(1, presentaciones.listarPublicadas().size());

        botellas.eliminar(botella.getId());
        em.flush(); em.clear();

        assertTrue(botellas.buscarPorId(botella.getId()).isEmpty());
        assertTrue(botellas.listarTodas().isEmpty());
        assertTrue(presentaciones.listarPublicadas().isEmpty());
        assertTrue(em.find(BotellaJpa.class, botella.getId()).eliminada);
        FraganciaJpa fragancia = em.find(FraganciaJpa.class, botella.getFragancia().getId());
        assertTrue(fragancia.eliminada); assertFalse(fragancia.activa);
        assertEquals(2, pedidos.buscarPorId(pedido.getId()).orElseThrow().getLineas().getFirst().getCantidad());
        assertThrows(ReglaDominioException.class, () -> inventario.reservar(oferta.getId(), 1));
        assertThrows(ReglaDominioException.class, () -> botellas.guardar(botella));
    }

    @Test void eliminarUnaBotellaNoOcultaLasDemasDeLaFragancia() {
        BotellaMadre primera = botella(); botellas.guardar(primera);
        BotellaMadre segunda = BotellaMadre.crear(UUID.randomUUID(), primera.getFragancia(), Volumen.crear(100));
        botellas.guardar(segunda);
        presentaciones.guardar(oferta(primera)); presentaciones.guardar(oferta(segunda));
        botellas.eliminar(primera.getId());
        em.flush(); em.clear();
        assertEquals(1, botellas.listarTodas().size());
        assertFalse(em.find(FraganciaJpa.class, primera.getFragancia().getId()).eliminada);
        assertEquals(1, presentaciones.listarPublicadas().size());
        botellas.eliminar(segunda.getId());
        em.flush(); em.clear();
        assertTrue(em.find(FraganciaJpa.class, primera.getFragancia().getId()).eliminada);
    }

    private BotellaMadre botella() {
        Fragancia f = Fragancia.crear(UUID.randomUUID(), "Aroma", FamiliaOlfativa.FLORAL,
                Concentracion.EAU_DE_PARFUM, PiramideOlfativa.crear("Salida","Corazon","Fondo"));
        f.publicar(); return BotellaMadre.crear(UUID.randomUUID(), f, Volumen.crear(100));
    }
    private Presentacion oferta(BotellaMadre b) {
        Presentacion p = Presentacion.crear(UUID.randomUUID(), UUID.randomUUID(), b.getFragancia().getId(),
                b.getId(), TipoPresentacion.DECANT, 10, new BigDecimal("20000.00"), OrigenProducto.IMPORTADO,"FR",false);
        p.publicar(false); return p;
    }
}
