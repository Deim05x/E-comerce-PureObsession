package com.uniquindio.Ecomerce_PureObsession.infrastructure.web;
import com.uniquindio.Ecomerce_PureObsession.application.port.out.GestionPedidosRepository.Detalle;
import com.uniquindio.Ecomerce_PureObsession.application.usecase.GestionPedidosUseCase;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.util.UUID;
@Profile("demo") @RestController
public class PagoDemoController {
    private final GestionPedidosUseCase gestion;
    public PagoDemoController(GestionPedidosUseCase gestion) {this.gestion=gestion;}
    @PostMapping("/api/demo/pedidos/{id}/pago") public Detalle simular(Principal actor,@PathVariable UUID id,@RequestHeader(value="X-Consulta-Pedido",required=false) String clave) {return gestion.simular(id,actor==null ? null : actor.getName(),clave);}
}
