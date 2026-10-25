package com.uniquindio.Ecomerce_PureObsession.application.port.out;

import java.util.UUID;

/** Contrato compartido: Brayan debe implementar stock real, bloqueo y transacción compartida. */
public interface InventarioVentaPort {
    void reservar(UUID presentacionId, int cantidad);
    void liberar(UUID presentacionId, int cantidad);
}
