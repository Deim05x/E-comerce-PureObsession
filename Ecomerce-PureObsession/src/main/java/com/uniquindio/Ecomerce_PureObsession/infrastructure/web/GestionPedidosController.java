package com.uniquindio.Ecomerce_PureObsession.infrastructure.web;
import com.uniquindio.Ecomerce_PureObsession.application.model.Marketplace.Venta;
import com.uniquindio.Ecomerce_PureObsession.application.port.out.GestionPedidosRepository.Detalle;
import com.uniquindio.Ecomerce_PureObsession.application.usecase.GestionPedidosUseCase;
import com.uniquindio.Ecomerce_PureObsession.domain.repository.Pagina;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import java.security.Principal;
import java.util.UUID;
@RestController @RequestMapping("/api")
public class GestionPedidosController {
    private final GestionPedidosUseCase gestion;
    public GestionPedidosController(GestionPedidosUseCase gestion) {this.gestion=gestion;}
    public record Pago(String referencia) {}
    public record Despacho(String transportadora,String guia) {}
    @GetMapping("/pedidos/{id}/seguimiento") public Detalle detalle(Principal actor,@PathVariable UUID id,@RequestHeader(value="X-Consulta-Pedido",required=false) String clave) {return gestion.detalle(id,actor==null ? null : actor.getName(),clave);}
    @PostMapping("/admin/pedidos/{id}/confirmacion") public Detalle confirmar(Principal actor,@PathVariable UUID id,@RequestBody Pago datos) {return gestion.confirmar(id,actor.getName(),datos.referencia());}
    @GetMapping("/admin/pedidos") public Pagina<Detalle> pedidos(Principal actor,@RequestParam(defaultValue="0") int pagina,@RequestParam(defaultValue="20") int tamanio) {return gestion.administrar(actor.getName(),pagina,tamanio);}
    @GetMapping("/vendedor/ventas") public Pagina<Venta> ventas(Principal actor,@RequestParam(defaultValue="0") int pagina,@RequestParam(defaultValue="20") int tamanio) {return gestion.ventas(actor.getName(),pagina,tamanio);}
    @PostMapping("/vendedor/ventas/{id}/envio") @ResponseStatus(HttpStatus.NO_CONTENT) public void enviar(Principal actor,@PathVariable UUID id,@RequestBody Despacho datos) {gestion.enviar(id,actor.getName(),datos.transportadora(),datos.guia());}
    @PostMapping("/pedidos/{id}/envios/{vendedor}/recepcion") public Detalle recibir(Principal actor,@PathVariable UUID id,@PathVariable UUID vendedor,@RequestHeader(value="X-Consulta-Pedido",required=false) String clave) {return gestion.recibir(id,vendedor,actor==null ? null : actor.getName(),clave);}
}
