package com.uniquindio.Ecomerce_PureObsession.application.model;

import com.uniquindio.Ecomerce_PureObsession.domain.valueObject.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

public final class Marketplace {
    private Marketplace() {}
    public record ProductoNuevo(String nombre,String marca,String descripcion,String categoria,
            FamiliaOlfativa familia,Concentracion concentracion,String notasSalida,String notasCorazon,String notasFondo,
            TipoPresentacion tipo,int volumenMl,BigDecimal precio,boolean original,OrigenProducto origen,String paisOrigen,
            UUID botellaMadreId,Integer stock,UUID fraganciaId) {
        public ProductoNuevo {if(stock==null)stock=0;}
    }
    public record ProductoEdicion(String marca,String descripcion,String categoria,BigDecimal precio) {}
    public record Producto(UUID id,UUID fraganciaId,UUID vendedorId,String nombre,String marca,String descripcion,
            String categoria,String vendedor,String ciudad,TipoPresentacion tipo,int volumenMl,BigDecimal precio,String moneda,
            boolean original,boolean publicada,FamiliaOlfativa familia,Concentracion concentracion,
            String notasSalida,String notasCorazon,String notasFondo,int stockDisponible,List<String> imagenes,
            double calificacion,long resenas,EstadoCertificado certificado) {}
    public record Filtro(String texto,FamiliaOlfativa familia,TipoPresentacion tipo,String categoria,BigDecimal minimo,
            BigDecimal maximo,UUID vendedorId,UUID fraganciaId,String orden,int pagina,int tamanio) {}
    public record Tienda(UUID id,String nombre,String descripcion,String ciudad,String telefono,String logoUrl) {}
    public record BotellaNueva(UUID fraganciaId,int volumenMl) {}
    public record Botella(UUID id,UUID fraganciaId,String nombre,int originalMl,int disponibleMl) {}
    public record Foto(UUID id,String tipo,byte[] contenido) {}
    public record LineaCarrito(Producto producto,int cantidad,BigDecimal subtotal) {}
    public record Carrito(List<LineaCarrito> items,BigDecimal subtotal,String moneda) {}
    public record Envio(UUID vendedorId,String estado,String transportadora,String guia,Instant enviadoEn,Instant entregadoEn) {}
    public record Venta(UUID pedidoId,EstadoPedido estado,DatosEntrega entrega,Instant creadoEn,Instant reservaVenceEn,
            BigDecimal subtotal,List<com.uniquindio.Ecomerce_PureObsession.application.dto.response.PedidoResponse.Linea> items,Envio envio) {}
    public record Resena(UUID id,String autor,int puntuacion,String comentario,Instant creadaEn) {}
    public record Dashboard(long productos,long pedidosPendientes,long pedidosVendidos,BigDecimal ventas) {}
    public record Reputacion(double calificacion,long resenas,long ventas,long productos) {}
    public record FraganciaNueva(String nombre,FamiliaOlfativa familia,Concentracion concentracion,String notasSalida,String notasCorazon,String notasFondo) {}
    public record FraganciaInfo(UUID id,String nombre,FamiliaOlfativa familia,Concentracion concentracion) {}
    public record CertificadoInfo(UUID presentacionId,String nombre,UUID vendedorId,EstadoCertificado estado,String tipoContenido) {}
}
