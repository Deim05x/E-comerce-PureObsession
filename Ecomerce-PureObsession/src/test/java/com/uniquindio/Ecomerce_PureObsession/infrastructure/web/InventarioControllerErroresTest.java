package com.uniquindio.Ecomerce_PureObsession.infrastructure.web;

import com.uniquindio.Ecomerce_PureObsession.application.port.in.*;
import com.uniquindio.Ecomerce_PureObsession.domain.exception.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.*;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(InventarioController.class)
@AutoConfigureMockMvc(addFilters=false)
class InventarioControllerErroresTest {
    @Autowired MockMvc mvc;
    @MockitoBean RegistrarBotellaMadrePort registro;
    @MockitoBean CrearDecantPort decants;
    @MockitoBean EliminarFraganciaPort eliminar;

    @Test void deberiaResponder404CuandoElCasoDeUsoNoEncuentraElRecurso() throws Exception {
        when(registro.ejecutar(any())).thenThrow(new RecursoNoEncontradoException("La botella no existe."));
        mvc.perform(post("/api/inventario/botellas").contentType(MediaType.APPLICATION_JSON).content(solicitud()))
                .andExpect(status().isNotFound());
    }

    @Test void deberiaResponder422CuandoSeRompeUnaReglaDeNegocio() throws Exception {
        when(registro.ejecutar(any())).thenThrow(new ReglaDominioException("El volumen no alcanza."));
        mvc.perform(post("/api/inventario/botellas").contentType(MediaType.APPLICATION_JSON).content(solicitud()))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test void noDeberiaInvocarElCasoDeUsoConDatosInvalidos() throws Exception {
        mvc.perform(post("/api/inventario/botellas").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
        verify(registro,never()).ejecutar(any());
    }

    private static String solicitud() {
        return "{\"nombreFragancia\":\"Iris\",\"familiaOlfativa\":\"FLORAL\",\"concentracion\":\"EAU_DE_PARFUM\",\"notasSalida\":\"a\",\"notasCorazon\":\"b\",\"notasFondo\":\"c\",\"volumenMl\":100}";
    }
}
