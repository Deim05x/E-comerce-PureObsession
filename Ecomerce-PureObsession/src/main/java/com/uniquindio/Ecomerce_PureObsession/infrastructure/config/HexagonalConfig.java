package com.uniquindio.Ecomerce_PureObsession.infrastructure.config;
import com.uniquindio.Ecomerce_PureObsession.application.port.in.*;
import com.uniquindio.Ecomerce_PureObsession.application.port.out.*;
import com.uniquindio.Ecomerce_PureObsession.application.usecase.*;
import com.uniquindio.Ecomerce_PureObsession.domain.repository.*;
import org.springframework.context.annotation.*;
import org.springframework.aop.framework.ProxyFactory;
import org.aopalliance.intercept.MethodInterceptor;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
@Configuration
public class HexagonalConfig {
    @SuppressWarnings("unchecked")
    private <T> T transaccional(T objetivo,PlatformTransactionManager manager) {
        ProxyFactory proxy=new ProxyFactory(objetivo);
        TransactionTemplate tx=new TransactionTemplate(manager);
        proxy.addAdvice((MethodInterceptor) invocation->tx.execute(status-> {
            try {return invocation.proceed();}
            catch(RuntimeException|Error e){throw e;}
            catch(Throwable e){throw new IllegalStateException(e);}
        }));
        return (T)proxy.getProxy();
    }
    @Bean RegistrarBotellaMadrePort registrarBotella(BotellaMadreRepository r,PlatformTransactionManager tx) {
        return transaccional(new RegistrarBotellaMadreUseCase(r),tx);
    }
    @Bean CrearDecantPort crearDecant(BotellaMadreRepository r,PlatformTransactionManager tx) {
        return transaccional(new CrearDecantUseCase(r),tx);
    }
    @Bean ConsultarCatalogoPort consultarCatalogo(BotellaMadreRepository r,PlatformTransactionManager tx) {
        return transaccional(new ConsultarCatalogoUseCase(r),tx);
    }
    @Bean EliminarFraganciaPort eliminarFragancia(BotellaMadreRepository r,PedidoRepository pedidos,PlatformTransactionManager tx) {
        return transaccional(new EliminarFraganciaUseCase(r,pedidos),tx);
    }
    @Bean CuentasPort cuentas(CuentaRepository r,PasswordEncoder encoder,PlatformTransactionManager tx) {
        return transaccional(new CuentasUseCase(r,encoder::encode),tx);
    }
    @Bean PresentacionesPort presentaciones(PresentacionRepository p,CuentaRepository c,
            CertificadoRepository cert,BotellaMadreRepository b,PlatformTransactionManager tx) {
        return transaccional(new PresentacionesUseCase(p,c,cert,b),tx);
    }
}

