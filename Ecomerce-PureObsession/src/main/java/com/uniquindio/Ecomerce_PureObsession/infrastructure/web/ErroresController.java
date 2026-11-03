package com.uniquindio.Ecomerce_PureObsession.infrastructure.web;
import com.uniquindio.Ecomerce_PureObsession.domain.exception.ReglaDominioException;
import com.uniquindio.Ecomerce_PureObsession.domain.exception.RecursoNoEncontradoException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.*;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.AuthenticationException;
import java.util.*;
@RestControllerAdvice
public class ErroresController {
    @ExceptionHandler(com.uniquindio.Ecomerce_PureObsession.domain.exception.ConflictoException.class)
    public ResponseEntity<ErrorResponse> conflicto(Exception e){return error(409,e.getMessage());}
    @ExceptionHandler(com.uniquindio.Ecomerce_PureObsession.domain.exception.AccesoProhibidoException.class)
    public ResponseEntity<ErrorResponse> prohibido(Exception e){return error(403,e.getMessage());}
    @ExceptionHandler(com.uniquindio.Ecomerce_PureObsession.domain.exception.ServicioNoDisponibleException.class)
    public ResponseEntity<ErrorResponse> noDisponible(Exception e){return error(503,"El servicio no esta disponible temporalmente.");}
    @ExceptionHandler({org.springframework.web.bind.MissingServletRequestParameterException.class,org.springframework.web.multipart.support.MissingServletRequestPartException.class})
    public ResponseEntity<ErrorResponse> parametro(Exception e){return error(400,"Faltan datos obligatorios.");}
    public record ErrorResponse(String mensaje,Map<String,String> campos) {}
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> validacion(MethodArgumentNotValidException e) {
        Map<String,String> campos=new LinkedHashMap<>();
        e.getBindingResult().getFieldErrors().forEach(f->campos.putIfAbsent(f.getField(),f.getDefaultMessage()));
        return ResponseEntity.badRequest().body(new ErrorResponse("Revise los datos enviados.",campos));
    }
    @ExceptionHandler(ReglaDominioException.class)
    public ResponseEntity<ErrorResponse> dominio(ReglaDominioException e){return error(422,e.getMessage());}
    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<ErrorResponse> noEncontrado(RecursoNoEncontradoException e){return error(404,e.getMessage());}
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> auth(AuthenticationException e){return error(401,"Credenciales inválidas.");}
    @ExceptionHandler({HttpMessageNotReadableException.class,MethodArgumentTypeMismatchException.class})
    public ResponseEntity<ErrorResponse> formato(Exception e){return error(400,"El formato de los datos es inválido.");}
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> duplicado(Exception e){return error(409,"Los datos ya existen o incumplen una restricción.");}
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> archivo(Exception e){return error(413,"El certificado supera 5 MB.");}
    private ResponseEntity<ErrorResponse> error(int status,String mensaje){
        return ResponseEntity.status(status).body(new ErrorResponse(mensaje,Map.of()));
    }
}

