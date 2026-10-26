package com.uniquindio.Ecomerce_PureObsession.support;

import com.uniquindio.Ecomerce_PureObsession.application.port.out.InventarioVentaPort;
import java.util.UUID;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

/** Configuración exclusiva de test mientras se integra el adaptador real de inventario. */
@TestConfiguration(proxyBeanMethods = false)
public class InventarioVentaTestConfig {
    @Bean InventarioVentaPort inventarioVentaPort() {
        return new InventarioVentaPort() {
            @Override public void reservar(UUID presentacionId, int cantidad) { }
            @Override public void liberar(UUID presentacionId, int cantidad) { }
        };
    }
}
