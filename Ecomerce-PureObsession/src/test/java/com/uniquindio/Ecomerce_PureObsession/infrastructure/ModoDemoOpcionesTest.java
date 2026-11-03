package com.uniquindio.Ecomerce_PureObsession.infrastructure;

import com.uniquindio.Ecomerce_PureObsession.infrastructure.web.ProductosController;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;
import static org.junit.jupiter.api.Assertions.*;

class ModoDemoOpcionesTest {
    @Test void simulacionSoloSeAnunciaConPerfilDemo() {
        MockEnvironment environment = new MockEnvironment();
        ProductosController controller = new ProductosController(null, environment);
        assertEquals(false, controller.opciones().get("modoDemo"));
        environment.setActiveProfiles("demo");
        assertEquals(true, controller.opciones().get("modoDemo"));
        environment.setActiveProfiles("prod");
        assertEquals(false, controller.opciones().get("modoDemo"));
    }
}
