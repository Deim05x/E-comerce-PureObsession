package com.uniquindio.Ecomerce_PureObsession.infrastructure.web;
import com.uniquindio.Ecomerce_PureObsession.application.dto.request.*;
import com.uniquindio.Ecomerce_PureObsession.application.port.in.*;
import com.uniquindio.Ecomerce_PureObsession.application.port.out.CuentaRepository;
import com.uniquindio.Ecomerce_PureObsession.domain.repository.BotellaMadreRepository;
import com.uniquindio.Ecomerce_PureObsession.domain.valueObject.*;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Sin @Transactional: la compra debe confirmar o revertir su propia transacción para verificar el inventario. */
@SpringBootTest(properties={"spring.datasource.url=jdbc:h2:mem:pedidos-invitado;DB_CLOSE_DELAY=-1","spring.jpa.hibernate.ddl-auto=create-drop","app.admin.email=","app.admin.clave="})
@AutoConfigureMockMvc
class PedidosInvitadoHttpTest {
    @Autowired MockMvc mvc;
    @Autowired CuentasPort cuentas;
    @Autowired CuentaRepository cuentaRepository;
    @Autowired RegistrarBotellaMadrePort registro;
    @Autowired BotellaMadreRepository botellas;
    private final JsonMapper json=JsonMapper.builder().build();
    record Oferta(String id,UUID botellaId){}

    @Test void invitadoConCsrfCreaPedidoYRecibeClaveDeConsulta() throws Exception {
        Oferta oferta=ofertaPublicada();
        var inicial=mvc.perform(get("/api/auth/csrf")).andExpect(status().isOk()).andReturn();
        var sesion=(MockHttpSession)inicial.getRequest().getSession(false);
        JsonNode token=json.readTree(inicial.getResponse().getContentAsString());
        // Campos que el cliente no debe controlar: el servidor los ignora y calcula los suyos.
        String cuerpo="""
            {"nombre":"Invitada","correo":"invitada@test.com","telefono":"3001234567","direccion":"Calle 1",
             "items":[{"presentacionId":"%s","cantidad":2,"precio":1}],
             "total":1,"clienteId":"%s","claveConsulta":"elegida-por-cliente"}
            """.formatted(oferta.id(),UUID.randomUUID());
        var resultado=mvc.perform(post("/api/pedidos").session(sesion).header(token.get("headerName").asText(),token.get("token").asText())
                .contentType(MediaType.APPLICATION_JSON).content(cuerpo))
            .andExpect(status().isCreated()).andReturn();
        JsonNode pedido=json.readTree(resultado.getResponse().getContentAsString());
        assertEquals("/api/pedidos/"+pedido.get("id").asText(),resultado.getResponse().getHeader("Location"));
        assertTrue(pedido.get("clienteId").isNull());
        assertEquals(190000.0,pedido.get("total").asDouble());
        String clave=pedido.get("claveConsulta").asText();
        assertFalse(clave.isBlank());
        assertNotEquals("elegida-por-cliente",clave);
        assertEquals(100,disponible(oferta));
    }

    @Test void invitadoSinCsrfRecibe403YNoReservaInventario() throws Exception {
        Oferta oferta=ofertaPublicada();
        mvc.perform(post("/api/pedidos").contentType(MediaType.APPLICATION_JSON).content(pedido(oferta.id(),1)))
            .andExpect(status().isForbidden());
        assertEquals(200,disponible(oferta));
    }

    @Test void clienteAutenticadoCompraSinRecibirClaveDeInvitado() throws Exception {
        Oferta oferta=ofertaPublicada();
        String email=cuenta(Rol.CLIENTE);
        String respuesta=mvc.perform(post("/api/pedidos").with(user(email).roles("CLIENTE")).with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content(pedido(oferta.id(),1)))
            .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        JsonNode pedido=json.readTree(respuesta);
        assertEquals(cuentaRepository.buscarPorEmail(email).orElseThrow().getId().toString(),pedido.get("clienteId").asText());
        assertTrue(pedido.path("claveConsulta").isNull()||pedido.path("claveConsulta").isMissingNode());
    }

    @Test void articuloInvalidoNoPersistePedidoNiReservaParcial() throws Exception {
        String email=cuenta(Rol.CLIENTE);
        Oferta a=ofertaPublicada(), b=ofertaPublicada();
        // El caso de uso reserva en orden de id: la primera reserva es válida y la segunda supera el inventario.
        Oferta primera=a.id().compareTo(b.id())<0?a:b, segunda=primera==a?b:a;
        String cuerpo="""
            {"telefono":"3001234567","direccion":"Calle 1",
             "items":[{"presentacionId":"%s","cantidad":1},{"presentacionId":"%s","cantidad":5}]}
            """.formatted(primera.id(),segunda.id());
        mvc.perform(post("/api/pedidos").with(user(email).roles("CLIENTE")).with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content(cuerpo))
            .andExpect(status().isUnprocessableEntity());
        assertEquals(200,disponible(primera));
        assertEquals(200,disponible(segunda));

        mvc.perform(post("/api/pedidos").with(user(email).roles("CLIENTE")).with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content(pedido(UUID.randomUUID().toString(),1)))
            .andExpect(status().isUnprocessableEntity());
        mvc.perform(post("/api/pedidos").with(user(email).roles("CLIENTE")).with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content(pedido(oferta(cuenta(Rol.VENDEDOR)).id(),1)))
            .andExpect(status().isUnprocessableEntity());
        mvc.perform(get("/api/pedidos/mios").with(user(email).roles("CLIENTE")))
            .andExpect(status().isOk()).andExpect(jsonPath("$").isEmpty());
    }

    @Test void invitadoConsultaSuPedidoConClaveSinExponerlaNiCrearSesion() throws Exception {
        JsonNode creado=pedidoInvitado(ofertaPublicada());
        String id=creado.get("id").asText(), clave=creado.get("claveConsulta").asText();
        var resultado=mvc.perform(get("/api/pedidos/"+id).header("X-Consulta-Pedido",clave))
            .andExpect(status().isOk()).andExpect(jsonPath("$.id").value(id)).andExpect(jsonPath("$.estado").value("CREADO"))
            .andReturn();
        JsonNode consultado=json.readTree(resultado.getResponse().getContentAsString());
        assertTrue(consultado.path("claveConsulta").isNull()||consultado.path("claveConsulta").isMissingNode());
        assertFalse(resultado.getResponse().getContentAsString().contains(clave));
        assertNull(resultado.getRequest().getSession(false));
        mvc.perform(get("/api/pedidos/"+id).header("X-Consulta-Pedido",clave)).andExpect(jsonPath("$.estado").value("CREADO"));
    }

    @Test void consultaDeInvitadoSinClaveOConClaveErroneaNoDevuelveDatos() throws Exception {
        String id=pedidoInvitado(ofertaPublicada()).get("id").asText();
        String sinClave=mvc.perform(get("/api/pedidos/"+id))
            .andExpect(status().isUnprocessableEntity()).andExpect(jsonPath("$.id").doesNotExist())
            .andReturn().getResponse().getContentAsString();
        String erronea=mvc.perform(get("/api/pedidos/"+id).header("X-Consulta-Pedido","clave-erronea"))
            .andExpect(status().isUnprocessableEntity()).andExpect(jsonPath("$.id").doesNotExist())
            .andReturn().getResponse().getContentAsString();
        assertFalse(sinClave.contains("invitada@test.com"));
        assertFalse(erronea.contains("invitada@test.com")||erronea.contains("clave-erronea"));
        // Una sesión ajena tampoco reemplaza la clave de invitado.
        mvc.perform(get("/api/pedidos/"+id).with(user(cuenta(Rol.CLIENTE)).roles("CLIENTE")))
            .andExpect(status().isUnprocessableEntity()).andExpect(jsonPath("$.id").doesNotExist());
    }

    @Test void soloElClienteDuenoConsultaSuPedidoSinClave() throws Exception {
        String dueno=cuenta(Rol.CLIENTE), ajeno=cuenta(Rol.CLIENTE);
        String id=json.readTree(mvc.perform(post("/api/pedidos").with(user(dueno).roles("CLIENTE")).with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content(pedido(ofertaPublicada().id(),1)))
            .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString()).get("id").asText();
        mvc.perform(get("/api/pedidos/"+id).with(user(dueno).roles("CLIENTE")))
            .andExpect(status().isOk()).andExpect(jsonPath("$.id").value(id));
        mvc.perform(get("/api/pedidos/"+id).with(user(ajeno).roles("CLIENTE")))
            .andExpect(status().isUnprocessableEntity()).andExpect(jsonPath("$.id").doesNotExist());
        mvc.perform(get("/api/pedidos/"+id)).andExpect(status().isUnprocessableEntity()).andExpect(jsonPath("$.id").doesNotExist());
    }

    @Test void misPedidosSigueRequiriendoSesion() throws Exception {
        mvc.perform(get("/api/pedidos/mios")).andExpect(status().isUnauthorized());
    }

    @Test void invitadoCancelaConClaveYCsrfLiberandoInventarioUnaSolaVez() throws Exception {
        Oferta oferta=ofertaPublicada();
        JsonNode creado=pedidoInvitado(oferta);
        String id=creado.get("id").asText(), clave=creado.get("claveConsulta").asText();
        assertEquals(150,disponible(oferta));
        var resultado=mvc.perform(post("/api/pedidos/"+id+"/cancelacion").header("X-Consulta-Pedido",clave).with(csrf()))
            .andExpect(status().isOk()).andExpect(jsonPath("$.estado").value("CANCELADO")).andReturn();
        assertFalse(resultado.getResponse().getContentAsString().contains(clave));
        assertEquals(200,disponible(oferta));
        mvc.perform(get("/api/pedidos/"+id).header("X-Consulta-Pedido",clave)).andExpect(jsonPath("$.estado").value("CANCELADO"));

        mvc.perform(post("/api/pedidos/"+id+"/cancelacion").header("X-Consulta-Pedido",clave).with(csrf()))
            .andExpect(status().isUnprocessableEntity());
        assertEquals(200,disponible(oferta));
    }

    @Test void cancelacionDeInvitadoSinCsrfOSinClaveValidaNoModificaElPedido() throws Exception {
        Oferta oferta=ofertaPublicada();
        JsonNode creado=pedidoInvitado(oferta);
        String id=creado.get("id").asText(), clave=creado.get("claveConsulta").asText();
        mvc.perform(post("/api/pedidos/"+id+"/cancelacion").header("X-Consulta-Pedido",clave))
            .andExpect(status().isForbidden());
        mvc.perform(post("/api/pedidos/"+id+"/cancelacion").with(csrf()))
            .andExpect(status().isUnprocessableEntity()).andExpect(jsonPath("$.id").doesNotExist());
        mvc.perform(post("/api/pedidos/"+id+"/cancelacion").header("X-Consulta-Pedido","clave-erronea").with(csrf()))
            .andExpect(status().isUnprocessableEntity()).andExpect(jsonPath("$.id").doesNotExist());
        mvc.perform(post("/api/pedidos/"+id+"/cancelacion").with(user(cuenta(Rol.CLIENTE)).roles("CLIENTE")).with(csrf()))
            .andExpect(status().isUnprocessableEntity());
        assertEquals(150,disponible(oferta));
        mvc.perform(get("/api/pedidos/"+id).header("X-Consulta-Pedido",clave)).andExpect(jsonPath("$.estado").value("CREADO"));
    }

    @Test void soloElClienteDuenoCancelaSuPedido() throws Exception {
        Oferta oferta=ofertaPublicada();
        String dueno=cuenta(Rol.CLIENTE), ajeno=cuenta(Rol.CLIENTE);
        String id=json.readTree(mvc.perform(post("/api/pedidos").with(user(dueno).roles("CLIENTE")).with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content(pedido(oferta.id(),1)))
            .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString()).get("id").asText();
        mvc.perform(post("/api/pedidos/"+id+"/cancelacion").with(user(ajeno).roles("CLIENTE")).with(csrf()))
            .andExpect(status().isUnprocessableEntity());
        assertEquals(150,disponible(oferta));
        mvc.perform(post("/api/pedidos/"+id+"/cancelacion").with(user(dueno).roles("CLIENTE")).with(csrf()))
            .andExpect(status().isOk()).andExpect(jsonPath("$.estado").value("CANCELADO"));
        assertEquals(200,disponible(oferta));
    }

    private JsonNode pedidoInvitado(Oferta oferta) throws Exception {
        return json.readTree(mvc.perform(post("/api/pedidos").with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content(pedido(oferta.id(),1)))
            .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString());
    }

    private String cuenta(Rol rol) {
        String email=UUID.randomUUID()+"@test.com";
        String cedula=Long.toString(Math.abs(UUID.randomUUID().getMostSignificantBits()%1000000000000L)+100000);
        cuentas.registrar(new RegistrarCuentaRequest("Persona",cedula,email,"ClavePrueba2026",rol));
        return email;
    }

    /** Tester de 50 ml sobre una botella madre de 200 ml: alcanza para 4 unidades. */
    private Oferta oferta(String vendedor) throws Exception {
        var botella=registro.ejecutar(new RegistrarBotellaMadreRequest("Aroma",FamiliaOlfativa.FLORAL,Concentracion.EAU_DE_PARFUM,"A","B","C",200));
        String contenido="""
            {"fraganciaId":"%s","botellaMadreId":"%s","tipo":"TESTER","volumenMl":50,
             "precio":95000,"origen":"IMPORTADO","paisOrigen":"FR","original":false}
            """.formatted(botella.fraganciaId(),botella.botellaMadreId());
        String respuesta=mvc.perform(post("/api/presentaciones").with(user(vendedor).roles("VENDEDOR")).with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content(contenido))
            .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return new Oferta(json.readTree(respuesta).get("id").asText(),botella.botellaMadreId());
    }

    private Oferta ofertaPublicada() throws Exception {
        String vendedor=cuenta(Rol.VENDEDOR);
        Oferta oferta=oferta(vendedor);
        mvc.perform(post("/api/presentaciones/"+oferta.id()+"/publicacion").with(user(vendedor).roles("VENDEDOR")).with(csrf()))
            .andExpect(status().isOk());
        return oferta;
    }

    private int disponible(Oferta oferta) {
        return botellas.buscarPorId(oferta.botellaId()).orElseThrow().getVolumenDisponible().mililitros();
    }

    private static String pedido(String presentacionId,int cantidad) {
        return """
            {"nombre":"Invitada","correo":"invitada@test.com","telefono":"3001234567","direccion":"Calle 1",
             "items":[{"presentacionId":"%s","cantidad":%d}]}
            """.formatted(presentacionId,cantidad);
    }
}
