package com.uniquindio.Ecomerce_PureObsession.infrastructure.web;
import com.uniquindio.Ecomerce_PureObsession.application.model.Marketplace.*;
import com.uniquindio.Ecomerce_PureObsession.application.usecase.MarketplaceUseCase;
import com.uniquindio.Ecomerce_PureObsession.domain.repository.Pagina;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import java.security.Principal;
import java.util.UUID;
@RestController @RequestMapping("/api")
public class ColeccionClienteController {
    private final MarketplaceUseCase marketplace;
    public ColeccionClienteController(MarketplaceUseCase marketplace) {this.marketplace=marketplace;}
    public record Cantidad(int cantidad) {}
    @GetMapping("/carrito") public Carrito carrito(Principal actor) {return marketplace.carrito(actor.getName());}
    @PutMapping("/carrito/items/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) public void item(Principal actor,@PathVariable UUID id,@RequestBody Cantidad datos) {marketplace.itemCarrito(actor.getName(),id,datos.cantidad());}
    @DeleteMapping("/carrito/items/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) public void eliminar(Principal actor,@PathVariable UUID id) {marketplace.itemCarrito(actor.getName(),id,0);}
    @DeleteMapping("/carrito") @ResponseStatus(HttpStatus.NO_CONTENT) public void vaciar(Principal actor) {marketplace.vaciarCarrito(actor.getName());}
    @GetMapping("/favoritos") public Pagina<Producto> favoritos(Principal actor,@RequestParam(defaultValue="0") int pagina,@RequestParam(defaultValue="20") int tamanio) {return marketplace.favoritos(actor.getName(),pagina,tamanio);}
    @PutMapping("/favoritos/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) public void favorito(Principal actor,@PathVariable UUID id) {marketplace.favorito(actor.getName(),id,true);}
    @DeleteMapping("/favoritos/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) public void quitar(Principal actor,@PathVariable UUID id) {marketplace.favorito(actor.getName(),id,false);}
}
