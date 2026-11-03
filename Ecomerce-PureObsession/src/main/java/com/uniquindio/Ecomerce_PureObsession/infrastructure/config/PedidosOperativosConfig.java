package com.uniquindio.Ecomerce_PureObsession.infrastructure.config;
import com.uniquindio.Ecomerce_PureObsession.application.port.in.ConsultarPedidoPort;
import com.uniquindio.Ecomerce_PureObsession.application.port.out.*;
import com.uniquindio.Ecomerce_PureObsession.application.usecase.GestionPedidosUseCase;
import org.springframework.context.annotation.*;
import org.springframework.scheduling.annotation.*;
@Configuration @EnableScheduling
public class PedidosOperativosConfig {
    @Bean GestionPedidosUseCase gestionPedidos(CuentaRepository cuentas,ConsultarPedidoPort consultas,GestionPedidosRepository gestion) {return new GestionPedidosUseCase(cuentas,consultas,gestion);}
    @Bean CaducidadReservas caducidadReservas(GestionPedidosRepository gestion) {return new CaducidadReservas(gestion);}
    public static class CaducidadReservas {
        private final GestionPedidosRepository gestion;
        public CaducidadReservas(GestionPedidosRepository gestion) {this.gestion=gestion;}
        @Scheduled(fixedDelayString="${app.reservas.intervalo-ms:60000}",initialDelayString="${app.reservas.intervalo-ms:60000}")
        public void ejecutar() {gestion.expirar();}
    }
}
