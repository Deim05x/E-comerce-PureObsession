package com.uniquindio.Ecomerce_PureObsession.application.port.in;
import com.uniquindio.Ecomerce_PureObsession.application.dto.request.CrearPresentacionRequest;
import com.uniquindio.Ecomerce_PureObsession.application.dto.response.PresentacionResponse;
import com.uniquindio.Ecomerce_PureObsession.application.port.out.CertificadoRepository.Certificado;
import java.util.*;
public interface PresentacionesPort {
    PresentacionResponse crear(String actor,CrearPresentacionRequest request);
    PresentacionResponse publicar(String actor,UUID id);
    List<PresentacionResponse> catalogo();
    com.uniquindio.Ecomerce_PureObsession.application.dto.response.EmpaqueResponse validarEmpaque(UUID id,com.uniquindio.Ecomerce_PureObsession.domain.valueObject.TipoEmpaque tipo);
    void cargarCertificado(String actor,UUID id,String tipo,byte[] archivo);
    Certificado consultarCertificado(String actor,UUID id);
    void revisarCertificado(String actor,UUID id,boolean aprobado);
}

