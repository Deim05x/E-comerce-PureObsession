package com.uniquindio.Ecomerce_PureObsession.infrastructure.config;
import com.uniquindio.Ecomerce_PureObsession.application.port.out.*;
import com.uniquindio.Ecomerce_PureObsession.application.usecase.MarketplaceUseCase;
import org.springframework.context.annotation.*;
@Configuration
public class MarketplaceConfig {
    @Bean com.uniquindio.Ecomerce_PureObsession.application.usecase.CheckoutUseCase checkout(
            com.uniquindio.Ecomerce_PureObsession.application.port.in.ComprarPort compras,MarketplaceRepository productos,CheckoutRegistroPort registro,
            @org.springframework.beans.factory.annotation.Value("${app.checkout.envio-por-vendedor:0}") java.math.BigDecimal envio,
            @org.springframework.beans.factory.annotation.Value("${app.checkout.regalo-por-unidad:0}") java.math.BigDecimal regalo) {
        return new com.uniquindio.Ecomerce_PureObsession.application.usecase.CheckoutUseCase(compras,productos,registro,envio,regalo);
    }
    @Bean com.uniquindio.Ecomerce_PureObsession.application.usecase.PerfilUseCase perfil(PerfilRepository repository) {return new com.uniquindio.Ecomerce_PureObsession.application.usecase.PerfilUseCase(repository);}
    @Bean MarketplaceUseCase marketplace(CuentaRepository cuentas,MarketplaceRepository repository) {return new MarketplaceUseCase(cuentas,repository);}
}
