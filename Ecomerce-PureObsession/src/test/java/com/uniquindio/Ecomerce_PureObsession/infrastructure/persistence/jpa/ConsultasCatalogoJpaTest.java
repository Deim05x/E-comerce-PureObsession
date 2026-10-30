package com.uniquindio.Ecomerce_PureObsession.infrastructure.persistence.jpa;

import com.uniquindio.Ecomerce_PureObsession.application.port.out.*;
import com.uniquindio.Ecomerce_PureObsession.domain.entity.*;
import com.uniquindio.Ecomerce_PureObsession.domain.repository.*;
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
@Import({ConsultaCatalogoJpaAdapter.class, BotellaMadreJpaAdapter.class, PresentacionJpaAdapter.class, PedidoJpaAdapter.class})
class ConsultasCatalogoJpaTest {
    @Autowired ConsultaCatalogoRepository consultas;
    @Autowired BotellaMadreRepository botellas;
    @Autowired PresentacionRepository presentaciones;
    @Autowired PedidoRepository pedidos;
    @Autowired EntityManager em;

    @Test void paginaTextoYFamiliaEnAmbosCatalogosSinDuplicarFragancias() {
        BotellaMadre primera=guardar("Rosa Alba",FamiliaOlfativa.FLORAL);
        BotellaMadre segunda=guardar("Rosa Brisa",FamiliaOlfativa.FLORAL);
        guardar("Rosa Cedro",FamiliaOlfativa.AMADERADA);
        guardar("Jazmin",FamiliaOlfativa.FLORAL);
        // Dos botellas de una fragancia no multiplican contenido ni count.
        botellas.guardar(BotellaMadre.crear(UUID.randomUUID(),primera.getFragancia(),Volumen.crear(100)));
        em.flush(); em.clear();
        var pagina=consultas.buscarFragancias("  ROSA ",FamiliaOlfativa.FLORAL,0,1);
        assertEquals(2,pagina.totalElementos()); assertEquals(2,pagina.totalPaginas());
        assertEquals("Rosa Alba",pagina.contenido().getFirst().getNombre());
        var siguiente=consultas.buscarFragancias("rosa",FamiliaOlfativa.FLORAL,1,1);
        assertEquals(segunda.getFragancia().getId(),siguiente.contenido().getFirst().getId());
        var ofertas=consultas.buscarPresentaciones("rosa",FamiliaOlfativa.FLORAL,1,1);
        assertEquals(2,ofertas.totalElementos()); assertEquals(2,ofertas.totalPaginas());
        assertEquals(segunda.getFragancia().getId(),ofertas.contenido().getFirst().getFraganciaId());
        var fuera=consultas.buscarFragancias("rosa",FamiliaOlfativa.FLORAL,5,1);
        assertTrue(fuera.contenido().isEmpty()); assertEquals(2,fuera.totalElementos());
        var vacia=consultas.buscarPresentaciones("inexistente",null,0,10);
        assertEquals(0,vacia.totalPaginas()); assertTrue(vacia.contenido().isEmpty());
    }

    @Test void noExponeEliminadasInactivasNiBorradoresYBuscaComodinesLiterales() {
        BotellaMadre retirada=guardar("Retirada",FamiliaOlfativa.FLORAL);
        botellas.eliminar(retirada.getId());
        BotellaMadre inactiva=guardar("Inactiva",FamiliaOlfativa.FLORAL);
        inactiva.getFragancia().desactivar(); botellas.guardar(inactiva);
        BotellaMadre borrador=guardar("Borrador",FamiliaOlfativa.FLORAL);
        FraganciaJpa f=em.find(FraganciaJpa.class,borrador.getFragancia().getId()); f.publicada=false;
        guardar("Aroma 100%_!",FamiliaOlfativa.FLORAL);
        guardar("Aroma normal",FamiliaOlfativa.FLORAL);
        em.flush(); em.clear();
        assertEquals(2,consultas.buscarFragancias(null,null,0,20).totalElementos());
        assertEquals(2,consultas.buscarPresentaciones(null,null,0,20).totalElementos());
        assertEquals(1,consultas.buscarFragancias("%_!",null,0,20).totalElementos());
        assertEquals(2,botellas.listarFraganciasPublicadas(null).size());
    }

    @Test void rankingSumaVentasRealesExcluyeCreadoCanceladoYOrdenaEmpates() {
        BotellaMadre alba=guardar("Alba",FamiliaOlfativa.FLORAL);
        BotellaMadre brisa=guardar("Brisa",FamiliaOlfativa.FLORAL);
        BotellaMadre cedro=guardar("Cedro",FamiliaOlfativa.AMADERADA);
        BotellaMadre retirada=guardar("Retirada",FamiliaOlfativa.FLORAL);
        venta(alba,2,EstadoPedido.CONFIRMADO); venta(alba,3,EstadoPedido.ENTREGADO);
        venta(brisa,5,EstadoPedido.ENVIADO); venta(cedro,1,EstadoPedido.CONFIRMADO);
        venta(cedro,100,EstadoPedido.CREADO); venta(cedro,100,EstadoPedido.CANCELADO);
        venta(retirada,1000,EstadoPedido.ENTREGADO); botellas.eliminar(retirada.getId());
        em.flush(); em.clear();
        var ranking=consultas.ranking(10);
        assertEquals(List.of("Alba","Brisa","Cedro"),ranking.stream().map(RankingFragancia::nombre).toList());
        assertEquals(List.of(5L,5L,1L),ranking.stream().map(RankingFragancia::cantidadVendida).toList());
        assertEquals(alba.getFragancia().getId(),consultas.ranking(1).getFirst().fraganciaId());
    }

    private BotellaMadre guardar(String nombre,FamiliaOlfativa familia) {
        Fragancia f=Fragancia.crear(UUID.randomUUID(),nombre,familia,Concentracion.EAU_DE_PARFUM,
                PiramideOlfativa.crear("Salida","Corazon","Fondo")); f.publicar();
        BotellaMadre b=BotellaMadre.crear(UUID.randomUUID(),f,Volumen.crear(100)); botellas.guardar(b);
        Presentacion p=Presentacion.crear(UUID.randomUUID(),UUID.randomUUID(),f.getId(),b.getId(),
                TipoPresentacion.DECANT,10,new BigDecimal("20000.00"),OrigenProducto.IMPORTADO,"FR",false);
        p.publicar(false); presentaciones.guardar(p); return b;
    }
    private void venta(BotellaMadre b,int cantidad,EstadoPedido estado) {
        Pedido p=Pedido.crear(UUID.randomUUID(),UUID.randomUUID(),List.of(LineaPedido.crear(UUID.randomUUID(),
                UUID.randomUUID(),b.getFragancia().getId(),b.getFragancia().getNombre(),Precio.crear(20000.0,"COP"),cantidad)));
        if(estado==EstadoPedido.CANCELADO) p.cancelar();
        else if(estado!=EstadoPedido.CREADO) {
            p.confirmar();
            if(estado==EstadoPedido.ENVIADO || estado==EstadoPedido.ENTREGADO) p.marcarEnviado();
            if(estado==EstadoPedido.ENTREGADO) p.marcarEntregado();
        }
        pedidos.guardar(p);
    }
}
