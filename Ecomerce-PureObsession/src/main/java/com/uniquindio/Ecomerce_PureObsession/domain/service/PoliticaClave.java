package com.uniquindio.Ecomerce_PureObsession.domain.service;
import com.uniquindio.Ecomerce_PureObsession.domain.exception.ReglaDominioException;
public final class PoliticaClave {
    private PoliticaClave() {}
    public static void validar(String clave) {
        if(clave==null || clave.length()<10 || clave.getBytes(java.nio.charset.StandardCharsets.UTF_8).length>72)
            throw new ReglaDominioException("La clave requiere minimo 10 caracteres y maximo 72 bytes.");
    }
}
