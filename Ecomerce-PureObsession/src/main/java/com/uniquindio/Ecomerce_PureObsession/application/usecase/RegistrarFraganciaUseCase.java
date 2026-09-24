package com.uniquindio.Ecomerce_PureObsession.application.usecase;

import java.util.UUID;

/**
 * Caso de uso para registrar una nueva fragancia antes de publicarla en el catálogo.
 */
public class RegistrarFraganciaUseCase {

    public FraganciaRegistrada registrar(String nombre, String familiaOlfativa, String concentracion) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre de la fragancia es obligatorio.");
        }
        if (familiaOlfativa == null || familiaOlfativa.isBlank()) {
            throw new IllegalArgumentException("La familia olfativa es obligatoria.");
        }
        if (concentracion == null || concentracion.isBlank()) {
            throw new IllegalArgumentException("La concentración es obligatoria.");
        }
        return new FraganciaRegistrada(UUID.randomUUID(), nombre, familiaOlfativa, concentracion);
    }

    public record FraganciaRegistrada(UUID id, String nombre, String familiaOlfativa, String concentracion) {
    }
}
