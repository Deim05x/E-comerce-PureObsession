package com.uniquindio.Ecomerce_PureObsession.application.usecase;
import com.uniquindio.Ecomerce_PureObsession.application.port.in.CuentasPort;
import com.uniquindio.Ecomerce_PureObsession.application.port.out.*;
import com.uniquindio.Ecomerce_PureObsession.application.dto.request.RegistrarCuentaRequest;
import com.uniquindio.Ecomerce_PureObsession.application.dto.response.CuentaResponse;
import com.uniquindio.Ecomerce_PureObsession.domain.entity.Cuenta;
import com.uniquindio.Ecomerce_PureObsession.domain.valueObject.Rol;
import com.uniquindio.Ecomerce_PureObsession.domain.exception.ReglaDominioException;
import java.util.*;
import java.nio.charset.StandardCharsets;
public class CuentasUseCase implements CuentasPort {
    private final CuentaRepository cuentas;
    private final ClaveEncoder encoder;
    public CuentasUseCase(CuentaRepository cuentas,ClaveEncoder encoder){this.cuentas=cuentas;this.encoder=encoder;}
    public CuentaResponse registrar(RegistrarCuentaRequest r) {
        if(r==null || (r.rol()!=Rol.CLIENTE && r.rol()!=Rol.VENDEDOR))
            throw new ReglaDominioException("Solo puede registrarse como cliente o vendedor.");
        if(r.clave()==null || r.clave().length()<10 || r.clave().getBytes(StandardCharsets.UTF_8).length>72)
            throw new ReglaDominioException("La clave requiere mínimo 10 caracteres y máximo 72 bytes.");
        if(r.email()==null) throw new ReglaDominioException("El correo es obligatorio.");
        String email=r.email().trim().toLowerCase(Locale.ROOT);
        if(cuentas.buscarPorEmail(email).isPresent() || cuentas.existeCedula(r.cedula()))
            throw new ReglaDominioException("El correo o la cédula ya están registrados.");
        Cuenta cuenta=Cuenta.crear(UUID.randomUUID(),r.nombre(),r.cedula(),email,encoder.codificar(r.clave()),r.rol());
        return CuentaResponse.desde(cuentas.guardar(cuenta));
    }
    public CuentaResponse consultar(String email) {
        return CuentaResponse.desde(cuentas.buscarPorEmail(email).orElseThrow(()->new ReglaDominioException("La cuenta no existe.")));
    }
}

