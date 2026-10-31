package com.uniquindio.Ecomerce_PureObsession.infrastructure;

import com.uniquindio.Ecomerce_PureObsession.application.port.out.*;
import com.uniquindio.Ecomerce_PureObsession.domain.entity.*;
import com.uniquindio.Ecomerce_PureObsession.domain.repository.*;
import com.uniquindio.Ecomerce_PureObsession.domain.valueObject.*;
import java.math.BigDecimal;
import java.util.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.*;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties={"spring.datasource.url=jdbc:h2:mem:consultas-http;DB_CLOSE_DELAY=-1","spring.jpa.hibernate.ddl-auto=create-drop","app.admin.email=","app.admin.clave="})
@SuppressWarnings({"removal","deprecation"})
class ConsultasCatalogoHttpTest {
    @Autowired WebApplicationContext contexto;
    @Autowired BotellaMadreRepository botellas;
    @Autowired PresentacionRepository presentaciones;
    @Autowired PedidoRepository pedidos;
    MockMvc mvc;
    @BeforeEach void preparar() { mvc=MockMvcBuilders.webAppContextSetup(contexto).apply(springSecurity()).build(); }

    @Test void catalogoPaginadoYRankingSonPublicosConMetadatosYCantidades() throws Exception {
        String nombre="HTTP-"+UUID.randomUUID();
        Fragancia f=Fragancia.crear(UUID.randomUUID(),nombre,FamiliaOlfativa.FLORAL,Concentracion.EAU_DE_PARFUM,
                PiramideOlfativa.crear("Salida","Corazon","Fondo")); f.publicar();
        BotellaMadre b=BotellaMadre.crear(UUID.randomUUID(),f,Volumen.crear(100)); botellas.guardar(b);
        Presentacion p=Presentacion.crear(UUID.randomUUID(),UUID.randomUUID(),f.getId(),b.getId(),TipoPresentacion.DECANT,
                10,new BigDecimal("10000.00"),OrigenProducto.IMPORTADO,"FR",false); p.publicar(false); presentaciones.guardar(p);
        Pedido pedido=Pedido.crear(UUID.randomUUID(),UUID.randomUUID(),List.of(LineaPedido.crear(UUID.randomUUID(),
                p.getId(),f.getId(),nombre,Precio.crear(10000.0,"COP"),3))); pedido.confirmar(); pedidos.guardar(pedido);
        mvc.perform(get("/api/catalogo/pagina").param("texto",nombre).param("familia","FLORAL").param("tamanio","1"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.totalElementos").value(1))
                .andExpect(jsonPath("$.totalPaginas").value(1)).andExpect(jsonPath("$.contenido[0].id").value(p.getId().toString()));
        mvc.perform(get("/api/catalogo/fragancias/pagina").param("texto",nombre))
                .andExpect(status().isOk()).andExpect(jsonPath("$.contenido[0].nombre").value(nombre));
        mvc.perform(get("/api/catalogo/ranking"))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].cantidadVendida").value(3));
    }
    @Test void rechazaParametrosInvalidos() throws Exception {
        mvc.perform(get("/api/catalogo/pagina").param("pagina","-1")).andExpect(status().isUnprocessableEntity());
        mvc.perform(get("/api/catalogo/fragancias/pagina").param("tamanio","101")).andExpect(status().isUnprocessableEntity());
        mvc.perform(get("/api/catalogo/pagina").param("pagina","2147483647").param("tamanio","100")).andExpect(status().isUnprocessableEntity());
        mvc.perform(get("/api/catalogo/ranking").param("limite","0")).andExpect(status().isUnprocessableEntity());
        mvc.perform(get("/api/catalogo/pagina").param("familia","INVALIDA")).andExpect(status().isBadRequest());
    }
}
