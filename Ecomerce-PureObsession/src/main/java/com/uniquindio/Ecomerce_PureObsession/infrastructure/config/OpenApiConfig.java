package com.uniquindio.Ecomerce_PureObsession.infrastructure.config;
import io.swagger.v3.oas.models.*;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.*;
import org.springframework.context.annotation.*;
/** Habilita el botón Authorize de Swagger UI con el JWT que devuelve POST /api/auth/entrar. */
@Configuration
public class OpenApiConfig {
    @Bean OpenAPI api() {
        return new OpenAPI().info(new Info().title("Pure Obsession API").version("v1"))
            .components(new Components().addSecuritySchemes("bearerAuth",
                new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT")))
            .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
    }
}
