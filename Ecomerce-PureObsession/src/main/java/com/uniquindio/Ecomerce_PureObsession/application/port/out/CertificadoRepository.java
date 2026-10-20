package com.uniquindio.Ecomerce_PureObsession.application.port.out;
import java.util.*;
import com.uniquindio.Ecomerce_PureObsession.domain.valueObject.EstadoCertificado;
public interface CertificadoRepository {
    void guardar(UUID presentacionId, String tipoContenido, byte[] contenido);
    Optional<Certificado> buscar(UUID presentacionId);
    void revisar(UUID presentacionId, boolean aprobado);
    record Certificado(String tipoContenido, byte[] contenido, EstadoCertificado estado) {}
}

