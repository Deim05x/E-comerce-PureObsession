package com.uniquindio.Ecomerce_PureObsession.domain.valueObject;

import com.uniquindio.Ecomerce_PureObsession.domain.exception.ReglaDominioException;
import java.util.Currency;
import java.util.Locale;

public record Precio(Double precio, String moneda) {
    public Precio {
        if (precio == null || !Double.isFinite(precio) || precio < 0) {
            throw new ReglaDominioException("El precio debe ser un número finito no negativo.");
        }
        if (moneda == null || moneda.isBlank()) {
            throw new ReglaDominioException("La moneda es obligatoria.");
        }
        moneda = moneda.trim().toUpperCase(Locale.ROOT);
        try {
            Currency.getInstance(moneda);
        } catch (IllegalArgumentException ex) {
            throw new ReglaDominioException("La moneda debe ser un código ISO 4217 válido.");
        }
        if (precio == 0.0) precio = 0.0; // normaliza -0.0 para igualdad por valor
    }
}
