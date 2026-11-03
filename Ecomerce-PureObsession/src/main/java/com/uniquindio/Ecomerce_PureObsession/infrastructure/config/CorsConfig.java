package com.uniquindio.Ecomerce_PureObsession.infrastructure.config;
import org.springframework.context.annotation.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.cors.*;
import java.util.*;
@Configuration
public class CorsConfig {
    @Bean CorsConfigurationSource cors(@Value("${app.cors.origenes:http://localhost:5173,http://localhost:3000}") String origenes) {
        CorsConfiguration c=new CorsConfiguration();
        c.setAllowedOrigins(Arrays.stream(origenes.split(",")).map(String::trim).filter(s->!s.isBlank()).toList());
        c.setAllowedMethods(List.of("GET","POST","PUT","PATCH","DELETE","OPTIONS"));
        c.setAllowedHeaders(List.of("Authorization","Content-Type","X-Consulta-Pedido","Idempotency-Key"));
        c.setExposedHeaders(List.of("Location","X-Estado-Certificado"));c.setAllowCredentials(false);c.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource source=new UrlBasedCorsConfigurationSource();source.registerCorsConfiguration("/api/**",c);return source;
    }
}
