package com.uniquindio.Ecomerce_PureObsession.infrastructure.persistence.jpa;

import com.uniquindio.Ecomerce_PureObsession.application.model.Marketplace.*;
import com.uniquindio.Ecomerce_PureObsession.application.model.Marketplace.Resena;
import com.uniquindio.Ecomerce_PureObsession.application.port.out.MarketplaceRepository;
import com.uniquindio.Ecomerce_PureObsession.domain.entity.*;
import com.uniquindio.Ecomerce_PureObsession.domain.exception.*;
import com.uniquindio.Ecomerce_PureObsession.domain.repository.Pagina;
import com.uniquindio.Ecomerce_PureObsession.domain.service.PoliticaMarketplace;
import com.uniquindio.Ecomerce_PureObsession.domain.valueObject.*;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository @Transactional
public class MarketplaceJpaAdapter implements MarketplaceRepository {
    @PersistenceContext private EntityManager em;
    private PresentacionJpa propia(UUID vendedor,UUID id) {
        PresentacionJpa p=em.find(PresentacionJpa.class,id,LockModeType.PESSIMISTIC_WRITE);
        if(p==null) throw new RecursoNoEncontradoException("El producto no existe.");
        PoliticaMarketplace.propietario(p.vendedorId,vendedor); return p;
    }
    private FraganciaJpa fragancia(UUID id) {
        FraganciaJpa f=em.find(FraganciaJpa.class,id);
        if(f==null || f.eliminada) throw new RecursoNoEncontradoException("La fragancia no existe.");
        return f;
    }
    private FichaProductoJpa ficha(UUID id) {
        FichaProductoJpa f=em.find(FichaProductoJpa.class,id,LockModeType.PESSIMISTIC_WRITE);
        if(f==null) { f=new FichaProductoJpa(); f.id=id; em.persist(f); }
        return f;
    }
    @Override public Producto crear(UUID vendedor,ProductoNuevo d) {
        UUID fraganciaId;
        if(d.tipo()==TipoPresentacion.DECANT) {
            BotellaJpa b=em.find(BotellaJpa.class,d.botellaMadreId(),LockModeType.PESSIMISTIC_WRITE);
            if(b==null || b.eliminada) throw new RecursoNoEncontradoException("La botella no existe.");
            PoliticaMarketplace.propietario(b.vendedorId,vendedor);
            if(d.volumenMl()>=b.originalMl) throw new ReglaDominioException("El decant debe ser menor que la botella.");
            fraganciaId=b.fragancia.id;
        } else if(d.fraganciaId()!=null) {
            fraganciaId=fragancia(d.fraganciaId()).id;
        } else {
            Fragancia f=Fragancia.crear(UUID.randomUUID(),d.nombre().trim(),d.familia(),d.concentracion(),
                    PiramideOlfativa.crear(d.notasSalida(),d.notasCorazon(),d.notasFondo()));
            em.persist(FraganciaPersistenceMapper.aJpa(f)); fraganciaId=f.getId();
        }
        FraganciaJpa f=fragancia(fraganciaId);
        if(!f.activa) throw new ReglaDominioException("La fragancia esta inactiva.");
        Presentacion p=Presentacion.crear(UUID.randomUUID(),vendedor,fraganciaId,
                d.tipo()==TipoPresentacion.DECANT ? d.botellaMadreId() : null,d.tipo(),d.volumenMl(),d.precio(),d.origen(),d.paisOrigen(),d.original());
        em.persist(PresentacionPersistenceMapper.aJpa(p));
        FichaProductoJpa ficha=ficha(p.getId()); ficha.marca=d.marca().trim(); ficha.descripcion=d.descripcion();
        ficha.categoria=d.categoria(); ficha.disponibles=d.tipo()==TipoPresentacion.DECANT ? 0 : d.stock();
        em.flush(); return producto(p.getId(),vendedor);
    }
    @Override public Producto editar(UUID vendedor,UUID id,ProductoEdicion datos) {
        PresentacionJpa p=propia(vendedor,id);
        p.precio=Presentacion.validarPrecio(datos.precio());
        FichaProductoJpa f=ficha(id); f.marca=datos.marca(); f.descripcion=datos.descripcion(); f.categoria=datos.categoria();
        return producto(id,vendedor);
    }
    @Override public void retirar(UUID vendedor,UUID id) { propia(vendedor,id).publicada=false; }
    @Override public void stock(UUID vendedor,UUID id,int disponible) {
        PresentacionJpa p=propia(vendedor,id); PoliticaMarketplace.stock(disponible);
        if(p.tipo==TipoPresentacion.DECANT) throw new ReglaDominioException("Los decants usan los mililitros de su botella; registre otra botella para reponer.");
        ficha(id).disponibles=disponible;
    }
    @Override public Pagina<Producto> productos(Filtro filtro,boolean propios) {
        Pagina.validar(filtro.pagina(),filtro.tamanio());
        StringBuilder where=new StringBuilder(" from PresentacionJpa p join FraganciaJpa f on f.id=p.fraganciaId left join FichaProductoJpa fp on fp.id=p.id where f.eliminada=false");
        Map<String,Object> args=new HashMap<>();
        if(!propios) where.append(" and ").append(ConsultaCatalogoJpaAdapter.PRESENTACION_PUBLICA);
        if(filtro.vendedorId()!=null) {where.append(" and p.vendedorId=:vendedor"); args.put("vendedor",filtro.vendedorId());}
        if(filtro.fraganciaId()!=null) {where.append(" and p.fraganciaId=:fragancia"); args.put("fragancia",filtro.fraganciaId());}
        if(filtro.familia()!=null) {where.append(" and f.familia=:familia"); args.put("familia",filtro.familia());}
        if(filtro.tipo()!=null) {where.append(" and p.tipo=:tipo"); args.put("tipo",filtro.tipo());}
        if(filtro.categoria()!=null && !filtro.categoria().isBlank()) {where.append(" and fp.categoria=:categoria"); args.put("categoria",filtro.categoria());}
        if(filtro.minimo()!=null) {where.append(" and p.precio>=:minimo"); args.put("minimo",filtro.minimo());}
        if(filtro.maximo()!=null) {where.append(" and p.precio<=:maximo"); args.put("maximo",filtro.maximo());}
        if(filtro.texto()!=null && !filtro.texto().isBlank()) {
            where.append(" and (lower(f.nombre) like :texto escape '!' or lower(fp.marca) like :texto escape '!')");
            args.put("texto","%"+filtro.texto().trim().toLowerCase(Locale.ROOT).replace("!","!!").replace("%","!%").replace("_","!_")+"%");
        }
        String orden=switch(filtro.orden()==null ? "nombre" : filtro.orden()) {
            case "precio_asc" -> "p.precio asc,p.id"; case "precio_desc" -> "p.precio desc,p.id";
            case "nombre" -> "lower(f.nombre),p.id"; default -> throw new ReglaDominioException("Orden: nombre, precio_asc o precio_desc.");
        };
        TypedQuery<Long> count=em.createQuery("select count(p)"+where,Long.class);
        TypedQuery<PresentacionJpa> query=em.createQuery("select p"+where+" order by "+orden,PresentacionJpa.class);
        args.forEach((k,v)->{count.setParameter(k,v);query.setParameter(k,v);});
        long total=count.getSingleResult();
        return new Pagina<>(query.setFirstResult(filtro.pagina()*filtro.tamanio()).setMaxResults(filtro.tamanio())
                .getResultList().stream().map(this::vista).toList(),total,(int)Math.ceil((double)total/filtro.tamanio()),filtro.pagina(),filtro.tamanio());
    }
    @Override public Producto producto(UUID id,UUID propietario) {
        PresentacionJpa p=em.find(PresentacionJpa.class,id);
        if(p==null) throw new RecursoNoEncontradoException("El producto no existe.");
        if(propietario!=null) PoliticaMarketplace.propietario(p.vendedorId,propietario);
        else if(em.createQuery("select count(p) from PresentacionJpa p,FraganciaJpa f where p.id=:id and f.id=p.fraganciaId and "
                +ConsultaCatalogoJpaAdapter.PRESENTACION_PUBLICA,Long.class).setParameter("id",id).getSingleResult()==0)
            throw new RecursoNoEncontradoException("El producto no esta publicado.");
        return vista(p);
    }
    private Producto vista(PresentacionJpa p) {
        FraganciaJpa f=em.find(FraganciaJpa.class,p.fraganciaId); FichaProductoJpa fp=em.find(FichaProductoJpa.class,p.id);
        if(f==null) throw new RecursoNoEncontradoException("La fragancia no existe.");
        Tienda tienda=tienda(p.vendedorId);
        int stock=fp!=null && p.tipo!=TipoPresentacion.DECANT ? fp.disponibles : 0;
        if(p.botellaMadreId!=null && (p.tipo==TipoPresentacion.DECANT || fp==null)) {
            BotellaJpa b=em.find(BotellaJpa.class,p.botellaMadreId);
            if(b!=null && !b.eliminada) stock=b.disponibleMl/p.volumenMl;
        }
        List<String> imagenes=em.createQuery("select x.id from FotoProductoJpa x where x.productoId=:id and x.eliminada=false order by x.id",UUID.class)
                .setParameter("id",p.id).getResultList().stream().map(x->"/api/imagenes/"+x).toList();
        Object[] rating=em.createQuery("select avg(r.puntuacion),count(r) from ResenaProductoJpa r where r.productoId=:id",Object[].class)
                .setParameter("id",p.id).getSingleResult();
        if(f.eliminada || !f.activa) stock=0;
        return new Producto(p.id,p.fraganciaId,p.vendedorId,f.nombre,fp==null ? "" : fp.marca,fp==null ? "" : fp.descripcion,
                fp==null ? null : fp.categoria,tienda.nombre(),tienda.ciudad(),p.tipo,p.volumenMl,p.precio,"COP",p.original,p.publicada && f.activa && !f.eliminada && f.publicada,
                f.familia,f.concentracion,f.salida,f.corazon,f.fondo,stock,imagenes,rating[0]==null ? 0 : ((Number)rating[0]).doubleValue(),((Number)rating[1]).longValue(),
                em.createQuery("select c.estado from CertificadoJpa c where c.id=:id",EstadoCertificado.class).setParameter("id",p.id).getResultStream().findFirst().orElse(null));
    }
    @Override public Tienda tienda(UUID id) {
        CuentaJpa c=em.find(CuentaJpa.class,id);
        TiendaJpa t=em.find(TiendaJpa.class,id);
        return new Tienda(id,t==null ? (c==null ? "Vendedor" : c.nombre) : t.nombre,t==null ? "" : t.descripcion,
                t==null ? "" : t.ciudad,t==null ? "" : t.telefono,t==null ? null : t.logoUrl);
    }
    @Override public Tienda guardarTienda(UUID vendedor,Tienda datos) {
        TiendaJpa t=em.find(TiendaJpa.class,vendedor,LockModeType.PESSIMISTIC_WRITE);
        if(t==null) { t=new TiendaJpa();t.id=vendedor;em.persist(t); }
        t.nombre=datos.nombre();t.descripcion=datos.descripcion();t.ciudad=datos.ciudad();t.telefono=datos.telefono();t.logoUrl=datos.logoUrl();
        return tienda(vendedor);
    }
    @Override public Pagina<Tienda> tiendas(int pagina,int tamanio) {
        long total=em.createQuery("select count(c) from CuentaJpa c where c.rol=:rol",Long.class).setParameter("rol",Rol.VENDEDOR).getSingleResult();
        List<Tienda> lista=em.createQuery("select c.id from CuentaJpa c where c.rol=:rol order by c.nombre,c.id",UUID.class)
                .setParameter("rol",Rol.VENDEDOR).setFirstResult(pagina*tamanio).setMaxResults(tamanio).getResultList().stream().map(this::tienda).toList();
        return new Pagina<>(lista,total,(int)Math.ceil((double)total/tamanio),pagina,tamanio);
    }
    @Override public List<Botella> botellas(UUID vendedor) {
        return em.createQuery("select b from BotellaJpa b where b.vendedorId=:v and b.eliminada=false and b.fragancia.eliminada=false order by b.fragancia.nombre,b.id",BotellaJpa.class)
                .setParameter("v",vendedor).getResultList().stream().map(b->new Botella(b.id,b.fragancia.id,b.fragancia.nombre,b.originalMl,b.disponibleMl)).toList();
    }
    @Override public Botella crearBotella(UUID vendedor,BotellaNueva datos) {
        FraganciaJpa f=fragancia(datos.fraganciaId());
        if(!f.activa) throw new ReglaDominioException("La fragancia esta inactiva.");
        Volumen.crear(datos.volumenMl());
        BotellaJpa b=new BotellaJpa(); b.id=UUID.randomUUID();b.vendedorId=vendedor;b.fragancia=f;
        b.originalMl=datos.volumenMl();b.disponibleMl=datos.volumenMl(); em.persist(b);
        return new Botella(b.id,f.id,f.nombre,b.originalMl,b.disponibleMl);
    }
    @Override public String foto(UUID vendedor,UUID producto,String tipo,byte[] contenido) {
        propia(vendedor,producto);
        if(em.createQuery("select count(x) from FotoProductoJpa x where x.productoId=:id and x.eliminada=false",Long.class).setParameter("id",producto).getSingleResult()>=6)
            throw new ReglaDominioException("Maximo seis fotografias por producto.");
        if(contenido==null || contenido.length==0 || contenido.length>5*1024*1024 || !("image/png".equals(tipo)||"image/jpeg".equals(tipo)))
            throw new ReglaDominioException("Use PNG o JPEG de hasta 5 MB.");
        try(var entrada=javax.imageio.ImageIO.createImageInputStream(new java.io.ByteArrayInputStream(contenido))) {
            var lectores=javax.imageio.ImageIO.getImageReaders(entrada);
            if(!lectores.hasNext()) throw new IllegalArgumentException();
            var lector=lectores.next();
            try {
                lector.setInput(entrada);
                String formato=lector.getFormatName();
                if(lector.getWidth(0)>4096 || lector.getHeight(0)>4096 || !("image/"+formato.toLowerCase(Locale.ROOT)).equals(tipo))
                    throw new IllegalArgumentException();
            } finally { lector.dispose(); }
        } catch(Exception e) {throw new ReglaDominioException("Imagen invalida; maximo 4096 x 4096 pixeles.");}
        FotoProductoJpa f=new FotoProductoJpa();f.id=UUID.randomUUID();f.productoId=producto;f.tipo=tipo;f.contenido=contenido.clone();em.persist(f);
        return "/api/imagenes/"+f.id;
    }
    @Override public Foto foto(UUID id) {
        return fotoPrivada(id,null);
    }
    @Override public Foto fotoPrivada(UUID id,UUID propietario) {
        FotoProductoJpa f=em.find(FotoProductoJpa.class,id);
        if(f==null || f.eliminada) throw new RecursoNoEncontradoException("La imagen no existe.");
        PresentacionJpa p=em.find(PresentacionJpa.class,f.productoId);
        if(propietario==null || !propietario.equals(p.vendedorId)) producto(f.productoId,null);
        return new Foto(f.id,f.tipo,f.contenido.clone());
    }
    @Override public void eliminarFoto(UUID vendedor,UUID producto,UUID foto) {
        propia(vendedor,producto);FotoProductoJpa f=em.find(FotoProductoJpa.class,foto);
        if(f==null || !producto.equals(f.productoId)) throw new RecursoNoEncontradoException("La imagen no existe.");
        f.eliminada=true;
    }
    private SeleccionClienteJpa seleccion(UUID cliente,UUID producto) {
        em.find(CuentaJpa.class,cliente,LockModeType.PESSIMISTIC_WRITE);
        String id=cliente+":"+producto;
        SeleccionClienteJpa s=em.find(SeleccionClienteJpa.class,id);
        if(s==null) {s=new SeleccionClienteJpa();s.id=id;s.clienteId=cliente;s.productoId=producto;em.persist(s);}
        return s;
    }
    @Override public Carrito carrito(UUID cliente) {
        List<LineaCarrito> items=new ArrayList<>(); BigDecimal total=BigDecimal.ZERO;
        for(SeleccionClienteJpa s:em.createQuery("select s from SeleccionClienteJpa s where s.clienteId=:id and s.cantidad>0 order by s.productoId",SeleccionClienteJpa.class).setParameter("id",cliente).getResultList()) {
            PresentacionJpa p=em.find(PresentacionJpa.class,s.productoId);
            // Mostrar retirados permite al cliente quitarlos; checkout vuelve a validar disponibilidad.
            Producto vista=vista(p);BigDecimal subtotal=p.precio.multiply(BigDecimal.valueOf(s.cantidad));
            items.add(new LineaCarrito(vista,s.cantidad,subtotal));total=total.add(subtotal);
        }
        return new Carrito(items,total,"COP");
    }
    @Override public void itemCarrito(UUID cliente,UUID producto,int cantidad) {
        if(cantidad<0 || cantidad>1000) throw new ReglaDominioException("Cantidad entre 0 y 1000.");
        if(cantidad>0) {
            Producto p=producto(producto,null);
            if(cantidad>p.stockDisponible()) throw new ReglaDominioException("Stock insuficiente.");
        }
        SeleccionClienteJpa s=seleccion(cliente,producto); s.cantidad=cantidad;
        if(em.createQuery("select count(s) from SeleccionClienteJpa s where s.clienteId=:id and s.cantidad>0",Long.class).setParameter("id",cliente).getSingleResult()>50)
            throw new ReglaDominioException("Maximo 50 productos por carrito.");
    }
    @Override public void vaciarCarrito(UUID cliente) {
        em.find(CuentaJpa.class,cliente,LockModeType.PESSIMISTIC_WRITE);
        em.createQuery("update SeleccionClienteJpa s set s.cantidad=0 where s.clienteId=:id").setParameter("id",cliente).executeUpdate();
    }
    @Override public Pagina<Producto> favoritos(UUID cliente,int pagina,int tamanio) {
        String desde=" from PresentacionJpa p,FraganciaJpa f where f.id=p.fraganciaId and "+ConsultaCatalogoJpaAdapter.PRESENTACION_PUBLICA
                +" and exists(select s.id from SeleccionClienteJpa s where s.clienteId=:cliente and s.productoId=p.id and s.favorito=true)";
        long total=em.createQuery("select count(p)"+desde,Long.class).setParameter("cliente",cliente).getSingleResult();
        var lista=em.createQuery("select p"+desde+" order by f.nombre,p.id",PresentacionJpa.class).setParameter("cliente",cliente)
                .setFirstResult(pagina*tamanio).setMaxResults(tamanio).getResultList().stream().map(this::vista).toList();
        return new Pagina<>(lista,total,(int)Math.ceil((double)total/tamanio),pagina,tamanio);
    }
    @Override public void favorito(UUID cliente,UUID producto,boolean guardar) {
        if(guardar) producto(producto,null);seleccion(cliente,producto).favorito=guardar;
    }
    @Override public List<Resena> resenas(UUID producto) {
        producto(producto,null);
        return em.createQuery("select r from ResenaProductoJpa r where r.productoId=:id order by r.creadaEn desc,r.id",ResenaProductoJpa.class)
                .setParameter("id",producto).setMaxResults(100).getResultList().stream().map(r->new Resena(r.id,em.find(CuentaJpa.class,r.clienteId).nombre,r.puntuacion,r.comentario,r.creadaEn)).toList();
    }
    @Override public Resena resena(UUID cliente,UUID producto,int puntuacion,String comentario) {
        producto(producto,null);em.find(CuentaJpa.class,cliente,LockModeType.PESSIMISTIC_WRITE);
        if(em.createQuery("select count(p) from PedidoJpa p join p.lineas l where p.clienteId=:cliente and l.itemId=:producto and p.estado=:estado",Long.class)
                .setParameter("cliente",cliente).setParameter("producto",producto).setParameter("estado",EstadoPedido.ENTREGADO).getSingleResult()==0)
            throw new ReglaDominioException("Solo puede resenar productos de pedidos entregados.");
        if(puntuacion<1 || puntuacion>5 || comentario==null || comentario.isBlank() || comentario.length()>2000)
            throw new ReglaDominioException("Puntuacion 1 a 5 y comentario de hasta 2000 caracteres.");
        UUID id=UUID.nameUUIDFromBytes((cliente+":"+producto).getBytes(java.nio.charset.StandardCharsets.UTF_8));
        ResenaProductoJpa r=em.find(ResenaProductoJpa.class,id);
        if(r==null) {r=new ResenaProductoJpa();r.id=id;r.clienteId=cliente;r.productoId=producto;em.persist(r);}
        r.puntuacion=puntuacion;r.comentario=comentario;r.creadaEn=Instant.now();
        return new Resena(r.id,em.find(CuentaJpa.class,cliente).nombre,puntuacion,comentario,r.creadaEn);
    }
    @Override public Dashboard dashboard(UUID vendedor) {
        long productos=em.createQuery("select count(p) from PresentacionJpa p where p.vendedorId=:id",Long.class).setParameter("id",vendedor).getSingleResult();
        long pendientes=em.createQuery("select count(distinct p.id) from PedidoJpa p join p.lineas l where l.vendedorId=:id and p.estado=:estado",Long.class)
                .setParameter("id",vendedor).setParameter("estado",EstadoPedido.CREADO).getSingleResult();
        Object[] ventas=em.createQuery("select count(distinct p.id),sum(l.precio*l.cantidad) from PedidoJpa p join p.lineas l where l.vendedorId=:id and p.estado in :estados",Object[].class)
                .setParameter("id",vendedor).setParameter("estados",List.of(EstadoPedido.CONFIRMADO,EstadoPedido.ENVIADO,EstadoPedido.ENTREGADO)).getSingleResult();
        return new Dashboard(productos,pendientes,((Number)ventas[0]).longValue(),ventas[1]==null ? BigDecimal.ZERO : (BigDecimal)ventas[1]);
    }
    @Override public Reputacion reputacion(UUID vendedor) {
        Object[] rating=em.createQuery("select avg(r.puntuacion),count(r) from ResenaProductoJpa r join PresentacionJpa p on p.id=r.productoId where p.vendedorId=:id",Object[].class)
                .setParameter("id",vendedor).getSingleResult();
        Long ventas=em.createQuery("select count(distinct p.id) from PedidoJpa p join p.lineas l where l.vendedorId=:id and p.estado in :estados",Long.class)
                .setParameter("id",vendedor).setParameter("estados",List.of(EstadoPedido.CONFIRMADO,EstadoPedido.ENVIADO,EstadoPedido.ENTREGADO)).getSingleResult();
        Long productos=em.createQuery("select count(p) from PresentacionJpa p,FraganciaJpa f where f.id=p.fraganciaId and p.vendedorId=:id and "+ConsultaCatalogoJpaAdapter.PRESENTACION_PUBLICA,Long.class)
                .setParameter("id",vendedor).getSingleResult();
        return new Reputacion(rating[0]==null ? 0 : ((Number)rating[0]).doubleValue(),((Number)rating[1]).longValue(),ventas,productos);
    }
    @Override public FraganciaInfo crearFragancia(UUID vendedor,FraganciaNueva datos) {
        Fragancia f=Fragancia.crear(UUID.randomUUID(),datos.nombre(),datos.familia(),datos.concentracion(),
                PiramideOlfativa.crear(datos.notasSalida(),datos.notasCorazon(),datos.notasFondo()));
        em.persist(FraganciaPersistenceMapper.aJpa(f));return new FraganciaInfo(f.getId(),f.getNombre(),f.getFamiliaOlfativa(),f.getConcentracion());
    }
    @Override public Pagina<CertificadoInfo> certificados(EstadoCertificado estado,int pagina,int tamanio) {
        String desde=" from CertificadoJpa c,PresentacionJpa p,FraganciaJpa f where c.id=p.id and p.fraganciaId=f.id"+(estado==null ? "" : " and c.estado=:estado");
        TypedQuery<Long> cuenta=em.createQuery("select count(c)"+desde,Long.class);
        TypedQuery<Object[]> consulta=em.createQuery("select p.id,f.nombre,p.vendedorId,c.estado,c.tipoContenido"+desde+" order by p.id",Object[].class);
        if(estado!=null) {cuenta.setParameter("estado",estado);consulta.setParameter("estado",estado);}
        long total=cuenta.getSingleResult();
        var contenido=consulta.setFirstResult(pagina*tamanio).setMaxResults(tamanio).getResultList().stream()
                .map(x->new CertificadoInfo((UUID)x[0],(String)x[1],(UUID)x[2],(EstadoCertificado)x[3],(String)x[4])).toList();
        return new Pagina<>(contenido,total,(int)Math.ceil((double)total/tamanio),pagina,tamanio);
    }
}
