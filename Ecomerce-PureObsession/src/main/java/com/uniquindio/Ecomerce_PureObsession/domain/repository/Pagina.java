package com.uniquindio.Ecomerce_PureObsession.domain.repository;

import com.uniquindio.Ecomerce_PureObsession.domain.exception.ReglaDominioException;
import java.util.List;
import java.util.function.Function;

/** Contrato de paginacion sin tipos de Spring. Las paginas empiezan en cero. */
public record Pagina<T>(List<T> contenido, long totalElementos, int totalPaginas, int pagina, int tamanio) {
    public Pagina {
        validar(pagina, tamanio);
        if (totalElementos < 0 || totalPaginas != (int) Math.ceil((double) totalElementos / tamanio))
            throw new ReglaDominioException("Los metadatos de pagina no son validos.");
        contenido = List.copyOf(contenido);
    }
    public static void validar(int pagina, int tamanio) {
        if (pagina < 0 || tamanio < 1 || tamanio > 100 || (long) pagina * tamanio > Integer.MAX_VALUE)
            throw new ReglaDominioException("Use pagina >= 0 y tamanio entre 1 y 100, con desplazamiento valido.");
    }
    public <R> Pagina<R> mapear(Function<T,R> mapper) {
        return new Pagina<>(contenido.stream().map(mapper).toList(), totalElementos, totalPaginas, pagina, tamanio);
    }
}
