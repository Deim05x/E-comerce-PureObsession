package com.uniquindio.Ecomerce_PureObsession.application.usecase;

import java.util.List;

/**
 * Caso de uso para consultar las fragancias publicadas por familia olfativa.
 */
public class ConsultarCatalogoUseCase {

    public List<String> consultarPorFamilia(String familiaOlfativa, List<FraganciaCatalogo> catalogo) {
        if (familiaOlfativa == null || familiaOlfativa.isBlank()) {
            throw new IllegalArgumentException("La familia olfativa es obligatoria para consultar el catálogo.");
        }
        return catalogo.stream()
                .filter(FraganciaCatalogo::publicada)
                .filter(fragancia -> familiaOlfativa.equalsIgnoreCase(fragancia.familiaOlfativa()))
                .map(FraganciaCatalogo::nombre)
                .toList();
    }

    public record FraganciaCatalogo(String nombre, String familiaOlfativa, boolean publicada) {
    }
}
