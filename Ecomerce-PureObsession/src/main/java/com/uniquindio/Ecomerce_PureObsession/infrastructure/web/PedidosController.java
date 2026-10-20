package com.uniquindio.Ecomerce_PureObsession.infrastructure.web;

import com.uniquindio.Ecomerce_PureObsession.application.dto.request.CrearPedidoRequest;
import com.uniquindio.Ecomerce_PureObsession.application.dto.response.PedidoResponse;
import com.uniquindio.Ecomerce_PureObsession.application.port.in.ComprarPort;
import jakarta.validation.Valid;
import java.net.URI;
import java.security.Principal;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pedidos")
public class PedidosController {
    private final ComprarPort compras;
    public PedidosController(ComprarPort compras) { this.compras = compras; }
    @PostMapping public ResponseEntity<PedidoResponse> crear(Principal actor,
            @Valid @RequestBody CrearPedidoRequest solicitud) {
        PedidoResponse creado = compras.ejecutar(actor == null ? null : actor.getName(), solicitud);
        return ResponseEntity.created(URI.create("/api/pedidos/" + creado.id())).body(creado);
    }
}
