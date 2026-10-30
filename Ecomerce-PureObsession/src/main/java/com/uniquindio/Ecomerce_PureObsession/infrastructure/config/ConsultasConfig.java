package com.uniquindio.Ecomerce_PureObsession.infrastructure.config;

import com.uniquindio.Ecomerce_PureObsession.application.port.in.*;
import com.uniquindio.Ecomerce_PureObsession.application.port.out.ConsultaCatalogoRepository;
import com.uniquindio.Ecomerce_PureObsession.application.usecase.*;
import org.springframework.context.annotation.*;

@Configuration
public class ConsultasConfig {
    @Bean ConsultarCatalogoPaginadoPort catalogoPaginado(ConsultaCatalogoRepository consultas) {
        return new ConsultarCatalogoPaginadoUseCase(consultas);
    }
    @Bean ConsultarRankingPort ranking(ConsultaCatalogoRepository consultas) {
        return new ConsultarRankingUseCase(consultas);
    }
}
