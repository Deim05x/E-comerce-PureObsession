package com.uniquindio.Ecomerce_PureObsession.infrastructure.web;
import com.uniquindio.Ecomerce_PureObsession.application.dto.request.*;
import com.uniquindio.Ecomerce_PureObsession.application.dto.response.PedidoResponse;
import com.uniquindio.Ecomerce_PureObsession.application.usecase.CheckoutUseCase;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.*;
@RestController @RequestMapping("/api/checkout")
public class CheckoutController {
    private final CheckoutUseCase checkout;
    public CheckoutController(CheckoutUseCase checkout){this.checkout=checkout;}
    public record Items(List<@Valid ItemCheckoutRequest> items) {}
    @PostMapping("/cotizacion") public CheckoutUseCase.Cotizacion cotizar(@Valid @RequestBody Items datos) {return checkout.cotizar(datos.items());}
    @PostMapping public ResponseEntity<PedidoResponse> comprar(Principal actor,@Valid @RequestBody CrearPedidoRequest datos,
            @RequestHeader(value="Idempotency-Key",required=false) String clave) {
        PedidoResponse pedido=checkout.comprar(actor==null ? null : actor.getName(),datos,clave);
        return ResponseEntity.created(java.net.URI.create("/api/pedidos/"+pedido.id())).body(pedido);
    }
}
