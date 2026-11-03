package com.uniquindio.Ecomerce_PureObsession.infrastructure.web;
import com.uniquindio.Ecomerce_PureObsession.application.port.out.PerfilRepository;
import com.uniquindio.Ecomerce_PureObsession.application.port.out.PerfilRepository.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import java.security.Principal;
import java.util.*;
@RestController @RequestMapping("/api")
public class PerfilController {
    private final com.uniquindio.Ecomerce_PureObsession.application.usecase.PerfilUseCase perfiles;
    public PerfilController(com.uniquindio.Ecomerce_PureObsession.application.usecase.PerfilUseCase perfiles) {this.perfiles=perfiles;}
    public record CambioClave(String actual,String nueva) {}
    public record Recuperacion(String email) {}
    public record Restablecer(String token,String nueva) {}
    @GetMapping("/perfil") public Perfil perfil(Principal actor) {return perfiles.perfil(actor.getName());}
    @PutMapping("/perfil") public Perfil guardar(Principal actor,@RequestBody DatosPerfil datos) {return perfiles.guardar(actor.getName(),datos);}
    @PutMapping("/perfil/clave") @ResponseStatus(HttpStatus.NO_CONTENT) public void clave(Principal actor,@RequestBody CambioClave datos) {perfiles.cambiarClave(actor.getName(),datos.actual(),datos.nueva());}
    @GetMapping("/perfil/direcciones") public List<Direccion> direcciones(Principal actor) {return perfiles.direcciones(actor.getName());}
    @PostMapping("/perfil/direcciones") @ResponseStatus(HttpStatus.CREATED) public Direccion crear(Principal actor,@RequestBody Direccion datos) {return perfiles.direccion(actor.getName(),null,datos);}
    @PutMapping("/perfil/direcciones/{id}") public Direccion editar(Principal actor,@PathVariable UUID id,@RequestBody Direccion datos) {return perfiles.direccion(actor.getName(),id,datos);}
    @DeleteMapping("/perfil/direcciones/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) public void eliminar(Principal actor,@PathVariable UUID id) {perfiles.eliminarDireccion(actor.getName(),id);}
    @PostMapping("/auth/recuperacion") public Map<String,String> recuperar(@RequestBody Recuperacion datos) {
        String token=perfiles.recuperar(datos.email());Map<String,String> respuesta=new LinkedHashMap<>();respuesta.put("mensaje","Si la cuenta existe, recibira instrucciones para restablecer su clave.");
        if(token!=null) respuesta.put("tokenDemo",token);return respuesta;
    }
    @PostMapping("/auth/restablecer") @ResponseStatus(HttpStatus.NO_CONTENT) public void restablecer(@RequestBody Restablecer datos) {perfiles.restablecer(datos.token(),datos.nueva());}
}
