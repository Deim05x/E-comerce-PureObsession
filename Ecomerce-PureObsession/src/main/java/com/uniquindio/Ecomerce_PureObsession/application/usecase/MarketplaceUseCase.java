package com.uniquindio.Ecomerce_PureObsession.application.usecase;

import com.uniquindio.Ecomerce_PureObsession.application.model.Marketplace.*;
import com.uniquindio.Ecomerce_PureObsession.application.port.out.*;
import com.uniquindio.Ecomerce_PureObsession.domain.entity.Cuenta;
import com.uniquindio.Ecomerce_PureObsession.domain.exception.ReglaDominioException;
import com.uniquindio.Ecomerce_PureObsession.domain.repository.Pagina;
import com.uniquindio.Ecomerce_PureObsession.domain.service.PoliticaMarketplace;
import com.uniquindio.Ecomerce_PureObsession.domain.valueObject.*;
import java.util.*;

/** Fachada de aplicacion para las pantallas del marketplace, sin dependencias web/JPA. */
public final class MarketplaceUseCase {
    private final CuentaRepository cuentas;
    private final MarketplaceRepository repository;
    public MarketplaceUseCase(CuentaRepository cuentas,MarketplaceRepository repository) {this.cuentas=cuentas;this.repository=repository;}
    public Cuenta cuenta(String actor) {
        if(actor==null) throw new ReglaDominioException("Inicie sesion.");
        return cuentas.buscarPorEmail(actor).orElseThrow(()->new ReglaDominioException("La cuenta no existe."));
    }
    private UUID vendedor(String actor) {
        Cuenta c=cuenta(actor);if(c.getRol()!=Rol.VENDEDOR) throw new ReglaDominioException("Solo vendedores.");return c.getId();
    }
    private void texto(String valor,int max,boolean requerido) {
        if((requerido && (valor==null || valor.isBlank())) || (valor!=null && valor.length()>max))
            throw new ReglaDominioException("Revise los textos obligatorios y sus longitudes.");
    }
    private void categoria(String c) {
        if(c!=null && !Set.of("NICHO","DISENADOR","ARABE").contains(c)) throw new ReglaDominioException("Categoria: NICHO, DISENADOR o ARABE.");
    }
    public Producto crear(String actor,ProductoNuevo d) {
        if(d==null) throw new ReglaDominioException("Solicitud obligatoria.");
        texto(d.nombre(),150,true);texto(d.marca(),120,true);texto(d.descripcion(),4000,false);categoria(d.categoria());
        if(d.tipo()!=TipoPresentacion.DECANT && d.fraganciaId()==null) {
            texto(d.notasSalida(),255,true);texto(d.notasCorazon(),255,true);texto(d.notasFondo(),255,true);
            if(d.familia()==null || d.concentracion()==null) throw new ReglaDominioException("Familia y concentracion obligatorias.");
        }
        PoliticaMarketplace.stock(d.stock());
        if(d.tipo()==null || d.volumenMl()<=0 || d.volumenMl()>100000) throw new ReglaDominioException("Tipo o volumen invalido.");
        if(d.tipo()==TipoPresentacion.DECANT && d.botellaMadreId()==null) throw new ReglaDominioException("Seleccione una botella propia.");
        return repository.crear(vendedor(actor),d);
    }
    public Producto editar(String actor,UUID id,ProductoEdicion d) {
        if(d==null) throw new ReglaDominioException("Solicitud obligatoria.");
        texto(d.marca(),120,true);texto(d.descripcion(),4000,false);categoria(d.categoria());return repository.editar(vendedor(actor),id,d);
    }
    public void retirar(String actor,UUID id) {repository.retirar(vendedor(actor),id);}
    public void stock(String actor,UUID id,int disponible) {PoliticaMarketplace.stock(disponible);repository.stock(vendedor(actor),id,disponible);}
    private void filtro(Filtro f) {
        Pagina.validar(f.pagina(),f.tamanio());texto(f.texto(),150,false);categoria(f.categoria());
        if((f.minimo()!=null && f.minimo().signum()<0)||(f.maximo()!=null && f.maximo().signum()<0)
                ||(f.minimo()!=null && f.maximo()!=null && f.minimo().compareTo(f.maximo())>0)) throw new ReglaDominioException("Rango de precios invalido.");
    }
    public Pagina<Producto> productos(Filtro f) {filtro(f);return repository.productos(f,false);}
    public Pagina<Producto> propios(String actor,int pagina,int tamanio) {
        Pagina.validar(pagina,tamanio);return repository.productos(new Filtro(null,null,null,null,null,null,vendedor(actor),null,"nombre",pagina,tamanio),true);
    }
    public Producto producto(UUID id) {return repository.producto(id,null);}
    public Producto propio(String actor,UUID id) {return repository.producto(id,vendedor(actor));}
    public Tienda tienda(UUID id) {
        Cuenta c=cuentas.buscarPorId(id).orElseThrow(()->new com.uniquindio.Ecomerce_PureObsession.domain.exception.RecursoNoEncontradoException("La tienda no existe."));
        if(c.getRol()!=Rol.VENDEDOR) throw new com.uniquindio.Ecomerce_PureObsession.domain.exception.RecursoNoEncontradoException("La tienda no existe.");
        return repository.tienda(id);
    }
    public Tienda guardarTienda(String actor,Tienda d) {
        if(d==null) throw new ReglaDominioException("Solicitud obligatoria.");
        texto(d.nombre(),120,true);texto(d.descripcion(),2000,false);texto(d.ciudad(),100,true);texto(d.telefono(),22,false);texto(d.logoUrl(),2000,false);
        if(d.logoUrl()!=null && !d.logoUrl().isBlank() && !d.logoUrl().startsWith("https://")) throw new ReglaDominioException("Use una URL HTTPS para el logo.");
        return repository.guardarTienda(vendedor(actor),d);
    }
    public Pagina<Tienda> tiendas(int pagina,int tamanio) {Pagina.validar(pagina,tamanio);return repository.tiendas(pagina,tamanio);}
    public List<Botella> botellas(String actor) {return repository.botellas(vendedor(actor));}
    public Botella crearBotella(String actor,BotellaNueva d) {
        if(d==null || d.fraganciaId()==null || d.volumenMl()<=0 || d.volumenMl()>100000) throw new ReglaDominioException("Fragancia y volumen entre 1 y 100000 ml.");
        return repository.crearBotella(vendedor(actor),d);
    }
    public String foto(String actor,UUID id,String tipo,byte[] contenido) {return repository.foto(vendedor(actor),id,tipo,contenido);}
    public Foto foto(UUID id) {return repository.foto(id);}
    public Foto foto(UUID id,String actor) {return repository.fotoPrivada(id,actor==null ? null : cuenta(actor).getId());}
    public void eliminarFoto(String actor,UUID producto,UUID foto) {repository.eliminarFoto(vendedor(actor),producto,foto);}
    public Carrito carrito(String actor) {return repository.carrito(cuenta(actor).getId());}
    public void itemCarrito(String actor,UUID producto,int cantidad) {repository.itemCarrito(cuenta(actor).getId(),producto,cantidad);}
    public void vaciarCarrito(String actor) {repository.vaciarCarrito(cuenta(actor).getId());}
    public Pagina<Producto> favoritos(String actor,int pagina,int tamanio) {Pagina.validar(pagina,tamanio);return repository.favoritos(cuenta(actor).getId(),pagina,tamanio);}
    public void favorito(String actor,UUID producto,boolean guardar) {repository.favorito(cuenta(actor).getId(),producto,guardar);}
    public List<Resena> resenas(UUID producto) {return repository.resenas(producto);}
    public Resena resena(String actor,UUID producto,int puntuacion,String comentario) {return repository.resena(cuenta(actor).getId(),producto,puntuacion,comentario);}
    public Dashboard dashboard(String actor) {return repository.dashboard(vendedor(actor));}
    public Reputacion reputacion(UUID vendedor) {tienda(vendedor);return repository.reputacion(vendedor);}
    public FraganciaInfo crearFragancia(String actor,FraganciaNueva d) {
        if(d==null || d.familia()==null || d.concentracion()==null) throw new ReglaDominioException("Familia y concentracion obligatorias.");
        texto(d.nombre(),150,true);texto(d.notasSalida(),255,true);texto(d.notasCorazon(),255,true);texto(d.notasFondo(),255,true);
        return repository.crearFragancia(vendedor(actor),d);
    }
    public Pagina<CertificadoInfo> certificados(String actor,EstadoCertificado estado,int pagina,int tamanio) {
        if(cuenta(actor).getRol()!=Rol.ADMIN) throw new ReglaDominioException("Solo administradores.");
        Pagina.validar(pagina,tamanio);return repository.certificados(estado,pagina,tamanio);
    }
}
