package com.uniquindio.Ecomerce_PureObsession.infrastructure.web;

import com.uniquindio.Ecomerce_PureObsession.application.model.Marketplace.*;
import com.uniquindio.Ecomerce_PureObsession.application.usecase.MarketplaceUseCase;
import com.uniquindio.Ecomerce_PureObsession.domain.repository.Pagina;
import com.uniquindio.Ecomerce_PureObsession.domain.valueObject.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.*;
import java.security.Principal;
import java.util.*;

@RestController @RequestMapping("/api")
public class ProductosController {
    private final MarketplaceUseCase marketplace;
    public ProductosController(MarketplaceUseCase marketplace) {this.marketplace=marketplace;}
    public record Stock(int disponible) {}
    public record ResenaRequest(int puntuacion,String comentario) {}
    @GetMapping("/opciones") public Map<String,Object> opciones() {
        return Map.of("familias",FamiliaOlfativa.values(),"concentraciones",Concentracion.values(),"presentaciones",TipoPresentacion.values(),
                "origenes",OrigenProducto.values(),"empaques",TipoEmpaque.values(),"categorias",List.of("NICHO","DISENADOR","ARABE"));
    }
    @GetMapping("/productos") public Pagina<Producto> productos(@RequestParam(required=false) String texto,
            @RequestParam(required=false) FamiliaOlfativa familia,@RequestParam(required=false) TipoPresentacion tipo,
            @RequestParam(required=false) String categoria,@RequestParam(required=false) java.math.BigDecimal minimo,
            @RequestParam(required=false) java.math.BigDecimal maximo,@RequestParam(required=false) UUID vendedorId,
            @RequestParam(required=false) UUID fraganciaId,@RequestParam(defaultValue="nombre") String orden,
            @RequestParam(defaultValue="0") int pagina,@RequestParam(defaultValue="20") int tamanio) {
        return marketplace.productos(new Filtro(texto,familia,tipo,categoria,minimo,maximo,vendedorId,fraganciaId,orden,pagina,tamanio));
    }
    @GetMapping("/productos/{id}") public Producto producto(@PathVariable UUID id) {return marketplace.producto(id);}
    @GetMapping("/productos/{id}/resenas") public List<Resena> resenas(@PathVariable UUID id) {return marketplace.resenas(id);}
    @PutMapping("/productos/{id}/resena") public Resena resena(Principal actor,@PathVariable UUID id,@RequestBody ResenaRequest r) {
        return marketplace.resena(actor.getName(),id,r.puntuacion(),r.comentario());
    }
    @GetMapping("/tiendas") public Pagina<Tienda> tiendas(@RequestParam(defaultValue="0") int pagina,@RequestParam(defaultValue="20") int tamanio) {return marketplace.tiendas(pagina,tamanio);}
    @GetMapping("/tiendas/{id}") public Tienda tienda(@PathVariable UUID id) {return marketplace.tienda(id);}
    @GetMapping("/tiendas/{id}/reputacion") public Reputacion reputacion(@PathVariable UUID id) {return marketplace.reputacion(id);}
    @PostMapping("/vendedor/fragancias") @ResponseStatus(HttpStatus.CREATED) public FraganciaInfo fragancia(Principal actor,@RequestBody FraganciaNueva datos) {return marketplace.crearFragancia(actor.getName(),datos);}
    @GetMapping("/admin/certificados") public Pagina<CertificadoInfo> certificados(Principal actor,@RequestParam(required=false) EstadoCertificado estado,@RequestParam(defaultValue="0") int pagina,@RequestParam(defaultValue="20") int tamanio) {return marketplace.certificados(actor.getName(),estado,pagina,tamanio);}
    @GetMapping("/vendedor/productos") public Pagina<Producto> propios(Principal actor,@RequestParam(defaultValue="0") int pagina,@RequestParam(defaultValue="20") int tamanio) {return marketplace.propios(actor.getName(),pagina,tamanio);}
    @GetMapping("/vendedor/productos/{id}") public Producto propio(Principal actor,@PathVariable UUID id) {return marketplace.propio(actor.getName(),id);}
    @PostMapping("/vendedor/productos") @ResponseStatus(HttpStatus.CREATED) public Producto crear(Principal actor,@RequestBody ProductoNuevo datos) {return marketplace.crear(actor.getName(),datos);}
    @PutMapping("/vendedor/productos/{id}") public Producto editar(Principal actor,@PathVariable UUID id,@RequestBody ProductoEdicion datos) {return marketplace.editar(actor.getName(),id,datos);}
    @DeleteMapping("/vendedor/productos/{id}/publicacion") @ResponseStatus(HttpStatus.NO_CONTENT) public void retirar(Principal actor,@PathVariable UUID id) {marketplace.retirar(actor.getName(),id);}
    @PutMapping("/vendedor/productos/{id}/stock") @ResponseStatus(HttpStatus.NO_CONTENT) public void stock(Principal actor,@PathVariable UUID id,@RequestBody Stock datos) {marketplace.stock(actor.getName(),id,datos.disponible());}
    @GetMapping("/vendedor/tienda") public Tienda miTienda(Principal actor) {return marketplace.tienda(marketplace.cuenta(actor.getName()).getId());}
    @PutMapping("/vendedor/tienda") public Tienda tienda(Principal actor,@RequestBody Tienda datos) {return marketplace.guardarTienda(actor.getName(),datos);}
    @GetMapping("/vendedor/botellas") public List<Botella> botellas(Principal actor) {return marketplace.botellas(actor.getName());}
    @PostMapping("/vendedor/botellas") @ResponseStatus(HttpStatus.CREATED) public Botella botella(Principal actor,@RequestBody BotellaNueva datos) {return marketplace.crearBotella(actor.getName(),datos);}
    @GetMapping("/vendedor/dashboard") public Dashboard dashboard(Principal actor) {return marketplace.dashboard(actor.getName());}
    @PostMapping(value="/vendedor/productos/{id}/imagenes",consumes=MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED) public Map<String,String> foto(Principal actor,@PathVariable UUID id,@RequestPart("archivo") MultipartFile archivo) throws java.io.IOException {
        return Map.of("url",marketplace.foto(actor.getName(),id,archivo.getContentType(),archivo.getBytes()));
    }
    @DeleteMapping("/vendedor/productos/{id}/imagenes/{foto}") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminarFoto(Principal actor,@PathVariable UUID id,@PathVariable UUID foto) {marketplace.eliminarFoto(actor.getName(),id,foto);}
    @GetMapping("/imagenes/{id}") public ResponseEntity<byte[]> foto(Principal actor,@PathVariable UUID id) {
        Foto f=marketplace.foto(id,actor==null ? null : actor.getName());return ResponseEntity.ok().contentType(MediaType.parseMediaType(f.tipo()))
                .header("X-Content-Type-Options","nosniff").cacheControl(CacheControl.noCache()).body(f.contenido());
    }
}
