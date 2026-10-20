package com.uniquindio.Ecomerce_PureObsession.infrastructure.web;

import com.uniquindio.Ecomerce_PureObsession.application.dto.request.CrearPedidoRequest;
import com.uniquindio.Ecomerce_PureObsession.application.dto.response.PedidoResponse;
import com.uniquindio.Ecomerce_PureObsession.application.port.in.ComprarPort;
import com.uniquindio.Ecomerce_PureObsession.application.port.in.ConsultarPedidoPort;
import com.uniquindio.Ecomerce_PureObsession.application.port.in.CancelarPedidoPort;
import jakarta.validation.Valid;
import java.net.URI;
import java.security.Principal;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pedidos")
public class PedidosController {
    private final ComprarPort compras;
    private final ConsultarPedidoPort consultas;
    private final CancelarPedidoPort cancelaciones;
    public PedidosController(ComprarPort compras, ConsultarPedidoPort consultas, CancelarPedidoPort cancelaciones) {
        this.compras = compras; this.consultas = consultas; this.cancelaciones = cancelaciones;
    }
    @PostMapping public ResponseEntity<PedidoResponse> crear(Principal actor,
            @Valid @RequestBody CrearPedidoRequest solicitud) {
        PedidoResponse creado = compras.ejecutar(actor == null ? null : actor.getName(), solicitud);
        return ResponseEntity.created(URI.create("/api/pedidos/" + creado.id())).body(creado);
    }
    @GetMapping("/mios") public List<PedidoResponse> mios(Principal actor) {
        return consultas.listarMios(actor == null ? null : actor.getName());
    }
    @GetMapping("/{id}") public PedidoResponse consultar(Principal actor, @PathVariable UUID id,
            @RequestHeader(value = "X-Consulta-Pedido", required = false) String clave) {
        return consultas.consultar(id, actor == null ? null : actor.getName(), clave);
    }
    @PostMapping("/{id}/cancelacion") public PedidoResponse cancelar(Principal actor, @PathVariable UUID id,
            @RequestHeader(value = "X-Consulta-Pedido", required = false) String clave) {
        return cancelaciones.ejecutar(id, actor == null ? null : actor.getName(), clave);
    }
}
