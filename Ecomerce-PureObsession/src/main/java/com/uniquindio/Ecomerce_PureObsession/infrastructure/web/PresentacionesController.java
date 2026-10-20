package com.uniquindio.Ecomerce_PureObsession.infrastructure.web;
import com.uniquindio.Ecomerce_PureObsession.application.port.in.PresentacionesPort;
import com.uniquindio.Ecomerce_PureObsession.application.dto.request.*;
import com.uniquindio.Ecomerce_PureObsession.application.dto.response.*;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.*;
import java.util.UUID;
import java.security.Principal;
import java.io.IOException;
@RestController
@RequestMapping("/api/presentaciones")
public class PresentacionesController {
    private final PresentacionesPort presentaciones;
    public PresentacionesController(PresentacionesPort p){presentaciones=p;}
    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    public PresentacionResponse crear(Principal actor,@Valid @RequestBody CrearPresentacionRequest r){
        return presentaciones.crear(actor.getName(),r);
    }
    @PostMapping("/{id}/publicacion")
    public PresentacionResponse publicar(Principal actor,@PathVariable UUID id){return presentaciones.publicar(actor.getName(),id);}
    @PostMapping("/{id}/empaque/validacion")
    public EmpaqueResponse empaque(@PathVariable UUID id,@Valid @RequestBody EmpaqueRequest r){
        return presentaciones.validarEmpaque(id,r.tipo());
    }
    @PostMapping(value="/{id}/certificado",consumes=MediaType.MULTIPART_FORM_DATA_VALUE) @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cargar(Principal actor,@PathVariable UUID id,@RequestPart("archivo") MultipartFile archivo) throws IOException {
        presentaciones.cargarCertificado(actor.getName(),id,archivo.getContentType(),archivo.getBytes());
    }
    @GetMapping("/{id}/certificado")
    public ResponseEntity<byte[]> descargar(Principal actor,@PathVariable UUID id) {
        var c=presentaciones.consultarCertificado(actor.getName(),id);
        return ResponseEntity.ok().contentType(MediaType.parseMediaType(c.tipoContenido()))
            .header(HttpHeaders.CONTENT_DISPOSITION,"attachment; filename=\"certificado\"")
            .header("X-Estado-Certificado",c.estado().name()).body(c.contenido());
    }
    @PostMapping("/{id}/revision") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void revisar(Principal actor,@PathVariable UUID id,@Valid @RequestBody RevisarCertificadoRequest r){
        presentaciones.revisarCertificado(actor.getName(),id,r.aprobado());
    }
}

