package com.uniquindio.Ecomerce_PureObsession.infrastructure;
import com.uniquindio.Ecomerce_PureObsession.application.port.out.CuentaRepository;
import com.uniquindio.Ecomerce_PureObsession.domain.entity.Cuenta;
import com.uniquindio.Ecomerce_PureObsession.domain.repository.BotellaMadreRepository;
import com.uniquindio.Ecomerce_PureObsession.domain.valueObject.Rol;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.json.JsonMapper;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties={"spring.datasource.url=jdbc:h2:mem:http-avanzadas;DB_CLOSE_DELAY=-1","spring.jpa.hibernate.ddl-auto=create-drop","app.admin.email=","app.admin.clave="})
@AutoConfigureMockMvc
@Transactional
@Rollback
class PruebasHttpAvanzadasTest {
    @Autowired MockMvc mvc;
    @Autowired BotellaMadreRepository botellas;
    @Autowired CuentaRepository cuentas;
    @Autowired PasswordEncoder encoder;
    private final JsonMapper json=JsonMapper.builder().build();

    @Test void deberiaAplicar401403Y201SegunElTokenJwtYElRol() throws Exception {
        mvc.perform(get("/api/auth/yo")).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/inventario/botellas").contentType(MediaType.APPLICATION_JSON).content(solicitud("Iris",100)))
                .andExpect(status().isUnauthorized());
        mvc.perform(post("/api/inventario/botellas").header("Authorization",bearer(Rol.VENDEDOR))
                .contentType(MediaType.APPLICATION_JSON).content(solicitud("Iris",100)))
                .andExpect(status().isForbidden());
        mvc.perform(post("/api/inventario/botellas").header("Authorization",bearer(Rol.ADMIN))
                .contentType(MediaType.APPLICATION_JSON).content(solicitud("Iris",100)))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.nombreFragancia").value("Iris"));
    }

    @Test void deberiaRecorrerControllerCasoDeUsoRepositorioYH2SinMocks() throws Exception {
        String cuerpo=mvc.perform(post("/api/inventario/botellas").with(user("admin").roles("ADMIN"))
                .contentType(MediaType.APPLICATION_JSON).content(solicitud("Neroli",120)))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.volumenDisponibleMl").value(120))
                .andReturn().getResponse().getContentAsString();
        UUID id=UUID.fromString(json.readTree(cuerpo).get("botellaMadreId").asText());

        var recuperada=botellas.buscarPorId(id);

        assertTrue(recuperada.isPresent());
        assertEquals("Neroli",recuperada.orElseThrow().getFragancia().getNombre());
        assertEquals(120,recuperada.orElseThrow().getVolumenDisponible().mililitros());
    }

    /** Token real: la cuenta se guarda con BCrypt y el JWT sale de POST /api/auth/entrar. */
    private String bearer(Rol rol) throws Exception {
        String email=UUID.randomUUID()+"@test.com";
        String cedula=Long.toString(Math.abs(UUID.randomUUID().getMostSignificantBits()%1000000000000L)+100000);
        cuentas.guardar(Cuenta.crear(UUID.randomUUID(),"Persona",cedula,email,encoder.encode("ClavePrueba2026"),rol));
        String respuesta=mvc.perform(post("/api/auth/entrar").contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\""+email+"\",\"clave\":\"ClavePrueba2026\"}"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return "Bearer "+json.readTree(respuesta).get("token").asText();
    }

    private static String solicitud(String nombre,int volumen) {
        return """
            {"nombreFragancia":"%s","familiaOlfativa":"FLORAL","concentracion":"EAU_DE_PARFUM",
             "notasSalida":"bergamota","notasCorazon":"iris","notasFondo":"almizcle","volumenMl":%d}
            """.formatted(nombre,volumen);
    }
}
