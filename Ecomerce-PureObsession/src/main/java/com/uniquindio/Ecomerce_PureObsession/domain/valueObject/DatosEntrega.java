package com.uniquindio.Ecomerce_PureObsession.domain.valueObject;

import com.uniquindio.Ecomerce_PureObsession.domain.exception.ReglaDominioException;
import java.util.Locale;

/** Instantánea de contacto y dirección para compra registrada o invitada. */
public record DatosEntrega(String nombre, String correo, String telefono, String direccion) {
    public DatosEntrega {
        if (nombre == null || nombre.isBlank() || nombre.trim().length() > 120)
            throw new ReglaDominioException("El nombre de entrega es obligatorio (máximo 120 caracteres).");
        if (correo == null || !correo.trim().matches("[^\\s@]+@[^\\s@]+\\.[^\\s@]+"))
            throw new ReglaDominioException("El correo de entrega no es válido.");
        if (telefono == null || !telefono.trim().matches("[0-9+ ()-]{7,22}"))
            throw new ReglaDominioException("El teléfono de entrega no es válido.");
        if (direccion == null || direccion.isBlank() || direccion.trim().length() > 350)
            throw new ReglaDominioException("La dirección es obligatoria (máximo 350 caracteres).");
        nombre = nombre.trim();
        correo = correo.trim().toLowerCase(Locale.ROOT);
        telefono = telefono.trim();
        direccion = direccion.trim();
    }
}
