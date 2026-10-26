package com.uniquindio.Ecomerce_PureObsession.infrastructure;
import com.uniquindio.Ecomerce_PureObsession.domain.repository.BotellaMadreRepository;
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
    private final JsonMapper json=JsonMapper.builder().build();

    @Test void deberiaAplicar401403Y201SegunLaSesionYElRol() throws Exception {
        mvc.perform(get("/api/auth/yo")).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/inventario/botellas").with(user("vendedor").roles("VENDEDOR")).with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content(solicitud("Iris",100)))
                .andExpect(status().isForbidden());
        mvc.perform(post("/api/inventario/botellas").with(user("admin").roles("ADMIN")).with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content(solicitud("Iris",100)))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.nombreFragancia").value("Iris"));
    }

    @Test void deberiaRecorrerControllerCasoDeUsoRepositorioYH2SinMocks() throws Exception {
        String cuerpo=mvc.perform(post("/api/inventario/botellas").with(user("admin").roles("ADMIN")).with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content(solicitud("Neroli",120)))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.volumenDisponibleMl").value(120))
                .andReturn().getResponse().getContentAsString();
        UUID id=UUID.fromString(json.readTree(cuerpo).get("botellaMadreId").asText());

        var recuperada=botellas.buscarPorId(id);

        assertTrue(recuperada.isPresent());
        assertEquals("Neroli",recuperada.orElseThrow().getFragancia().getNombre());
        assertEquals(120,recuperada.orElseThrow().getVolumenDisponible().mililitros());
    }

    private static String solicitud(String nombre,int volumen) {
        return """
            {"nombreFragancia":"%s","familiaOlfativa":"FLORAL","concentracion":"EAU_DE_PARFUM",
             "notasSalida":"bergamota","notasCorazon":"iris","notasFondo":"almizcle","volumenMl":%d}
            """.formatted(nombre,volumen);
    }
}
