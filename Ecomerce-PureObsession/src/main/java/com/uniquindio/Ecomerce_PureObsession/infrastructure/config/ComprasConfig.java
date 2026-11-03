package com.uniquindio.Ecomerce_PureObsession.infrastructure.config;

import com.uniquindio.Ecomerce_PureObsession.application.port.in.*;
import com.uniquindio.Ecomerce_PureObsession.application.port.out.*;
import com.uniquindio.Ecomerce_PureObsession.application.usecase.*;
import com.uniquindio.Ecomerce_PureObsession.domain.repository.PedidoRepository;
import org.springframework.context.annotation.*;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@Configuration
public class ComprasConfig {
    @Bean ComprarPort compras(CuentaRepository cuentas, PresentacionRepository presentaciones, PedidoRepository pedidos,
                              InventarioVentaPort inventario, PlatformTransactionManager transactionManager,
                              @org.springframework.beans.factory.annotation.Value("${app.checkout.envio-por-vendedor:0}") java.math.BigDecimal envio,
                              @org.springframework.beans.factory.annotation.Value("${app.checkout.regalo-por-unidad:0}") java.math.BigDecimal regalo) {
        var casoDeUso = new ComprarUseCase(cuentas, presentaciones, pedidos, inventario,envio,regalo);
        var transaccion = new TransactionTemplate(transactionManager);
        return (actor, solicitud) -> transaccion.execute(status -> casoDeUso.ejecutar(actor, solicitud));
    }

    @Bean ConsultarPedidoPort consultarPedidos(PedidoRepository pedidos, CuentaRepository cuentas) {
        return new ConsultarPedidoUseCase(pedidos, cuentas);
    }

    @Bean CancelarPedidoPort cancelarPedido(PedidoRepository pedidos, CuentaRepository cuentas,
                                            InventarioVentaPort inventario, PlatformTransactionManager transactionManager) {
        var casoDeUso = new CancelarPedidoUseCase(pedidos, cuentas, inventario);
        var transaccion = new TransactionTemplate(transactionManager);
        return (id, actor, clave) -> transaccion.execute(status -> casoDeUso.ejecutar(id, actor, clave));
    }
}
