package com.uniquindio.Ecomerce_PureObsession.infrastructure.security;
import com.uniquindio.Ecomerce_PureObsession.application.port.out.CuentaRepository;
import com.uniquindio.Ecomerce_PureObsession.domain.entity.Cuenta;
import com.uniquindio.Ecomerce_PureObsession.domain.valueObject.Rol;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.springframework.context.annotation.*;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.authentication.*;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.server.resource.authentication.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
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
    @Bean SecretKey claveJwt(@Value("${app.jwt.secreto:}") String secreto) {
        byte[] bytes=secreto.getBytes(StandardCharsets.UTF_8);
        if(secreto.isBlank()) new SecureRandom().nextBytes(bytes=new byte[32]);
        else if(bytes.length<32) throw new IllegalArgumentException("app.jwt.secreto requiere mínimo 32 bytes.");
        return new SecretKeySpec(bytes,"HmacSHA256");
    }
    @Bean JwtEncoder jwtEncoder(SecretKey clave){return new NimbusJwtEncoder(new ImmutableSecret<>(clave));}
    @Bean JwtDecoder jwtDecoder(SecretKey clave){return NimbusJwtDecoder.withSecretKey(clave).macAlgorithm(MacAlgorithm.HS256).build();}
    @Bean SecurityFilterChain filtros(HttpSecurity http) throws Exception {
        AuthenticationEntryPoint sinToken=(req,res,ex)->{res.setStatus(401);res.setContentType("application/json;charset=UTF-8");res.getWriter().write("{\"mensaje\":\"Token ausente, inválido o expirado.\"}");};
        var roles=new JwtGrantedAuthoritiesConverter();
        roles.setAuthoritiesClaimName("rol"); roles.setAuthorityPrefix("ROLE_");
        var conversor=new JwtAuthenticationConverter();
        conversor.setJwtGrantedAuthoritiesConverter(roles);
        // Sin cookies ni sesión, el navegador no envía credenciales solo: CSRF no aplica a una API con Bearer.
        http.csrf(c->c.disable())
            .sessionManagement(s->s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(a->a
                .requestMatchers(HttpMethod.POST,"/api/auth/registro","/api/auth/entrar").permitAll()
                .requestMatchers(HttpMethod.GET,"/v3/api-docs/**","/swagger-ui/**","/swagger-ui.html").permitAll()
                .requestMatchers(HttpMethod.GET,"/api/catalogo","/api/catalogo/fragancias").permitAll()
                // mios antes del comodín: /api/pedidos/* también coincide con /mios
                .requestMatchers(HttpMethod.GET,"/api/pedidos/mios").authenticated()
                .requestMatchers(HttpMethod.GET,"/api/pedidos/*").permitAll()
                .requestMatchers(HttpMethod.POST,"/api/pedidos","/api/pedidos/*/cancelacion").permitAll()
                .requestMatchers("/api/inventario/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST,"/api/presentaciones/*/revision").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST,"/api/presentaciones","/api/presentaciones/*/publicacion","/api/presentaciones/*/certificado").hasRole("VENDEDOR")
                .anyRequest().authenticated())
            .oauth2ResourceServer(o->o.authenticationEntryPoint(sinToken).jwt(j->j.jwtAuthenticationConverter(conversor)))
            .exceptionHandling(e->e
                .authenticationEntryPoint(sinToken)
                .accessDeniedHandler((req,res,ex)->{res.setStatus(403);res.setContentType("application/json;charset=UTF-8");res.getWriter().write("{\"mensaje\":\"Acceso denegado.\"}");}));
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
