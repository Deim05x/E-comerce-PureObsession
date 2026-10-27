package com.uniquindio.Ecomerce_PureObsession.infrastructure.security;
import com.uniquindio.Ecomerce_PureObsession.application.port.out.CuentaRepository;
import com.uniquindio.Ecomerce_PureObsession.domain.entity.Cuenta;
import com.uniquindio.Ecomerce_PureObsession.domain.valueObject.Rol;
import org.springframework.context.annotation.*;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.*;
import org.springframework.security.web.authentication.session.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.authentication.*;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import java.util.*;
@Configuration
public class SeguridadConfig {
    @Bean PasswordEncoder passwordEncoder(){return new BCryptPasswordEncoder();}
    @Bean UserDetailsService usuarios(CuentaRepository cuentas) {
        return email->cuentas.buscarPorEmail(email.trim().toLowerCase(Locale.ROOT))
            .map(c->User.withUsername(c.getEmail()).password(c.getClaveHash()).roles(c.getRol().name()).build())
            .orElseThrow(()->new UsernameNotFoundException("Credenciales inválidas."));
    }
    @Bean AuthenticationManager autenticador(UserDetailsService usuarios,PasswordEncoder encoder) {
        DaoAuthenticationProvider provider=new DaoAuthenticationProvider(usuarios);
        provider.setPasswordEncoder(encoder);
        return new ProviderManager(provider);
    }
    @Bean SecurityContextRepository contextos(){return new HttpSessionSecurityContextRepository();}
    @Bean SessionAuthenticationStrategy sesiones(){return new ChangeSessionIdAuthenticationStrategy();}
    @Bean SecurityFilterChain filtros(HttpSecurity http,SecurityContextRepository contextos) throws Exception {
        http.securityContext(c->c.securityContextRepository(contextos))
            .authorizeHttpRequests(a->a
                .requestMatchers("/api/auth/csrf","/api/auth/registro","/api/auth/entrar").permitAll()
                .requestMatchers(HttpMethod.GET,"/api/catalogo","/api/catalogo/fragancias").permitAll()
                // mios antes del comodín: /api/pedidos/* también coincide con /mios
                .requestMatchers(HttpMethod.GET,"/api/pedidos/mios").authenticated()
                .requestMatchers(HttpMethod.GET,"/api/pedidos/*").permitAll()
                .requestMatchers(HttpMethod.POST,"/api/pedidos","/api/pedidos/*/cancelacion").permitAll()
                .requestMatchers("/api/inventario/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST,"/api/presentaciones/*/revision").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST,"/api/presentaciones","/api/presentaciones/*/publicacion","/api/presentaciones/*/certificado").hasRole("VENDEDOR")
                .anyRequest().authenticated())
            .exceptionHandling(e->e
                .authenticationEntryPoint((req,res,ex)->{res.setStatus(401);res.setContentType("application/json");res.getWriter().write("{\"mensaje\":\"Inicie sesión.\"}");})
                .accessDeniedHandler((req,res,ex)->{res.setStatus(403);res.setContentType("application/json");res.getWriter().write("{\"mensaje\":\"Acceso denegado o token CSRF inválido.\"}");}))
            .logout(l->l.logoutUrl("/api/auth/salir").logoutSuccessHandler((req,res,auth)->res.setStatus(204)));
        return http.build();
    }
    @Bean ApplicationRunner administradorInicial(CuentaRepository cuentas,PasswordEncoder encoder,Environment env) {
        return args->{
            String email=env.getProperty("app.admin.email","");
            String clave=env.getProperty("app.admin.clave","");
            if(email.isBlank() && clave.isBlank()) return;
            if(clave.length()<10 || clave.getBytes(java.nio.charset.StandardCharsets.UTF_8).length>72)
                throw new IllegalArgumentException("Configure una clave de administrador de mínimo 10 caracteres y máximo 72 bytes.");
            email=email.trim().toLowerCase(Locale.ROOT);
            if(cuentas.buscarPorEmail(email).isEmpty())
                cuentas.guardar(Cuenta.crear(UUID.randomUUID(),"Administrador",env.getRequiredProperty("app.admin.cedula"),email,encoder.encode(clave),Rol.ADMIN));
        };
    }
}

