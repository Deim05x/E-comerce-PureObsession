package com.uniquindio.Ecomerce_PureObsession.infrastructure;
import com.uniquindio.Ecomerce_PureObsession.application.port.in.*;
import com.uniquindio.Ecomerce_PureObsession.application.port.out.*;
import com.uniquindio.Ecomerce_PureObsession.application.dto.request.*;
import com.uniquindio.Ecomerce_PureObsession.domain.entity.*;
import com.uniquindio.Ecomerce_PureObsession.domain.repository.*;
import com.uniquindio.Ecomerce_PureObsession.domain.valueObject.*;
import com.uniquindio.Ecomerce_PureObsession.support.InventarioVentaTestConfig;
import org.springframework.context.annotation.Import;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.test.web.servlet.*;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
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
@Import(InventarioVentaTestConfig.class)
class FlujoHexagonalTest {
    @Autowired WebApplicationContext contexto;
    @Autowired CuentasPort cuentas;
    @Autowired CuentaRepository cuentaRepository;
    @Autowired PasswordEncoder encoder;
    @Autowired RegistrarBotellaMadrePort registro;
    @Autowired CrearDecantPort decants;
    @Autowired BotellaMadreRepository botellas;
    @Autowired PedidoRepository pedidos;
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
        var resultado=mvc.perform(post("/api/presentaciones").with(user(vendedor).roles("VENDEDOR")).with(csrf())
            .contentType(MediaType.APPLICATION_JSON).content(contenido)).andExpect(status().isCreated()).andReturn();
        return json.readTree(resultado.getResponse().getContentAsString()).get("id").asText();
    }
    @Test void jakartaRechazaCamposFaltantesYNoAdmiteRegistroAdmin() throws Exception {
        mvc.perform(post("/api/auth/registro").with(csrf()).contentType(MediaType.APPLICATION_JSON).content("{}"))
            .andExpect(status().isBadRequest()).andExpect(jsonPath("$.campos.cedula").exists());
        mvc.perform(post("/api/auth/registro").with(csrf()).contentType(MediaType.APPLICATION_JSON)
            .content("{\"nombre\":\"Uno\",\"cedula\":\"123456789\",\"email\":\"admin@test.com\",\"clave\":\"ClavePrueba2026\",\"rol\":\"ADMIN\"}"))
            .andExpect(status().isUnprocessableEntity());
    }
    @Test void registroLoginSesionYCierre() throws Exception {
        String email=UUID.randomUUID()+"@test.com";
        String registroJson="""
            {"nombre":"Cliente","cedula":"%s","email":"%s","clave":"ClavePrueba2026","rol":"CLIENTE"}
            """.formatted(Long.toString(System.nanoTime()).substring(0,12),email);
        mvc.perform(post("/api/auth/registro").with(csrf()).contentType(MediaType.APPLICATION_JSON).content(registroJson))
            .andExpect(status().isCreated()).andExpect(jsonPath("$.clave").doesNotExist()).andExpect(jsonPath("$.claveHash").doesNotExist());
        var guardada=cuentaRepository.buscarPorEmail(email).orElseThrow();
        assertNotEquals("ClavePrueba2026",guardada.getClaveHash());
        assertTrue(encoder.matches("ClavePrueba2026",guardada.getClaveHash()));
        mvc.perform(post("/api/auth/registro").with(csrf()).contentType(MediaType.APPLICATION_JSON).content(registroJson))
            .andExpect(status().isUnprocessableEntity());
        mvc.perform(post("/api/auth/entrar").with(csrf()).contentType(MediaType.APPLICATION_JSON)
            .content("{\"email\":\""+email+"\",\"clave\":\"incorrecta\"}")).andExpect(status().isUnauthorized());
        var login=mvc.perform(post("/api/auth/entrar").with(csrf()).contentType(MediaType.APPLICATION_JSON)
            .content("{\"email\":\""+email+"\",\"clave\":\"ClavePrueba2026\"}"))
            .andExpect(status().isOk()).andReturn();
        var sesion=(MockHttpSession)login.getRequest().getSession(false);
        mvc.perform(get("/api/auth/yo").session(sesion)).andExpect(status().isOk()).andExpect(jsonPath("$.email").value(email));
        mvc.perform(post("/api/auth/salir").session(sesion).with(csrf())).andExpect(status().isNoContent());
        assertTrue(sesion.isInvalid());
    }
    @Test void protegeMutacionesConCsrfYRoles() throws Exception {
        mvc.perform(post("/api/auth/registro").contentType(MediaType.APPLICATION_JSON).content("{}")).andExpect(status().isForbidden());
        mvc.perform(get("/api/auth/yo")).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/inventario/botellas").with(user("cliente").roles("CLIENTE")).with(csrf())
            .contentType(MediaType.APPLICATION_JSON).content("{}")).andExpect(status().isForbidden());
        mvc.perform(post("/api/inventario/botellas").with(user("admin").roles("ADMIN")).with(csrf())
            .contentType(MediaType.APPLICATION_JSON).content("{}")).andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.campos.volumenMl").exists());
    }
    @Test void certificadoRequierePropietarioRevisionYDespublicaAlCambiar() throws Exception {
        String vendedor=cuenta(Rol.VENDEDOR), otro=cuenta(Rol.VENDEDOR), admin=cuenta(Rol.ADMIN);
        String id=nuevaPresentacion(vendedor,true,"DECANT");
        mvc.perform(post("/api/presentaciones/"+id+"/publicacion").with(user(vendedor).roles("VENDEDOR")).with(csrf()))
            .andExpect(status().isUnprocessableEntity());
        var falso=new MockMultipartFile("archivo","certificado.pdf","application/pdf","texto".getBytes());
        mvc.perform(multipart("/api/presentaciones/"+id+"/certificado").file(falso).with(user(vendedor).roles("VENDEDOR")).with(csrf()))
            .andExpect(status().isUnprocessableEntity());
        var archivo=new MockMultipartFile("archivo","certificado.pdf","application/pdf","%PDF-1.7\nDocumento de prueba".getBytes());
        mvc.perform(multipart("/api/presentaciones/"+id+"/certificado").file(archivo).with(user(vendedor).roles("VENDEDOR")).with(csrf()))
            .andExpect(status().isNoContent());
        mvc.perform(post("/api/presentaciones/"+id+"/revision").with(user(vendedor).roles("VENDEDOR")).with(csrf())
            .contentType(MediaType.APPLICATION_JSON).content("{\"aprobado\":true}")).andExpect(status().isForbidden());
        mvc.perform(get("/api/presentaciones/"+id+"/certificado").with(user(otro).roles("VENDEDOR")))
            .andExpect(status().isUnprocessableEntity());
        mvc.perform(get("/api/presentaciones/"+id+"/certificado").with(user(admin).roles("ADMIN")))
            .andExpect(status().isOk()).andExpect(header().string("X-Estado-Certificado","PENDIENTE"));
        mvc.perform(post("/api/presentaciones/"+id+"/revision").with(user(admin).roles("ADMIN")).with(csrf())
            .contentType(MediaType.APPLICATION_JSON).content("{\"aprobado\":true}")).andExpect(status().isNoContent());
        mvc.perform(post("/api/presentaciones/"+id+"/publicacion").with(user(vendedor).roles("VENDEDOR")).with(csrf()))
            .andExpect(status().isOk()).andExpect(jsonPath("$.publicada").value(true));
        mvc.perform(get("/api/catalogo")).andExpect(status().isOk()).andExpect(jsonPath("$[?(@.id=='"+id+"')]").isNotEmpty());
        mvc.perform(multipart("/api/presentaciones/"+id+"/certificado").file(archivo).with(user(vendedor).roles("VENDEDOR")).with(csrf()))
            .andExpect(status().isNoContent());
        mvc.perform(get("/api/catalogo")).andExpect(jsonPath("$[?(@.id=='"+id+"')]").isEmpty());
        mvc.perform(post("/api/presentaciones/"+id+"/publicacion").with(user(vendedor).roles("VENDEDOR")).with(csrf()))
            .andExpect(status().isUnprocessableEntity());
    }
    @Test void testerPublicadoRechazaEmpaqueRegalo() throws Exception {
        String vendedor=cuenta(Rol.VENDEDOR);
        String id=nuevaPresentacion(vendedor,false,"TESTER");
        mvc.perform(post("/api/presentaciones/"+id+"/publicacion").with(user(vendedor).roles("VENDEDOR")).with(csrf()))
            .andExpect(status().isOk());
        mvc.perform(post("/api/presentaciones/"+id+"/empaque/validacion").with(user("cliente").roles("CLIENTE")).with(csrf())
            .contentType(MediaType.APPLICATION_JSON).content("{\"tipo\":\"REGALO\"}")).andExpect(status().isUnprocessableEntity());
        mvc.perform(post("/api/presentaciones/"+id+"/empaque/validacion").with(user("cliente").roles("CLIENTE")).with(csrf())
            .contentType(MediaType.APPLICATION_JSON).content("{\"tipo\":\"ESTANDAR\"}"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.permitido").value(true));
    }
    @Test void csrfRealSeRenuevaAlEntrar() throws Exception {
        String email=cuenta(Rol.CLIENTE);
        var inicial=mvc.perform(get("/api/auth/csrf")).andExpect(status().isOk()).andReturn();
        var sesion=(MockHttpSession)inicial.getRequest().getSession(false);
        var token=json.readTree(inicial.getResponse().getContentAsString());
        String encabezado=token.get("headerName").asText();
        String anterior=token.get("token").asText();
        mvc.perform(post("/api/auth/entrar").session(sesion).header(encabezado,anterior)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"email\":\""+email+"\",\"clave\":\"ClavePrueba2026\"}"))
            .andExpect(status().isOk());
        mvc.perform(post("/api/auth/salir").session(sesion).header(encabezado,anterior))
            .andExpect(status().isForbidden());
        var nuevo=mvc.perform(get("/api/auth/csrf").session(sesion)).andReturn();
        String actualizado=json.readTree(nuevo.getResponse().getContentAsString()).get("token").asText();
        mvc.perform(post("/api/auth/salir").session(sesion).header(encabezado,actualizado))
            .andExpect(status().isNoContent());
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

