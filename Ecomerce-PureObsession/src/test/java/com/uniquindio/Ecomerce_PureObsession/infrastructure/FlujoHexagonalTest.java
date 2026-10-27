package com.uniquindio.Ecomerce_PureObsession.infrastructure;
import com.uniquindio.Ecomerce_PureObsession.application.port.in.*;
import com.uniquindio.Ecomerce_PureObsession.application.port.out.*;
import com.uniquindio.Ecomerce_PureObsession.application.dto.request.*;
import com.uniquindio.Ecomerce_PureObsession.domain.entity.*;
import com.uniquindio.Ecomerce_PureObsession.domain.repository.*;
import com.uniquindio.Ecomerce_PureObsession.domain.valueObject.*;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.test.web.servlet.*;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import java.time.Instant;
import org.springframework.http.MediaType;
import tools.jackson.databind.json.JsonMapper;
import java.util.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static com.uniquindio.Ecomerce_PureObsession.DatosPrueba.*;

@SpringBootTest(properties={"spring.datasource.url=jdbc:h2:mem:hexagonal;DB_CLOSE_DELAY=-1","spring.jpa.hibernate.ddl-auto=create-drop","app.admin.email=","app.admin.clave="})
class FlujoHexagonalTest {
    @Autowired WebApplicationContext contexto;
    @Autowired CuentasPort cuentas;
    @Autowired CuentaRepository cuentaRepository;
    @Autowired PasswordEncoder encoder;
    @Autowired RegistrarBotellaMadrePort registro;
    @Autowired CrearDecantPort decants;
    @Autowired BotellaMadreRepository botellas;
    @Autowired PedidoRepository pedidos;
    @Autowired JwtEncoder jwtEncoder;
    @Autowired JwtDecoder jwtDecoder;
    MockMvc mvc;
    JsonMapper json=JsonMapper.builder().build();
    @BeforeEach void preparar(){mvc=MockMvcBuilders.webAppContextSetup(contexto).apply(springSecurity()).build();}
    String cuenta(Rol rol) {
        String sufijo=UUID.randomUUID().toString();
        String email=sufijo+"@test.com";
        String cedula=Long.toString(Math.abs(UUID.randomUUID().getMostSignificantBits()%1000000000000L)+100000);
        if(rol==Rol.ADMIN)
            cuentaRepository.guardar(Cuenta.crear(UUID.randomUUID(),"Admin",cedula,email,encoder.encode("ClavePrueba2026"),rol));
        else cuentas.registrar(new RegistrarCuentaRequest("Persona",cedula,email,"ClavePrueba2026",rol));
        return email;
    }
    String nuevaPresentacion(String vendedor,boolean original,String tipo) throws Exception {
        var botella=registro.ejecutar(new RegistrarBotellaMadreRequest("Aroma",FamiliaOlfativa.FLORAL,Concentracion.EAU_DE_PARFUM,"A","B","C",200));
        String contenido="""
            {"fraganciaId":"%s","botellaMadreId":"%s","tipo":"%s","volumenMl":50,
             "precio":95000,"origen":"IMPORTADO","paisOrigen":"FR","original":%s}
            """.formatted(botella.fraganciaId(),botella.botellaMadreId(),tipo,original);
        var resultado=mvc.perform(post("/api/presentaciones").with(user(vendedor).roles("VENDEDOR"))
            .contentType(MediaType.APPLICATION_JSON).content(contenido)).andExpect(status().isCreated()).andReturn();
        return json.readTree(resultado.getResponse().getContentAsString()).get("id").asText();
    }
    @Test void jakartaRechazaCamposFaltantesYNoAdmiteRegistroAdmin() throws Exception {
        mvc.perform(post("/api/auth/registro").contentType(MediaType.APPLICATION_JSON).content("{}"))
            .andExpect(status().isBadRequest()).andExpect(jsonPath("$.campos.cedula").exists());
        mvc.perform(post("/api/auth/registro").contentType(MediaType.APPLICATION_JSON)
            .content("{\"nombre\":\"Uno\",\"cedula\":\"123456789\",\"email\":\"admin@test.com\",\"clave\":\"ClavePrueba2026\",\"rol\":\"ADMIN\"}"))
            .andExpect(status().isUnprocessableEntity());
    }
    @Test void registroYLoginDevuelvenJwtSinCrearSesion() throws Exception {
        String email=UUID.randomUUID()+"@test.com";
        String registroJson="""
            {"nombre":"Cliente","cedula":"%s","email":"%s","clave":"ClavePrueba2026","rol":"CLIENTE"}
            """.formatted(Long.toString(System.nanoTime()).substring(0,12),email);
        mvc.perform(post("/api/auth/registro").contentType(MediaType.APPLICATION_JSON).content(registroJson))
            .andExpect(status().isCreated()).andExpect(jsonPath("$.clave").doesNotExist()).andExpect(jsonPath("$.claveHash").doesNotExist());
        var guardada=cuentaRepository.buscarPorEmail(email).orElseThrow();
        assertNotEquals("ClavePrueba2026",guardada.getClaveHash());
        assertTrue(encoder.matches("ClavePrueba2026",guardada.getClaveHash()));
        mvc.perform(post("/api/auth/registro").contentType(MediaType.APPLICATION_JSON).content(registroJson))
            .andExpect(status().isUnprocessableEntity());
        mvc.perform(post("/api/auth/entrar").contentType(MediaType.APPLICATION_JSON)
            .content("{\"email\":\""+email+"\",\"clave\":\"incorrecta\"}")).andExpect(status().isUnauthorized());
        var login=mvc.perform(post("/api/auth/entrar").contentType(MediaType.APPLICATION_JSON)
            .content("{\"email\":\""+email+"\",\"clave\":\"ClavePrueba2026\"}"))
            .andExpect(status().isOk()).andReturn();
        assertNull(login.getRequest().getSession(false));
        var respuesta=json.readTree(login.getResponse().getContentAsString());
        assertEquals("Bearer",respuesta.get("tipo").asText());
        String token=respuesta.get("token").asText();
        var jwt=jwtDecoder.decode(token);
        assertEquals(email,jwt.getSubject());
        assertEquals("CLIENTE",jwt.getClaimAsString("rol"));
        assertNotNull(jwt.getExpiresAt());
        assertFalse(token.contains("ClavePrueba2026"));
        assertFalse(jwt.getClaims().containsKey("clave"));
        mvc.perform(get("/api/auth/yo").header("Authorization","Bearer "+token))
            .andExpect(status().isOk()).andExpect(jsonPath("$.email").value(email));
    }
    @Test void protegeRutasPorRol() throws Exception {
        mvc.perform(get("/api/auth/yo")).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/inventario/botellas").with(user("cliente").roles("CLIENTE"))
            .contentType(MediaType.APPLICATION_JSON).content("{}")).andExpect(status().isForbidden());
        mvc.perform(post("/api/inventario/botellas").with(user("admin").roles("ADMIN"))
            .contentType(MediaType.APPLICATION_JSON).content("{}")).andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.campos.volumenMl").exists());
    }
    @Test void certificadoRequierePropietarioRevisionYDespublicaAlCambiar() throws Exception {
        String vendedor=cuenta(Rol.VENDEDOR), otro=cuenta(Rol.VENDEDOR), admin=cuenta(Rol.ADMIN);
        String id=nuevaPresentacion(vendedor,true,"DECANT");
        mvc.perform(post("/api/presentaciones/"+id+"/publicacion").with(user(vendedor).roles("VENDEDOR")))
            .andExpect(status().isUnprocessableEntity());
        var falso=new MockMultipartFile("archivo","certificado.pdf","application/pdf","texto".getBytes());
        mvc.perform(multipart("/api/presentaciones/"+id+"/certificado").file(falso).with(user(vendedor).roles("VENDEDOR")))
            .andExpect(status().isUnprocessableEntity());
        var archivo=new MockMultipartFile("archivo","certificado.pdf","application/pdf","%PDF-1.7\nDocumento de prueba".getBytes());
        mvc.perform(multipart("/api/presentaciones/"+id+"/certificado").file(archivo).with(user(vendedor).roles("VENDEDOR")))
            .andExpect(status().isNoContent());
        mvc.perform(post("/api/presentaciones/"+id+"/revision").with(user(vendedor).roles("VENDEDOR"))
            .contentType(MediaType.APPLICATION_JSON).content("{\"aprobado\":true}")).andExpect(status().isForbidden());
        mvc.perform(get("/api/presentaciones/"+id+"/certificado").with(user(otro).roles("VENDEDOR")))
            .andExpect(status().isUnprocessableEntity());
        mvc.perform(get("/api/presentaciones/"+id+"/certificado").with(user(admin).roles("ADMIN")))
            .andExpect(status().isOk()).andExpect(header().string("X-Estado-Certificado","PENDIENTE"));
        mvc.perform(post("/api/presentaciones/"+id+"/revision").with(user(admin).roles("ADMIN"))
            .contentType(MediaType.APPLICATION_JSON).content("{\"aprobado\":true}")).andExpect(status().isNoContent());
        mvc.perform(post("/api/presentaciones/"+id+"/publicacion").with(user(vendedor).roles("VENDEDOR")))
            .andExpect(status().isOk()).andExpect(jsonPath("$.publicada").value(true));
        mvc.perform(get("/api/catalogo")).andExpect(status().isOk()).andExpect(jsonPath("$[?(@.id=='"+id+"')]").isNotEmpty());
        mvc.perform(multipart("/api/presentaciones/"+id+"/certificado").file(archivo).with(user(vendedor).roles("VENDEDOR")))
            .andExpect(status().isNoContent());
        mvc.perform(get("/api/catalogo")).andExpect(jsonPath("$[?(@.id=='"+id+"')]").isEmpty());
        mvc.perform(post("/api/presentaciones/"+id+"/publicacion").with(user(vendedor).roles("VENDEDOR")))
            .andExpect(status().isUnprocessableEntity());
    }
    @Test void testerPublicadoRechazaEmpaqueRegalo() throws Exception {
        String vendedor=cuenta(Rol.VENDEDOR);
        String id=nuevaPresentacion(vendedor,false,"TESTER");
        mvc.perform(post("/api/presentaciones/"+id+"/publicacion").with(user(vendedor).roles("VENDEDOR")))
            .andExpect(status().isOk());
        mvc.perform(post("/api/presentaciones/"+id+"/empaque/validacion").with(user("cliente").roles("CLIENTE"))
            .contentType(MediaType.APPLICATION_JSON).content("{\"tipo\":\"REGALO\"}")).andExpect(status().isUnprocessableEntity());
        mvc.perform(post("/api/presentaciones/"+id+"/empaque/validacion").with(user("cliente").roles("CLIENTE"))
            .contentType(MediaType.APPLICATION_JSON).content("{\"tipo\":\"ESTANDAR\"}"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.permitido").value(true));
    }
    @Test void tokenExpiradoOConFirmaAjenaRecibe401() throws Exception {
        String email=cuenta(Rol.CLIENTE);
        var claims=JwtClaimsSet.builder().subject(email).claim("rol","CLIENTE")
            .issuedAt(Instant.now().minusSeconds(7200)).expiresAt(Instant.now().minusSeconds(3600)).build();
        String expirado=jwtEncoder.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(),claims)).getTokenValue();
        mvc.perform(get("/api/auth/yo").header("Authorization","Bearer "+expirado)).andExpect(status().isUnauthorized());
        var ajeno=new NimbusJwtEncoder(new com.nimbusds.jose.jwk.source.ImmutableSecret<>(new byte[32]));
        String falsificado=ajeno.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(),
            JwtClaimsSet.builder().subject(email).claim("rol","ADMIN").expiresAt(Instant.now().plusSeconds(600)).build())).getTokenValue();
        mvc.perform(get("/api/inventario/botellas").header("Authorization","Bearer "+falsificado)).andExpect(status().isUnauthorized());
    }
    @Test void extraccionesConcurrentesNoVendenElMismoVolumen() throws Exception {
        var b=registro.ejecutar(new RegistrarBotellaMadreRequest("Concurrente",FamiliaOlfativa.FLORAL,
            Concentracion.EAU_DE_PARFUM,"A","B","C",100));
        var inicio=new java.util.concurrent.CountDownLatch(1);
        java.util.concurrent.Callable<Boolean> extraer=()->{
            inicio.await();
            try { decants.ejecutar(new CrearDecantRequest(b.botellaMadreId(),60)); return true; }
            catch (com.uniquindio.Ecomerce_PureObsession.domain.exception.ReglaDominioException e) { return false; }
        };
        try(var executor=java.util.concurrent.Executors.newFixedThreadPool(2)) {
            var a=executor.submit(extraer); var c=executor.submit(extraer); inicio.countDown();
            assertNotEquals(a.get(15,java.util.concurrent.TimeUnit.SECONDS),c.get(15,java.util.concurrent.TimeUnit.SECONDS));
        }
        var recargada=botellas.buscarPorId(b.botellaMadreId()).orElseThrow();
        assertEquals(40,recargada.getVolumenDisponible().mililitros());
        assertEquals(1,recargada.getDecants().size());
    }
    @Test void h2ConservaDecantsAgotamientoYEstadoDePedidos() {
        var b=registro.ejecutar(new RegistrarBotellaMadreRequest("Inventario",FamiliaOlfativa.FLORAL,Concentracion.EAU_DE_PARFUM,"A","B","C",100));
        decants.ejecutar(new CrearDecantRequest(b.botellaMadreId(),70));
        decants.ejecutar(new CrearDecantRequest(b.botellaMadreId(),30));
        var recargada=botellas.buscarPorId(b.botellaMadreId()).orElseThrow();
        assertEquals(0,recargada.getVolumenDisponible().mililitros()); assertEquals(2,recargada.getDecants().size());
        var pedido=Pedido.crear(UUID.randomUUID(),UUID.randomUUID(),List.of(linea(b.fraganciaId(),"COP")));
        pedidos.guardar(pedido);
        assertTrue(pedidos.existePedidoActivoConFragancia(b.fraganciaId()));
        pedido.confirmar(); pedido.marcarEnviado(); pedido.marcarEntregado(); pedidos.guardar(pedido);
        assertFalse(pedidos.existePedidoActivoConFragancia(b.fraganciaId()));
        assertEquals(EstadoPedido.ENTREGADO,pedidos.buscarPorId(pedido.getId()).orElseThrow().getEstado());
    }
}

