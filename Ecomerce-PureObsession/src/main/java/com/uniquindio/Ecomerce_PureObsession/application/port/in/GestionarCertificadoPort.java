package com.uniquindio.Ecomerce_PureObsession.application.port.in;

import com.uniquindio.Ecomerce_PureObsession.application.port.out.CertificadoRepository.Certificado;
import java.util.UUID;

public interface GestionarCertificadoPort {
    void cargar(String actor, UUID presentacionId, String tipoContenido, byte[] archivo);
    Certificado consultar(String actor, UUID presentacionId);
    void revisar(String actor, UUID presentacionId, boolean aprobado);
}
