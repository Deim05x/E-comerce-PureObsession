package com.uniquindio.Ecomerce_PureObsession.domain.exception;

/** Señala un recurso inexistente para convertirlo en respuesta HTTP 404. */
public final class RecursoNoEncontradoException extends RuntimeException {
    public RecursoNoEncontradoException(String mensaje) { super(mensaje); }
}
