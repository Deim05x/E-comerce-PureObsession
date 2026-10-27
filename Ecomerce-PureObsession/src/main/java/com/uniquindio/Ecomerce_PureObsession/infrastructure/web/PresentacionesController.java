package com.uniquindio.Ecomerce_PureObsession.infrastructure.web;

import com.uniquindio.Ecomerce_PureObsession.application.dto.request.CrearPresentacionRequest;
import com.uniquindio.Ecomerce_PureObsession.application.dto.request.EmpaqueRequest;
import com.uniquindio.Ecomerce_PureObsession.application.dto.request.RevisarCertificadoRequest;
import com.uniquindio.Ecomerce_PureObsession.application.dto.response.EmpaqueResponse;
import com.uniquindio.Ecomerce_PureObsession.application.dto.response.PresentacionResponse;
import com.uniquindio.Ecomerce_PureObsession.application.port.in.ConsultarPresentacionesPort;
import com.uniquindio.Ecomerce_PureObsession.application.port.in.CrearPresentacionPort;
import com.uniquindio.Ecomerce_PureObsession.application.port.in.GestionarCertificadoPort;
import com.uniquindio.Ecomerce_PureObsession.application.port.in.PublicarPresentacionPort;
import com.uniquindio.Ecomerce_PureObsession.infrastructure.web.mapper.PresentacionWebMapper;
import jakarta.validation.Valid;
import java.io.IOException;
import java.security.Principal;
import java.util.UUID;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/presentaciones")
public class PresentacionesController {
    private final CrearPresentacionPort crearPresentacion;
    private final PublicarPresentacionPort publicarPresentacion;
    private final ConsultarPresentacionesPort consultarPresentaciones;
    private final GestionarCertificadoPort certificados;
    private final PresentacionWebMapper mapper;

    public PresentacionesController(CrearPresentacionPort crearPresentacion,
            PublicarPresentacionPort publicarPresentacion,
            ConsultarPresentacionesPort consultarPresentaciones,
            GestionarCertificadoPort certificados,
            PresentacionWebMapper mapper) {
        this.crearPresentacion = crearPresentacion;
        this.publicarPresentacion = publicarPresentacion;
        this.consultarPresentaciones = consultarPresentaciones;
        this.certificados = certificados;
        this.mapper = mapper;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PresentacionResponse crear(Principal actor,
            @Valid @RequestBody CrearPresentacionRequest request) {
        return mapper.aRespuesta(crearPresentacion.ejecutar(
                actor.getName(), mapper.aComando(request)));
    }

    @PostMapping("/{id}/publicacion")
    public PresentacionResponse publicar(Principal actor, @PathVariable UUID id) {
        return mapper.aRespuesta(publicarPresentacion.ejecutar(actor.getName(), id));
    }

    @PostMapping("/{id}/empaque/validacion")
    public EmpaqueResponse empaque(@PathVariable UUID id,
            @Valid @RequestBody EmpaqueRequest request) {
        consultarPresentaciones.validarEmpaque(id, request.tipo());
        return new EmpaqueResponse(id, request.tipo(), true);
    }

    @PostMapping(value = "/{id}/certificado", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cargar(Principal actor, @PathVariable UUID id,
            @RequestPart("archivo") MultipartFile archivo) throws IOException {
        certificados.cargar(actor.getName(), id, archivo.getContentType(), archivo.getBytes());
    }

    @GetMapping("/{id}/certificado")
    public ResponseEntity<byte[]> descargar(Principal actor, @PathVariable UUID id) {
        var certificado = certificados.consultar(actor.getName(), id);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(certificado.tipoContenido()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"certificado\"")
                .header("X-Estado-Certificado", certificado.estado().name())
                .body(certificado.contenido());
    }

    @PostMapping("/{id}/revision")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void revisar(Principal actor, @PathVariable UUID id,
            @Valid @RequestBody RevisarCertificadoRequest request) {
        certificados.revisar(actor.getName(), id, request.aprobado());
    }
}
