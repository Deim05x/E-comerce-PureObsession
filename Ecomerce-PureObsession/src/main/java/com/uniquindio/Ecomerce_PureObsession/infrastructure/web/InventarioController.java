package com.uniquindio.Ecomerce_PureObsession.infrastructure.web;
import com.uniquindio.Ecomerce_PureObsession.application.port.in.*;
import com.uniquindio.Ecomerce_PureObsession.application.dto.request.*;
import com.uniquindio.Ecomerce_PureObsession.application.dto.response.*;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import java.util.UUID;
@RestController
@RequestMapping("/api/inventario")
public class InventarioController {
    private final RegistrarBotellaMadrePort registro;
    private final CrearDecantPort decants;
    private final EliminarFraganciaPort eliminar;
    public InventarioController(RegistrarBotellaMadrePort r,CrearDecantPort d,EliminarFraganciaPort e){registro=r;decants=d;eliminar=e;}
    @PostMapping("/botellas") @ResponseStatus(HttpStatus.CREATED)
    public BotellaMadreResponse registrar(@Valid @RequestBody RegistrarBotellaMadreRequest r){return registro.ejecutar(r);}
    @PostMapping("/decants") @ResponseStatus(HttpStatus.CREATED)
    public DecantResponse crear(@Valid @RequestBody CrearDecantRequest r){return decants.ejecutar(r);}
    @DeleteMapping("/fragancias/{id}")
    public EliminarFraganciaResponse eliminar(@PathVariable UUID id){return eliminar.ejecutar(new EliminarFraganciaRequest(id));}
}

