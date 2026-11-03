package com.uniquindio.Ecomerce_PureObsession.infrastructure;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.test.web.servlet.*;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.http.MediaType;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest(properties={"spring.datasource.url=jdbc:h2:mem:sin-demo;DB_CLOSE_DELAY=-1","spring.jpa.hibernate.ddl-auto=create-drop","app.admin.email=","app.admin.clave=","app.reservas.intervalo-ms=3600000"})
@SuppressWarnings({"deprecation","removal"})
class ContratoFueraDemoTest {
    @Autowired WebApplicationContext contexto;
    MockMvc mvc;
    @BeforeEach void preparar(){mvc=MockMvcBuilders.webAppContextSetup(contexto).apply(springSecurity()).build();}
    @Test void pagoSimuladoNoExisteFueraDelPerfilDemo() throws Exception {
        assertFalse(contexto.containsBean("pagoDemoController"));
        mvc.perform(post("/api/demo/pedidos/00000000-0000-0000-0000-000000000001/pago")).andExpect(status().isNotFound());
    }
    @Test void recuperacionSinSmtpNoExponeTokensNiFingeEnviarCorreo() throws Exception {
        mvc.perform(post("/api/auth/recuperacion").contentType(MediaType.APPLICATION_JSON).content("{\"email\":\"cualquiera@ejemplo.com\"}"))
                .andExpect(status().isServiceUnavailable()).andExpect(jsonPath("$.tokenDemo").doesNotExist());
    }
}
