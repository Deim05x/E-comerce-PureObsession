package com.uniquindio.Ecomerce_PureObsession.infrastructure;

import com.uniquindio.Ecomerce_PureObsession.application.port.in.CuentasPort;
import com.uniquindio.Ecomerce_PureObsession.application.port.out.*;
import com.uniquindio.Ecomerce_PureObsession.application.dto.request.RegistrarCuentaRequest;
import com.uniquindio.Ecomerce_PureObsession.domain.entity.Cuenta;
import com.uniquindio.Ecomerce_PureObsession.domain.valueObject.Rol;
import jakarta.persistence.EntityManager;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import java.util.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.*;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.mock.web.MockMultipartFile;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("demo")
@SpringBootTest(properties={"spring.datasource.url=jdbc:h2:mem:marketplace-completo;DB_CLOSE_DELAY=-1","spring.jpa.hibernate.ddl-auto=create-drop","app.admin.email=","app.admin.clave=","app.reservas.intervalo-ms=3600000"})
@SuppressWarnings({"deprecation","removal"})
class MarketplaceCompletoHttpTest {
    @Autowired WebApplicationContext contexto;
    @Autowired CuentasPort cuentas;
    @Autowired CuentaRepository cuentasRepository;
    @Autowired PasswordEncoder encoder;
    @Autowired GestionPedidosRepository gestion;
    @Autowired EntityManager em;
    @Autowired PlatformTransactionManager tx;
    MockMvc mvc;JsonMapper json=JsonMapper.builder().build();
    String vendedor,otro,cliente,admin;
    @BeforeEach void preparar() throws Exception {
        mvc=MockMvcBuilders.webAppContextSetup(contexto).apply(springSecurity()).build();
        vendedor=usuario(Rol.VENDEDOR);otro=usuario(Rol.VENDEDOR);cliente=usuario(Rol.CLIENTE);admin=usuario(Rol.ADMIN);
    }
    String usuario(Rol rol) throws Exception {
        String email=UUID.randomUUID()+"@ejemplo.com";
        String cedula=Long.toString(Math.abs(UUID.randomUUID().getMostSignificantBits()%1000000000000L)+100000);
        if(rol==Rol.ADMIN) cuentasRepository.guardar(Cuenta.crear(UUID.randomUUID(),"Admin",cedula,email,encoder.encode("ClavePrueba2026"),rol));
        else cuentas.registrar(new RegistrarCuentaRequest("Persona",cedula,email,"ClavePrueba2026",rol));
        return entrar(email,"ClavePrueba2026");
    }
    String entrar(String email,String clave) throws Exception {
        JsonNode r=body(mvc.perform(post("/api/auth/entrar").contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(Map.of("email",email,"clave",clave)))).andExpect(status().isOk()).andReturn());
        return r.has("token") ? r.get("token").asText() : r.get("accessToken").asText();
    }
    MockHttpServletRequestBuilder auth(MockHttpServletRequestBuilder b,String token) {return b.header("Authorization","Bearer "+token);}
    JsonNode body(MvcResult r) throws Exception {return json.readTree(r.getResponse().getContentAsString());}
    JsonNode producto(String token,String tipo,int stock) throws Exception {
        String nombre="Aroma-"+UUID.randomUUID();
        return body(mvc.perform(auth(post("/api/vendedor/productos"),token).contentType(MediaType.APPLICATION_JSON).content("""
            {"nombre":"%s","marca":"Casa de Prueba","descripcion":"Descripcion","categoria":"NICHO",
            "familia":"FLORAL","concentracion":"EAU_DE_PARFUM","notasSalida":"A","notasCorazon":"B","notasFondo":"C",
            "tipo":"%s","volumenMl":100,"precio":150000.25,"original":false,"origen":"NACIONAL","paisOrigen":"CO","stock":%d}
            """.formatted(nombre,tipo,stock))).andExpect(status().isCreated()).andReturn());
    }
    void publicar(String token,String id) throws Exception {mvc.perform(auth(post("/api/presentaciones/"+id+"/publicacion"),token)).andExpect(status().isOk());}
    JsonNode comprar(String token,String id,int cantidad) throws Exception {
        return body(mvc.perform(auth(post("/api/pedidos"),token).contentType(MediaType.APPLICATION_JSON).content("""
            {"telefono":"3001234567","direccion":"Calle de prueba","items":[{"presentacionId":"%s","cantidad":%d}]}
            """.formatted(id,cantidad))).andExpect(status().isCreated()).andReturn());
    }
    int stock(String id) throws Exception {return body(mvc.perform(get("/api/productos/"+id)).andExpect(status().isOk()).andReturn()).get("stockDisponible").asInt();}

    @Test void vendedorGestionaSoloSusProductosYPublicoTieneFichaCompleta() throws Exception {
        JsonNode p=producto(vendedor,"PERFUME",4);String id=p.get("id").asText();
        mvc.perform(get("/api/productos/"+id)).andExpect(status().isNotFound());
        mvc.perform(auth(put("/api/vendedor/productos/"+id+"/stock"),otro).contentType(MediaType.APPLICATION_JSON).content("{\"disponible\":99}")).andExpect(status().isForbidden());
        publicar(vendedor,id);
        mvc.perform(get("/api/productos").param("texto",p.get("nombre").asText()).param("minimo","100000").param("orden","precio_desc"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.totalElementos").value(1)).andExpect(jsonPath("$.contenido[0].marca").value("Casa de Prueba"));
        assertEquals(4,stock(id));
        mvc.perform(auth(delete("/api/vendedor/productos/"+id+"/publicacion"),vendedor)).andExpect(status().isNoContent());
        mvc.perform(get("/api/productos/"+id)).andExpect(status().isNotFound());
    }
    @Test void perfumeYTesterCompranPorUnidadesYCancelacionRestauraUnaSolaVez() throws Exception {
        for(String tipo:List.of("PERFUME","TESTER")) {
            String id=producto(vendedor,tipo,3).get("id").asText();publicar(vendedor,id);
            String pedido=comprar(cliente,id,2).get("id").asText();assertEquals(1,stock(id));
            mvc.perform(auth(post("/api/pedidos/"+pedido+"/cancelacion"),cliente)).andExpect(status().isOk());
            assertEquals(3,stock(id));
            mvc.perform(auth(post("/api/pedidos/"+pedido+"/cancelacion"),cliente)).andExpect(status().isUnprocessableEntity());
            assertEquals(3,stock(id));
        }
    }
    @Test void decantUsaMililitrosYNoPuedeUsarBotellaAjena() throws Exception {
        JsonNode perfume=producto(vendedor,"PERFUME",1);
        JsonNode botella=body(mvc.perform(auth(post("/api/vendedor/botellas"),vendedor).contentType(MediaType.APPLICATION_JSON)
                .content("{\"fraganciaId\":\""+perfume.get("fraganciaId").asText()+"\",\"volumenMl\":100}")).andExpect(status().isCreated()).andReturn());
        String datos="""
            {"nombre":"Decant","marca":"Casa","familia":"FLORAL","tipo":"DECANT","volumenMl":10,"precio":20000,
            "original":false,"origen":"NACIONAL","paisOrigen":"CO","botellaMadreId":"%s"}
            """.formatted(botella.get("id").asText());
        mvc.perform(auth(post("/api/vendedor/productos"),otro).contentType(MediaType.APPLICATION_JSON).content(datos)).andExpect(status().isForbidden());
        String id=body(mvc.perform(auth(post("/api/vendedor/productos"),vendedor).contentType(MediaType.APPLICATION_JSON).content(datos)).andExpect(status().isCreated()).andReturn()).get("id").asText();
        publicar(vendedor,id);assertEquals(10,stock(id));
        String pedido=comprar(cliente,id,6).get("id").asText();assertEquals(4,stock(id));
        mvc.perform(auth(post("/api/pedidos/"+pedido+"/cancelacion"),cliente)).andExpect(status().isOk());assertEquals(10,stock(id));
    }
    @Test void dosVendedoresDespachanSinVerNiModificarVentasAjenas() throws Exception {
        String a=producto(vendedor,"PERFUME",4).get("id").asText(),b=producto(otro,"TESTER",5).get("id").asText();publicar(vendedor,a);publicar(otro,b);
        JsonNode pedido=body(mvc.perform(auth(post("/api/pedidos"),cliente).contentType(MediaType.APPLICATION_JSON).content("""
            {"telefono":"3001234567","direccion":"Calle 1","items":[{"presentacionId":"%s","cantidad":1},{"presentacionId":"%s","cantidad":2}]}
            """.formatted(a,b))).andExpect(status().isCreated()).andReturn());String id=pedido.get("id").asText();
        mvc.perform(auth(post("/api/admin/pedidos/"+id+"/confirmacion"),vendedor).contentType(MediaType.APPLICATION_JSON).content("{\"referencia\":\"X\"}")).andExpect(status().isForbidden());
        mvc.perform(auth(post("/api/demo/pedidos/"+id+"/pago"),cliente)).andExpect(status().isOk()).andExpect(jsonPath("$.pago.simulado").value(true));
        mvc.perform(auth(post("/api/demo/pedidos/"+id+"/pago"),cliente)).andExpect(status().isOk());
        JsonNode ventas=body(mvc.perform(auth(get("/api/vendedor/ventas"),vendedor)).andExpect(status().isOk()).andReturn());
        assertEquals(1,ventas.get("contenido").get(0).get("items").size());
        assertEquals(a,ventas.get("contenido").get(0).get("items").get(0).get("presentacionId").asText());
        String va=body(mvc.perform(get("/api/productos/"+a)).andReturn()).get("vendedorId").asText();
        String vb=body(mvc.perform(get("/api/productos/"+b)).andReturn()).get("vendedorId").asText();
        mvc.perform(auth(post("/api/vendedor/ventas/"+id+"/envio"),vendedor).contentType(MediaType.APPLICATION_JSON).content("{\"transportadora\":\"Demo\",\"guia\":\"A\"}")).andExpect(status().isNoContent());
        mvc.perform(auth(get("/api/pedidos/"+id+"/seguimiento"),cliente)).andExpect(jsonPath("$.pedido.estado").value("CONFIRMADO"));
        mvc.perform(auth(post("/api/pedidos/"+id+"/envios/"+va+"/recepcion"),cliente)).andExpect(status().isOk());
        mvc.perform(auth(post("/api/vendedor/ventas/"+id+"/envio"),otro).contentType(MediaType.APPLICATION_JSON).content("{\"transportadora\":\"Demo\",\"guia\":\"B\"}")).andExpect(status().isNoContent());
        mvc.perform(auth(post("/api/pedidos/"+id+"/envios/"+vb+"/recepcion"),cliente)).andExpect(status().isOk()).andExpect(jsonPath("$.pedido.estado").value("ENTREGADO"));
        mvc.perform(auth(put("/api/productos/"+a+"/resena"),cliente).contentType(MediaType.APPLICATION_JSON).content("{\"puntuacion\":5,\"comentario\":\"Excelente\"}")).andExpect(status().isOk());
        mvc.perform(get("/api/productos/"+a)).andExpect(jsonPath("$.calificacion").value(5));
        mvc.perform(auth(get("/api/vendedor/dashboard"),vendedor)).andExpect(status().isOk()).andExpect(jsonPath("$.pedidosVendidos").value(1));
    }
    @Test void reservaVencidaSeLiberaSinDuplicarStock() throws Exception {
        String id=producto(vendedor,"PERFUME",3).get("id").asText();publicar(vendedor,id);
        UUID pedido=UUID.fromString(comprar(cliente,id,2).get("id").asText());
        new TransactionTemplate(tx).executeWithoutResult(s->em.createQuery("update PedidoJpa p set p.reservaVenceEn=:fecha where p.id=:id")
                .setParameter("fecha",java.time.Instant.now().minusSeconds(10)).setParameter("id",pedido).executeUpdate());
        assertEquals(1,gestion.expirar());assertEquals(0,gestion.expirar());assertEquals(3,stock(id));
        mvc.perform(auth(get("/api/pedidos/"+pedido+"/seguimiento"),cliente)).andExpect(jsonPath("$.motivoCancelacion").value("RESERVA_VENCIDA"));
        mvc.perform(auth(post("/api/demo/pedidos/"+pedido+"/pago"),cliente)).andExpect(status().isUnprocessableEntity());
    }
    @Test void carritoYFavoritosSonPrivadosYNoReservanHastaComprar() throws Exception {
        String id=producto(vendedor,"PERFUME",3).get("id").asText();publicar(vendedor,id);
        mvc.perform(auth(put("/api/carrito/items/"+id),cliente).contentType(MediaType.APPLICATION_JSON).content("{\"cantidad\":2}")).andExpect(status().isNoContent());
        mvc.perform(auth(put("/api/favoritos/"+id),cliente)).andExpect(status().isNoContent());
        mvc.perform(auth(get("/api/carrito"),cliente)).andExpect(jsonPath("$.subtotal").value(300000.5));
        mvc.perform(auth(get("/api/carrito"),otro)).andExpect(jsonPath("$.items").isEmpty());
        mvc.perform(auth(get("/api/favoritos"),cliente)).andExpect(jsonPath("$.totalElementos").value(1));
        assertEquals(3,stock(id));
        mvc.perform(auth(delete("/api/carrito"),cliente)).andExpect(status().isNoContent());
        mvc.perform(auth(get("/api/carrito"),cliente)).andExpect(jsonPath("$.items").isEmpty());
    }
    @Test void imagenValidaSePublicaYElVendedorAjenoNoPuedeModificarla() throws Exception {
        String id=producto(vendedor,"PERFUME",2).get("id").asText();
        var salida=new java.io.ByteArrayOutputStream();javax.imageio.ImageIO.write(new java.awt.image.BufferedImage(2,2,java.awt.image.BufferedImage.TYPE_INT_RGB),"png",salida);
        MockMultipartFile foto=new MockMultipartFile("archivo","foto.png","image/png",salida.toByteArray());
        mvc.perform(multipart("/api/vendedor/productos/"+id+"/imagenes").file(foto).header("Authorization","Bearer "+otro)).andExpect(status().isForbidden());
        String url=body(mvc.perform(multipart("/api/vendedor/productos/"+id+"/imagenes").file(foto).header("Authorization","Bearer "+vendedor)).andExpect(status().isCreated()).andReturn()).get("url").asText();
        mvc.perform(get(url)).andExpect(status().isNotFound());
        mvc.perform(auth(get(url),vendedor)).andExpect(status().isOk());
        mvc.perform(auth(get(url),otro)).andExpect(status().isNotFound());
        publicar(vendedor,id);mvc.perform(get(url)).andExpect(status().isOk()).andExpect(content().contentType("image/png"));
        mvc.perform(multipart("/api/vendedor/productos/"+id+"/imagenes").file(new MockMultipartFile("archivo","falso.png","image/png","texto".getBytes())).header("Authorization","Bearer "+vendedor)).andExpect(status().isUnprocessableEntity());
    }
    @Test void perfilDireccionesYRecuperacionRevocanTokensAnteriores() throws Exception {
        JsonNode perfil=body(mvc.perform(auth(get("/api/perfil"),cliente)).andReturn());String email=perfil.get("email").asText();
        mvc.perform(auth(put("/api/perfil"),cliente).contentType(MediaType.APPLICATION_JSON).content("{\"nombre\":\"Nombre actualizado\",\"telefono\":\"3001234567\"}")).andExpect(status().isOk());
        String direccion="{\"etiqueta\":\"Casa\",\"receptor\":\"Ana\",\"telefono\":\"3001234567\",\"departamento\":\"Quindio\",\"ciudad\":\"Armenia\",\"direccion\":\"Calle 1\",\"predeterminada\":true}";
        String id=body(mvc.perform(auth(post("/api/perfil/direcciones"),cliente).contentType(MediaType.APPLICATION_JSON).content(direccion)).andExpect(status().isCreated()).andReturn()).get("id").asText();
        mvc.perform(auth(put("/api/perfil/direcciones/"+id),otro).contentType(MediaType.APPLICATION_JSON).content(direccion)).andExpect(status().isNotFound());
        JsonNode recuperacion=body(mvc.perform(post("/api/auth/recuperacion").contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(Map.of("email",email)))).andExpect(status().isOk()).andReturn());
        String token=recuperacion.get("tokenDemo").asText();
        String datos=json.writeValueAsString(Map.of("token",token,"nueva","OtraClave2026!"));
        mvc.perform(post("/api/auth/restablecer").contentType(MediaType.APPLICATION_JSON).content(datos)).andExpect(status().isNoContent());
        mvc.perform(auth(get("/api/perfil"),cliente)).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/auth/restablecer").contentType(MediaType.APPLICATION_JSON).content(datos)).andExpect(status().isUnprocessableEntity());
        String nuevo=entrar(email,"OtraClave2026!");mvc.perform(auth(get("/api/perfil"),nuevo)).andExpect(status().isOk());
    }
    @Test void corsPermiteFrontendConfiguradoYRechazaOrigenAjeno() throws Exception {
        mvc.perform(options("/api/vendedor/productos").header("Origin","http://localhost:5173").header("Access-Control-Request-Method","POST").header("Access-Control-Request-Headers","authorization,content-type"))
                .andExpect(status().isOk()).andExpect(header().string("Access-Control-Allow-Origin","http://localhost:5173"));
        mvc.perform(options("/api/vendedor/productos").header("Origin","https://ajeno.test").header("Access-Control-Request-Method","POST")).andExpect(status().isForbidden());
    }
    @Test void checkoutIdempotenteCotizaYNoDuplicaPedidoNiReserva() throws Exception {
        String id=producto(vendedor,"PERFUME",5).get("id").asText();publicar(vendedor,id);String key=UUID.randomUUID().toString();
        String datos="""
            {"telefono":"3001234567","direccion":"Calle 1","items":[{"presentacionId":"%s","cantidad":2,"empaque":"REGALO"}]}
            """.formatted(id);
        mvc.perform(post("/api/checkout/cotizacion").contentType(MediaType.APPLICATION_JSON).content("{\"items\":[{\"presentacionId\":\""+id+"\",\"cantidad\":2}]}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.subtotal").value(300000.5));
        assertEquals(5,stock(id));
        String primero=body(mvc.perform(auth(post("/api/checkout"),cliente).header("Idempotency-Key",key).contentType(MediaType.APPLICATION_JSON).content(datos)).andExpect(status().isCreated()).andReturn()).get("id").asText();
        mvc.perform(auth(post("/api/checkout"),cliente).header("Idempotency-Key",key).contentType(MediaType.APPLICATION_JSON).content(datos))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.id").value(primero));
        assertEquals(3,stock(id));
        mvc.perform(auth(post("/api/checkout"),cliente).header("Idempotency-Key",key).contentType(MediaType.APPLICATION_JSON).content(datos.replace("Calle 1","Calle 2")))
                .andExpect(status().isConflict());
        assertEquals(3,stock(id));
    }
    @Test void testerRechazaRegaloYUnFalloDeStockRevierteTodoElPedido() throws Exception {
        String tester=producto(vendedor,"TESTER",2).get("id").asText();publicar(vendedor,tester);
        mvc.perform(post("/api/checkout/cotizacion").contentType(MediaType.APPLICATION_JSON).content("{\"items\":[{\"presentacionId\":\""+tester+"\",\"cantidad\":1,\"empaque\":\"REGALO\"}]}"))
                .andExpect(status().isUnprocessableEntity());
        String a=producto(vendedor,"PERFUME",1).get("id").asText(),b=producto(vendedor,"PERFUME",1).get("id").asText();publicar(vendedor,a);publicar(vendedor,b);
        String primero=a.compareTo(b)<0 ? a : b,segundo=a.compareTo(b)<0 ? b : a;
        mvc.perform(auth(put("/api/vendedor/productos/"+segundo+"/stock"),vendedor).contentType(MediaType.APPLICATION_JSON).content("{\"disponible\":0}")).andExpect(status().isNoContent());
        mvc.perform(auth(post("/api/pedidos"),cliente).contentType(MediaType.APPLICATION_JSON).content("""
            {"telefono":"3001234567","direccion":"Calle 1","items":[{"presentacionId":"%s","cantidad":1},{"presentacionId":"%s","cantidad":1}]}
            """.formatted(primero,segundo))).andExpect(status().isUnprocessableEntity());
        assertEquals(1,stock(primero));assertEquals(0,stock(segundo));
    }
    @Test void dosComprasConcurrentesNoVendenLasMismasUnidades() throws Exception {
        String id=producto(vendedor,"PERFUME",3).get("id").asText();publicar(vendedor,id);
        var inicio=new java.util.concurrent.CountDownLatch(1);
        java.util.concurrent.Callable<Integer> compra=()->{
            inicio.await();return mvc.perform(auth(post("/api/pedidos"),cliente).contentType(MediaType.APPLICATION_JSON).content("""
                {"telefono":"3001234567","direccion":"Calle 1","items":[{"presentacionId":"%s","cantidad":2}]}
                """.formatted(id))).andReturn().getResponse().getStatus();
        };
        try(var executor=java.util.concurrent.Executors.newFixedThreadPool(2)) {
            var a=executor.submit(compra);var b=executor.submit(compra);inicio.countDown();
            assertEquals(Set.of(201,422),Set.of(a.get(20,java.util.concurrent.TimeUnit.SECONDS),b.get(20,java.util.concurrent.TimeUnit.SECONDS)));
        }
        assertEquals(1,stock(id));
    }
    @Test void administradorConfirmaPagoManualConReferenciaUnica() throws Exception {
        String id=producto(vendedor,"PERFUME",5).get("id").asText();publicar(vendedor,id);
        String a=comprar(cliente,id,1).get("id").asText(),b=comprar(cliente,id,1).get("id").asText(),ref=UUID.randomUUID().toString();
        String datos="{\"referencia\":\""+ref+"\"}";
        mvc.perform(auth(post("/api/admin/pedidos/"+a+"/confirmacion"),admin).contentType(MediaType.APPLICATION_JSON).content(datos))
                .andExpect(status().isOk()).andExpect(jsonPath("$.pago.simulado").value(false));
        mvc.perform(auth(post("/api/admin/pedidos/"+a+"/confirmacion"),admin).contentType(MediaType.APPLICATION_JSON).content(datos)).andExpect(status().isOk());
        mvc.perform(auth(post("/api/admin/pedidos/"+b+"/confirmacion"),admin).contentType(MediaType.APPLICATION_JSON).content(datos)).andExpect(status().isConflict());
        mvc.perform(auth(get("/api/pedidos/"+b+"/seguimiento"),cliente)).andExpect(jsonPath("$.pedido.estado").value("CREADO"));
        assertEquals(3,stock(id));
    }
    @Test void direccionPredeterminadaUnicaYEliminacionNoAfectaOtraCuenta() throws Exception {
        String datos="{\"etiqueta\":\"Casa\",\"receptor\":\"Ana\",\"telefono\":\"3001234567\",\"departamento\":\"Quindio\",\"ciudad\":\"Armenia\",\"direccion\":\"Calle 1\",\"predeterminada\":true}";
        String a=body(mvc.perform(auth(post("/api/perfil/direcciones"),cliente).contentType(MediaType.APPLICATION_JSON).content(datos)).andExpect(status().isCreated()).andReturn()).get("id").asText();
        String b=body(mvc.perform(auth(post("/api/perfil/direcciones"),cliente).contentType(MediaType.APPLICATION_JSON).content(datos)).andExpect(status().isCreated()).andReturn()).get("id").asText();
        mvc.perform(auth(put("/api/perfil/direcciones/"+b),cliente).contentType(MediaType.APPLICATION_JSON).content(datos)).andExpect(status().isOk());
        JsonNode lista=body(mvc.perform(auth(get("/api/perfil/direcciones"),cliente)).andReturn());
        int principales=0;for(JsonNode d:lista) if(d.get("predeterminada").asBoolean()) principales++;
        assertEquals(1,principales);assertEquals(b,lista.get(0).get("id").asText());
        mvc.perform(auth(delete("/api/perfil/direcciones/"+a),otro)).andExpect(status().isNotFound());
        mvc.perform(auth(delete("/api/perfil/direcciones/"+b),cliente)).andExpect(status().isNoContent());
        mvc.perform(auth(get("/api/perfil/direcciones"),cliente)).andExpect(jsonPath("$[0].id").value(a)).andExpect(jsonPath("$[0].predeterminada").value(true));
    }
    @Test void recuperacionExpiradaYCambioDeClaveConClaveActual() throws Exception {
        JsonNode perfil=body(mvc.perform(auth(get("/api/perfil"),cliente)).andReturn());
        String email=perfil.get("email").asText();UUID cuentaId=UUID.fromString(perfil.get("id").asText());
        String token=body(mvc.perform(post("/api/auth/recuperacion").contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(Map.of("email",email)))).andReturn()).get("tokenDemo").asText();
        new TransactionTemplate(tx).executeWithoutResult(s->em.createQuery("update RecuperacionClaveJpa r set r.venceEn=:fecha where r.cuentaId=:id")
                .setParameter("fecha",java.time.Instant.now().minusSeconds(10)).setParameter("id",cuentaId).executeUpdate());
        mvc.perform(post("/api/auth/restablecer").contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(Map.of("token",token,"nueva","NuevaClave2026!"))))
                .andExpect(status().isUnprocessableEntity());
        mvc.perform(auth(put("/api/perfil/clave"),cliente).contentType(MediaType.APPLICATION_JSON).content("{\"actual\":\"incorrecta\",\"nueva\":\"NuevaClave2026!\"}")).andExpect(status().isUnprocessableEntity());
        mvc.perform(auth(put("/api/perfil/clave"),cliente).contentType(MediaType.APPLICATION_JSON).content("{\"actual\":\"ClavePrueba2026\",\"nueva\":\"NuevaClave2026!\"}")).andExpect(status().isNoContent());
        mvc.perform(auth(get("/api/perfil"),cliente)).andExpect(status().isUnauthorized());
        mvc.perform(auth(get("/api/perfil"),entrar(email,"NuevaClave2026!"))).andExpect(status().isOk());
    }
    @Test void fraganciaBasePermitePublicarSoloDecantsYConfigurarTienda() throws Exception {
        String fragancia=body(mvc.perform(auth(post("/api/vendedor/fragancias"),vendedor).contentType(MediaType.APPLICATION_JSON).content("""
            {"nombre":"Fragancia independiente","familia":"FLORAL","concentracion":"EAU_DE_PARFUM","notasSalida":"A","notasCorazon":"B","notasFondo":"C"}
            """)).andExpect(status().isCreated()).andReturn()).get("id").asText();
        String botella=body(mvc.perform(auth(post("/api/vendedor/botellas"),vendedor).contentType(MediaType.APPLICATION_JSON).content("{\"fraganciaId\":\""+fragancia+"\",\"volumenMl\":100}"))
                .andExpect(status().isCreated()).andReturn()).get("id").asText();
        String tienda="{\"nombre\":\"Atelier de prueba\",\"descripcion\":\"Nuestra tienda\",\"ciudad\":\"Armenia\",\"telefono\":\"3001234567\"}";
        String vendedorId=body(mvc.perform(auth(put("/api/vendedor/tienda"),vendedor).contentType(MediaType.APPLICATION_JSON).content(tienda)).andExpect(status().isOk()).andReturn()).get("id").asText();
        String producto=body(mvc.perform(auth(post("/api/vendedor/productos"),vendedor).contentType(MediaType.APPLICATION_JSON).content("""
            {"nombre":"Decant","marca":"Casa","tipo":"DECANT","volumenMl":10,"precio":10000,"original":false,"origen":"NACIONAL","paisOrigen":"CO","botellaMadreId":"%s"}
            """.formatted(botella))).andExpect(status().isCreated()).andReturn()).get("id").asText();
        publicar(vendedor,producto);
        mvc.perform(get("/api/tiendas/"+vendedorId)).andExpect(status().isOk()).andExpect(jsonPath("$.nombre").value("Atelier de prueba"));
        mvc.perform(get("/api/tiendas/"+vendedorId+"/reputacion")).andExpect(status().isOk()).andExpect(jsonPath("$.productos").value(1));
        mvc.perform(get("/api/opciones")).andExpect(status().isOk()).andExpect(jsonPath("$.presentaciones").isArray());
        mvc.perform(auth(get("/api/admin/certificados"),cliente)).andExpect(status().isForbidden());
        mvc.perform(auth(get("/api/admin/certificados").param("estado","PENDIENTE"),admin)).andExpect(status().isOk());
    }
}
