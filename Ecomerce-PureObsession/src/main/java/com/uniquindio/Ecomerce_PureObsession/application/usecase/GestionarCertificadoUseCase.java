package com.uniquindio.Ecomerce_PureObsession.application.usecase;

import com.uniquindio.Ecomerce_PureObsession.application.port.in.GestionarCertificadoPort;
import com.uniquindio.Ecomerce_PureObsession.application.port.out.CertificadoRepository;
import com.uniquindio.Ecomerce_PureObsession.application.port.out.CuentaRepository;
import com.uniquindio.Ecomerce_PureObsession.application.port.out.PresentacionRepository;
import com.uniquindio.Ecomerce_PureObsession.domain.entity.Cuenta;
import com.uniquindio.Ecomerce_PureObsession.domain.entity.Presentacion;
import com.uniquindio.Ecomerce_PureObsession.domain.exception.RecursoNoEncontradoException;
import com.uniquindio.Ecomerce_PureObsession.domain.exception.ReglaDominioException;
import com.uniquindio.Ecomerce_PureObsession.domain.valueObject.Rol;
import java.util.UUID;

public final class GestionarCertificadoUseCase implements GestionarCertificadoPort {
    private final PresentacionRepository presentaciones;
    private final CuentaRepository cuentas;
    private final CertificadoRepository certificados;

    public GestionarCertificadoUseCase(PresentacionRepository presentaciones,
            CuentaRepository cuentas, CertificadoRepository certificados) {
        this.presentaciones = presentaciones;
        this.cuentas = cuentas;
        this.certificados = certificados;
    }

    @Override
    public void cargar(String actor, UUID presentacionId, String tipoContenido, byte[] archivo) {
        Presentacion presentacion = AutorizacionPresentacion.propia(
                actor, presentacionId, cuentas, presentaciones);
        if (!presentacion.isOriginal()) {
            throw new ReglaDominioException("El certificado corresponde a una presentación original.");
        }
        validarArchivo(tipoContenido, archivo);
        presentacion.despublicar();
        presentaciones.guardar(presentacion);
        certificados.guardar(presentacionId, tipoContenido, archivo);
    }

    @Override
    public CertificadoRepository.Certificado consultar(String actor, UUID presentacionId) {
        Cuenta cuenta = AutorizacionPresentacion.cuenta(actor, cuentas);
        if (cuenta.getRol() != Rol.ADMIN) {
            AutorizacionPresentacion.propia(actor, presentacionId, cuentas, presentaciones);
        }
        return certificados.buscar(presentacionId)
                .orElseThrow(() -> new RecursoNoEncontradoException("No hay certificado."));
    }

    @Override
    public void revisar(String actor, UUID presentacionId, boolean aprobado) {
        if (AutorizacionPresentacion.cuenta(actor, cuentas).getRol() != Rol.ADMIN) {
            throw new ReglaDominioException("Solo un administrador revisa certificados.");
        }
        Presentacion presentacion = presentaciones.buscarPorId(presentacionId)
                .orElseThrow(() -> new RecursoNoEncontradoException("La presentación no existe."));
        certificados.revisar(presentacionId, aprobado);
        if (!aprobado) {
            presentacion.despublicar();
            presentaciones.guardar(presentacion);
        }
    }

    private void validarArchivo(String tipoContenido, byte[] archivo) {
        if (archivo == null || archivo.length == 0 || archivo.length > 5 * 1024 * 1024) {
            throw new ReglaDominioException("El certificado debe pesar entre 1 byte y 5 MB.");
        }
        boolean pdf = "application/pdf".equals(tipoContenido)
                && comienza(archivo, new byte[]{37, 80, 68, 70, 45});
        boolean png = "image/png".equals(tipoContenido)
                && comienza(archivo, new byte[]{(byte) 137, 80, 78, 71, 13, 10, 26, 10});
        boolean jpg = "image/jpeg".equals(tipoContenido)
                && comienza(archivo, new byte[]{(byte) 255, (byte) 216, (byte) 255});
        if (!pdf && !png && !jpg) {
            throw new ReglaDominioException("Use un archivo PDF, PNG o JPEG válido.");
        }
    }

    private boolean comienza(byte[] archivo, byte[] firma) {
        if (archivo.length < firma.length) {
            return false;
        }
        for (int i = 0; i < firma.length; i++) {
            if (archivo[i] != firma[i]) {
                return false;
            }
        }
        return true;
    }
}
    