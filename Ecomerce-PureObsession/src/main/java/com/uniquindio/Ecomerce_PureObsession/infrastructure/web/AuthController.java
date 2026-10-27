package com.uniquindio.Ecomerce_PureObsession.infrastructure.web;
import com.uniquindio.Ecomerce_PureObsession.application.port.in.CuentasPort;
import com.uniquindio.Ecomerce_PureObsession.application.dto.request.*;
import com.uniquindio.Ecomerce_PureObsession.application.dto.response.*;
import com.uniquindio.Ecomerce_PureObsession.infrastructure.security.EmisorTokens;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.*;
import java.security.Principal;
import java.util.*;
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final CuentasPort cuentas;
    private final AuthenticationManager auth;
    private final EmisorTokens tokens;
    public AuthController(CuentasPort c,AuthenticationManager a,EmisorTokens t){cuentas=c;auth=a;tokens=t;}
    @PostMapping("/registro") @ResponseStatus(HttpStatus.CREATED)
    public CuentaResponse registrar(@Valid @RequestBody RegistrarCuentaRequest r){return cuentas.registrar(r);}
    @PostMapping("/entrar")
    public TokenResponse entrar(@Valid @RequestBody EntrarRequest r) {
        return tokens.emitir(auth.authenticate(UsernamePasswordAuthenticationToken.unauthenticated(r.email().trim().toLowerCase(Locale.ROOT),r.clave())));
    }
    @GetMapping("/yo") public CuentaResponse yo(Principal p){return cuentas.consultar(p.getName());}
}
