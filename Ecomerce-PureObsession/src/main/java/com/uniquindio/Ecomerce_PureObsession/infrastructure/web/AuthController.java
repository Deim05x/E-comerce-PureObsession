package com.uniquindio.Ecomerce_PureObsession.infrastructure.web;
import com.uniquindio.Ecomerce_PureObsession.application.port.in.CuentasPort;
import com.uniquindio.Ecomerce_PureObsession.application.dto.request.*;
import com.uniquindio.Ecomerce_PureObsession.application.dto.response.CuentaResponse;
import jakarta.validation.Valid;
import jakarta.servlet.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.csrf.*;
import java.security.Principal;
import java.util.*;
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final CuentasPort cuentas;
    private final AuthenticationManager auth;
    private final SecurityContextRepository contextos;
    private final SessionAuthenticationStrategy sesiones;
    public AuthController(CuentasPort c,AuthenticationManager a,SecurityContextRepository r,SessionAuthenticationStrategy s){
        cuentas=c;auth=a;contextos=r;sesiones=s;
    }
    @GetMapping("/csrf") public Map<String,String> csrf(CsrfToken token) {
        return Map.of("headerName",token.getHeaderName(),"token",token.getToken());
    }
    @PostMapping("/registro") @ResponseStatus(HttpStatus.CREATED)
    public CuentaResponse registrar(@Valid @RequestBody RegistrarCuentaRequest r){return cuentas.registrar(r);}
    @PostMapping("/entrar")
    public CuentaResponse entrar(@Valid @RequestBody EntrarRequest r,HttpServletRequest req,HttpServletResponse res) {
        var autenticacion=auth.authenticate(UsernamePasswordAuthenticationToken.unauthenticated(r.email().trim().toLowerCase(Locale.ROOT),r.clave()));
        sesiones.onAuthentication(autenticacion,req,res);
        var contexto=SecurityContextHolder.createEmptyContext(); contexto.setAuthentication(autenticacion);
        SecurityContextHolder.setContext(contexto); contextos.saveContext(contexto,req,res);
        new HttpSessionCsrfTokenRepository().saveToken(null,req,res);
        return cuentas.consultar(autenticacion.getName());
    }
    @GetMapping("/yo") public CuentaResponse yo(Principal p){return cuentas.consultar(p.getName());}
}

