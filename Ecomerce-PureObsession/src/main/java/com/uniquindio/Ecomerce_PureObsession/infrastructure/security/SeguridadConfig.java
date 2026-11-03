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
    @Bean JwtDecoder jwtDecoder(SecretKey clave,com.uniquindio.Ecomerce_PureObsession.application.port.out.EstadoTokenPort estados){
        NimbusJwtDecoder decoder=NimbusJwtDecoder.withSecretKey(clave).macAlgorithm(MacAlgorithm.HS256).build();
        decoder.setJwtValidator(new org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator<>(JwtValidators.createDefault(),jwt->{
            Object ver=jwt.getClaims().get("ver");int version=ver instanceof Number n ? n.intValue() : 0;
            int actual=estados.version(jwt.getSubject());
            return actual>=0 && version==actual ? org.springframework.security.oauth2.core.OAuth2TokenValidatorResult.success()
                : org.springframework.security.oauth2.core.OAuth2TokenValidatorResult.failure(new org.springframework.security.oauth2.core.OAuth2Error("invalid_token"));
        }));return decoder;
    }
    @Bean SecurityFilterChain filtros(HttpSecurity http,@org.springframework.beans.factory.annotation.Qualifier("cors") org.springframework.web.cors.CorsConfigurationSource cors) throws Exception {
        AuthenticationEntryPoint sinToken=(req,res,ex)->{res.setStatus(401);res.setContentType("application/json;charset=UTF-8");res.getWriter().write("{\"mensaje\":\"Token ausente, inválido o expirado.\"}");};
        var roles=new JwtGrantedAuthoritiesConverter();
        roles.setAuthoritiesClaimName("rol"); roles.setAuthorityPrefix("ROLE_");
        var conversor=new JwtAuthenticationConverter();
        conversor.setJwtGrantedAuthoritiesConverter(roles);
        // Sin cookies ni sesión, el navegador no envía credenciales solo: CSRF no aplica a una API con Bearer.
        http.csrf(c->c.disable())
            .cors(c->c.configurationSource(cors))
            .sessionManagement(s->s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(a->a
                .requestMatchers(HttpMethod.POST,"/api/auth/registro","/api/auth/entrar").permitAll()
                .requestMatchers(HttpMethod.POST,"/api/auth/recuperacion","/api/auth/restablecer").permitAll()
                .requestMatchers(HttpMethod.POST,"/api/checkout","/api/checkout/cotizacion").permitAll()
                .requestMatchers(HttpMethod.GET,"/api/productos/**","/api/productos","/api/tiendas/**","/api/tiendas","/api/imagenes/**").permitAll()
                .requestMatchers(HttpMethod.GET,"/api/opciones").permitAll()
                .requestMatchers("/api/vendedor/**").hasRole("VENDEDOR")
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET,"/api/pedidos/*/seguimiento").permitAll()
                .requestMatchers(HttpMethod.POST,"/api/pedidos/*/envios/*/recepcion","/api/demo/pedidos/*/pago").permitAll()
                .requestMatchers(HttpMethod.GET,"/v3/api-docs/**","/swagger-ui/**","/swagger-ui.html").permitAll()
                .requestMatchers(HttpMethod.GET,"/api/catalogo","/api/catalogo/**").permitAll()
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
