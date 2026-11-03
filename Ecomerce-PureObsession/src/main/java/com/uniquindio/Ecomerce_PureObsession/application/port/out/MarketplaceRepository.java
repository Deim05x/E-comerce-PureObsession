package com.uniquindio.Ecomerce_PureObsession.application.port.out;

import com.uniquindio.Ecomerce_PureObsession.application.model.Marketplace.*;
import com.uniquindio.Ecomerce_PureObsession.domain.repository.Pagina;
import java.util.*;
import com.uniquindio.Ecomerce_PureObsession.domain.valueObject.EstadoCertificado;

public interface MarketplaceRepository {
    Producto crear(UUID vendedor,ProductoNuevo datos);
    Producto editar(UUID vendedor,UUID id,ProductoEdicion datos);
    void retirar(UUID vendedor,UUID id);
    void stock(UUID vendedor,UUID id,int disponible);
    Pagina<Producto> productos(Filtro filtro,boolean propios);
    Producto producto(UUID id,UUID propietario);
    Tienda tienda(UUID id);
    Tienda guardarTienda(UUID vendedor,Tienda datos);
    Pagina<Tienda> tiendas(int pagina,int tamanio);
    List<Botella> botellas(UUID vendedor);
    Botella crearBotella(UUID vendedor,BotellaNueva datos);
    String foto(UUID vendedor,UUID producto,String tipo,byte[] contenido);
    Foto foto(UUID id);
    Foto fotoPrivada(UUID id,UUID propietario);
    void eliminarFoto(UUID vendedor,UUID producto,UUID foto);
    Carrito carrito(UUID cliente);
    void itemCarrito(UUID cliente,UUID producto,int cantidad);
    void vaciarCarrito(UUID cliente);
    Pagina<Producto> favoritos(UUID cliente,int pagina,int tamanio);
    void favorito(UUID cliente,UUID producto,boolean guardar);
    List<Resena> resenas(UUID producto);
    Resena resena(UUID cliente,UUID producto,int puntuacion,String comentario);
    Dashboard dashboard(UUID vendedor);
    Reputacion reputacion(UUID vendedor);
    FraganciaInfo crearFragancia(UUID vendedor,FraganciaNueva datos);
    Pagina<CertificadoInfo> certificados(EstadoCertificado estado,int pagina,int tamanio);
}
