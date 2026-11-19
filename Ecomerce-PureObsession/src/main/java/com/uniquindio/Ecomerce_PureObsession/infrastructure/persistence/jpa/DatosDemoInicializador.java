package com.uniquindio.Ecomerce_PureObsession.infrastructure.persistence.jpa;

import com.uniquindio.Ecomerce_PureObsession.application.port.out.*;
import com.uniquindio.Ecomerce_PureObsession.domain.entity.*;
import com.uniquindio.Ecomerce_PureObsession.domain.repository.*;
import com.uniquindio.Ecomerce_PureObsession.domain.valueObject.*;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.*;
import org.springframework.boot.*;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Semilla optativa, atomica y estable; nunca reinicia existencias al volver a arrancar. */
@Component
@Profile("demo")
public class DatosDemoInicializador implements ApplicationRunner {
    @PersistenceContext private EntityManager em;
    private final CuentaRepository cuentas;
    private final BotellaMadreRepository botellas;
    private final PresentacionRepository presentaciones;
    private final PedidoRepository pedidos;
    private final InventarioVentaPort inventario;
    private final PasswordEncoder encoder;

    public DatosDemoInicializador(CuentaRepository cuentas, BotellaMadreRepository botellas,
            PresentacionRepository presentaciones, PedidoRepository pedidos,
            InventarioVentaPort inventario, PasswordEncoder encoder) {
        this.cuentas=cuentas; this.botellas=botellas; this.presentaciones=presentaciones;
        this.pedidos=pedidos; this.inventario=inventario; this.encoder=encoder;
    }

    @Override @Transactional
    public void run(ApplicationArguments argumentos) {
        // Toda la carga comparte transaccion: la cuenta marcador solo queda si termina la semilla.
        if(em.find(CuentaJpa.class,id("cliente"))!=null) {completarFichas();completarCatalogoVisual();return;}
        cuenta("admin","Administracion demo","90000001",Rol.ADMIN);
        cuenta("vendedor","Vendedor demo","90000002",Rol.VENDEDOR);
        cuenta("vendedor2","Segundo vendedor demo","90000003",Rol.VENDEDOR);
        Cuenta cliente=cuenta("cliente","Cliente demo","90000004",Rol.CLIENTE);
        Presentacion rosa=oferta("rosa","Rosa de Jardin",FamiliaOlfativa.FLORAL,"vendedor","20000.00");
        Presentacion cedro=oferta("cedro","Cedro Nocturno",FamiliaOlfativa.AMADERADA,"vendedor2","25000.00");
        Presentacion brisa=oferta("brisa","Brisa Citrica",FamiliaOlfativa.CITRICA,"vendedor","15000.00");
        // Mismo producto y tamano con precio de otro vendedor, sobre el inventario central actual.
        BotellaJpa segundaBotella=new BotellaJpa();segundaBotella.id=id("botella-rosa-vendedor2");segundaBotella.vendedorId=id("vendedor2");
        segundaBotella.fragancia=em.find(FraganciaJpa.class,rosa.getFraganciaId());segundaBotella.originalMl=500;segundaBotella.disponibleMl=500;em.persist(segundaBotella);
        Presentacion alternativa=Presentacion.crear(id("oferta-rosa-alternativa"),id("vendedor2"),rosa.getFraganciaId(),
                segundaBotella.id,TipoPresentacion.DECANT,10,new BigDecimal("18000.00"),OrigenProducto.NACIONAL,"CO",false);
        alternativa.publicar(false); presentaciones.guardar(alternativa);
        pedido("confirmado",cliente,rosa,3,EstadoPedido.CONFIRMADO);
        pedido("entregado",cliente,rosa,2,EstadoPedido.ENTREGADO);
        pedido("enviado",cliente,cedro,2,EstadoPedido.ENVIADO);
        pedido("creado",cliente,brisa,1,EstadoPedido.CREADO);
        pedido("cancelado",cliente,brisa,8,EstadoPedido.CANCELADO);
        completarFichas();
        completarCatalogoVisual();
    }

    private Cuenta cuenta(String clave,String nombre,String cedula,Rol rol) {
        return cuentas.guardar(Cuenta.crear(id(clave),nombre,cedula,clave+"@pureobsession.demo",
                encoder.encode("DemoPure2026!"),rol));
    }
    private Presentacion oferta(String clave,String nombre,FamiliaOlfativa familia,String vendedor,String precio) {
        Fragancia f=Fragancia.crear(id("fragancia-"+clave),nombre,familia,Concentracion.EAU_DE_PARFUM,
                PiramideOlfativa.crear("Bergamota","Jazmin","Almizcle")); f.publicar();
        BotellaMadre b=BotellaMadre.crear(id("botella-"+clave),f,Volumen.crear(500)); botellas.guardar(b);
        em.find(BotellaJpa.class,b.getId()).vendedorId=id(vendedor);
        Presentacion p=Presentacion.crear(id("oferta-"+clave),id(vendedor),f.getId(),b.getId(),TipoPresentacion.DECANT,
                10,new BigDecimal(precio),OrigenProducto.NACIONAL,"CO",false);
        p.publicar(false); return presentaciones.guardar(p);
    }
    private void pedido(String clave,Cuenta cliente,Presentacion p,int cantidad,EstadoPedido estado) {
        LineaPedido l=LineaPedido.crearConVendedor(id("linea-"+clave),p.getId(),p.getFraganciaId(),p.getVendedorId(),
                "Decant demo 10 ml",Precio.crear(p.getPrecio().doubleValue(),"COP"),cantidad);
        Pedido pedido=Pedido.crearParaCheckout(id("pedido-"+clave),cliente.getId(),
                new DatosEntrega(cliente.getNombre(),cliente.getEmail(),"3001234567","Calle Demo 123"),null,List.of(l));
        if(estado==EstadoPedido.CANCELADO) pedido.cancelar();
        else {
            inventario.reservar(p.getId(),cantidad);
            if(estado!=EstadoPedido.CREADO) pedido.confirmar();
            if(estado==EstadoPedido.ENVIADO || estado==EstadoPedido.ENTREGADO) pedido.marcarEnviado();
            if(estado==EstadoPedido.ENTREGADO) pedido.marcarEntregado();
        }
        pedidos.guardar(pedido);
    }
    static UUID id(String clave) {
        return UUID.nameUUIDFromBytes(("pure-obsession-demo-"+clave).getBytes(StandardCharsets.UTF_8));
    }
    private void completarFichas() {
        for(String clave:List.of("rosa","cedro","brisa","rosa-alternativa")) {
            UUID producto=id("oferta-"+clave);
            if(em.find(PresentacionJpa.class,producto)!=null && em.find(FichaProductoJpa.class,producto)==null) {
                FichaProductoJpa f=new FichaProductoJpa();f.id=producto;f.marca="Pure Obsession Demo";
                f.descripcion="Fragancia ficticia de demostracion. Consulte sus notas olfativas.";f.categoria="NICHO";em.persist(f);
            }
        }
    }

    private record ProductoCatalogo(String clave,String nombre,String inspirado,FamiliaOlfativa familia,String imagen) {}

    /** Catálogo visual de demostración. Los nombres son de Pure Obsession; las referencias solo describen inspiración olfativa. */
    private void completarCatalogoVisual() {
        int indice=0;
        for(ProductoCatalogo producto:catalogoVisual()) {
            UUID fraganciaId=id("fragancia-catalogo-"+producto.clave());
            UUID presentacionId=id("producto-catalogo-"+producto.clave());
            UUID vendedorId=id(indice%2==0 ? "vendedor" : "vendedor2");
            FraganciaJpa fragancia=em.find(FraganciaJpa.class,fraganciaId);
            if(fragancia==null) {
                Fragancia dominio=Fragancia.crear(fraganciaId,producto.nombre(),producto.familia(),Concentracion.EAU_DE_PARFUM,
                        PiramideOlfativa.crear(notaSalida(producto.familia()),notaCorazon(producto.familia()),notaFondo(producto.familia())));
                dominio.publicar();fragancia=FraganciaPersistenceMapper.aJpa(dominio);em.persist(fragancia);
            }
            PresentacionJpa presentacion=em.find(PresentacionJpa.class,presentacionId);
            if(presentacion==null) {
                BigDecimal precio=BigDecimal.valueOf(89000L+(indice%8)*7000L).setScale(2);
                Presentacion dominio=Presentacion.crear(presentacionId,vendedorId,fraganciaId,null,TipoPresentacion.PERFUME,
                        100,precio,OrigenProducto.NACIONAL,"CO",false);
                dominio.publicar(false);presentacion=PresentacionPersistenceMapper.aJpa(dominio);em.persist(presentacion);
            }
            FichaProductoJpa ficha=em.find(FichaProductoJpa.class,presentacionId);
            if(ficha==null) {ficha=new FichaProductoJpa();ficha.id=presentacionId;em.persist(ficha);}
            ficha.marca="Pure Obsession";
            ficha.categoria="DISENADOR";
            ficha.disponibles=Math.max(ficha.disponibles,12+(indice%9)*3);
            ficha.descripcion="Fragancia de autor Pure Obsession inspirada en "+producto.inspirado()
                    +". Elaborada como interpretación olfativa independiente; no es un producto oficial ni afiliado a la marca de referencia.";
            UUID fotoId=id("foto-catalogo-"+producto.clave());
            if(em.find(FotoProductoJpa.class,fotoId)==null) {
                try(var entrada=new ClassPathResource("demo/perfumes/"+producto.imagen()).getInputStream()) {
                    FotoProductoJpa foto=new FotoProductoJpa();foto.id=fotoId;foto.productoId=presentacionId;
                    foto.tipo="image/jpeg";foto.contenido=entrada.readAllBytes();em.persist(foto);
                } catch(java.io.IOException e) {
                    throw new IllegalStateException("No se pudo cargar la imagen demo de "+producto.nombre(),e);
                }
            }
            indice++;
        }
    }

    private static String notaSalida(FamiliaOlfativa familia) {
        return switch(familia) {
            case FLORAL -> "Bergamota y pétalos frescos";
            case CITRICA -> "Bergamota, limón y mandarina";
            case AMADERADA -> "Pimienta rosa y cítricos";
            case ORIENTAL -> "Especias cálidas y frutos rojos";
            case GOURMAND -> "Frutas dulces y almendra";
        };
    }
    private static String notaCorazon(FamiliaOlfativa familia) {
        return switch(familia) {
            case FLORAL -> "Jazmín, rosa y peonía";
            case CITRICA -> "Lavanda y notas marinas";
            case AMADERADA -> "Cedro, vetiver y lavanda";
            case ORIENTAL -> "Ámbar, canela y flores blancas";
            case GOURMAND -> "Praliné, cacao y jazmín";
        };
    }
    private static String notaFondo(FamiliaOlfativa familia) {
        return switch(familia) {
            case FLORAL -> "Almizcle blanco y sándalo";
            case CITRICA -> "Almizcle, cedro y ámbar";
            case AMADERADA -> "Sándalo, pachulí y almizcle";
            case ORIENTAL -> "Vainilla, haba tonka y maderas";
            case GOURMAND -> "Vainilla, caramelo y haba tonka";
        };
    }

    private static List<ProductoCatalogo> catalogoVisual() {
        return List.of(
            new ProductoCatalogo("cristal","Cristal","Omnia Crystalline de Bvlgari",FamiliaOlfativa.FLORAL,"cristal.jpeg"),
            new ProductoCatalogo("bruna","Bruna","CK IN2U for Her de Calvin Klein",FamiliaOlfativa.FLORAL,"bruna.jpeg"),
            new ProductoCatalogo("fifi","Fifi","Euphoria de Calvin Klein",FamiliaOlfativa.ORIENTAL,"fifi.jpeg"),
            new ProductoCatalogo("citadine","Citadine","212 Women de Carolina Herrera",FamiliaOlfativa.FLORAL,"citadine.jpeg"),
            new ProductoCatalogo("pattino","Pattino","212 Heroes Forever Young de Carolina Herrera",FamiliaOlfativa.FLORAL,"pattino.jpeg"),
            new ProductoCatalogo("empire","Empire","212 Sexy for Her de Carolina Herrera",FamiliaOlfativa.ORIENTAL,"empire.jpeg"),
            new ProductoCatalogo("private","Private","212 VIP for Her de Carolina Herrera",FamiliaOlfativa.ORIENTAL,"private.jpeg"),
            new ProductoCatalogo("privex","Privex","212 VIP Rosé Extra de Carolina Herrera",FamiliaOlfativa.FLORAL,"privex.jpeg"),
            new ProductoCatalogo("privrose","Privrose","212 VIP Rosé de Carolina Herrera",FamiliaOlfativa.FLORAL,"privrose.jpeg"),
            new ProductoCatalogo("caro","Caro","Carolina Herrera for Her",FamiliaOlfativa.FLORAL,"caro.jpeg"),
            new ProductoCatalogo("carlina","Carlina","CH for Her de Carolina Herrera",FamiliaOlfativa.ORIENTAL,"carlina.jpeg"),
            new ProductoCatalogo("bonne","Bonne","Good Girl de Carolina Herrera",FamiliaOlfativa.ORIENTAL,"bonne.jpeg"),
            new ProductoCatalogo("louise","Louise","Good Girl Légère de Carolina Herrera",FamiliaOlfativa.FLORAL,"louise.jpeg"),
            new ProductoCatalogo("bomb","Bomb","La Bomba de Carolina Herrera",FamiliaOlfativa.GOURMAND,"bomb.jpeg"),
            new ProductoCatalogo("jacki","Jacki","Chance Eau Fraîche de Chanel",FamiliaOlfativa.CITRICA,"jacki.jpeg"),
            new ProductoCatalogo("animali","Animali","Animale for Men",FamiliaOlfativa.AMADERADA,"animali.jpeg"),
            new ProductoCatalogo("atraccion","Atracción","Blue Seduction de Antonio Banderas",FamiliaOlfativa.CITRICA,"atraccion.jpeg"),
            new ProductoCatalogo("loris","Loris","The Most Wanted de Azzaro",FamiliaOlfativa.ORIENTAL,"loris.jpeg"),
            new ProductoCatalogo("acuatico","Acuático","Aqva Pour Homme de Bvlgari",FamiliaOlfativa.CITRICA,"acuatico.jpeg"),
            new ProductoCatalogo("jacques","Jacques","Aqva Pour Homme Marine de Bvlgari",FamiliaOlfativa.CITRICA,"jacques.jpeg"),
            new ProductoCatalogo("albert","Albert","BLV Pour Homme de Bvlgari",FamiliaOlfativa.AMADERADA,"albert.jpeg"),
            new ProductoCatalogo("rome","Rome","Bvlgari Man Extreme",FamiliaOlfativa.AMADERADA,"rome.jpeg"),
            new ProductoCatalogo("vulcano","Vulcano","Man in Black de Bvlgari",FamiliaOlfativa.ORIENTAL,"vulcano.jpeg"),
            new ProductoCatalogo("richard","Richard","CK Be de Calvin Klein",FamiliaOlfativa.AMADERADA,"richard.jpeg"),
            new ProductoCatalogo("single","Single","CK One de Calvin Klein",FamiliaOlfativa.CITRICA,"single.jpeg"),
            new ProductoCatalogo("infinite","Infinite","Eternity for Men de Calvin Klein",FamiliaOlfativa.AMADERADA,"infinite.jpeg"),
            new ProductoCatalogo("skate","Skate","212 Heroes de Carolina Herrera",FamiliaOlfativa.CITRICA,"skate.jpeg"),
            new ProductoCatalogo("cosmopolitan","Cosmopolitan","212 Men de Carolina Herrera",FamiliaOlfativa.AMADERADA,"cosmopolitan.jpeg"),
            new ProductoCatalogo("moma","Moma","212 Men versión 2 de Carolina Herrera",FamiliaOlfativa.AMADERADA,"moma.jpeg"),
            new ProductoCatalogo("state","State","212 Sexy Men de Carolina Herrera",FamiliaOlfativa.ORIENTAL,"state.jpeg"),
            new ProductoCatalogo("gaspard","Gaspard","Bleu de Chanel",FamiliaOlfativa.AMADERADA,"gaspard.jpeg"),
            new ProductoCatalogo("johnny","Johnny","Sauvage Eau de Parfum de Dior",FamiliaOlfativa.AMADERADA,"johnny.jpeg"),
            new ProductoCatalogo("gold-man","Gold Man","1 Million de Paco Rabanne",FamiliaOlfativa.ORIENTAL,"gold-man.jpeg"),
            new ProductoCatalogo("brian","Brian","Eros de Versace",FamiliaOlfativa.ORIENTAL,"brian.jpeg"),
            new ProductoCatalogo("giorgio","Giorgio","Acqua di Giò Pour Homme de Giorgio Armani",FamiliaOlfativa.CITRICA,"giorgio.jpeg"),
            new ProductoCatalogo("chris","Chris","Boss Bottled de Hugo Boss",FamiliaOlfativa.AMADERADA,"chris.jpeg"),
            new ProductoCatalogo("invencible","Invencible","Invictus de Paco Rabanne",FamiliaOlfativa.CITRICA,"invencible.jpeg"),
            new ProductoCatalogo("terra","Terra","Terre d’Hermès",FamiliaOlfativa.AMADERADA,"terra.jpeg"),
            new ProductoCatalogo("mariner","Mariner","Le Male de Jean Paul Gaultier",FamiliaOlfativa.ORIENTAL,"mariner.jpeg"),
            new ProductoCatalogo("jordi-pr","Jordi PR","Explorer de Montblanc",FamiliaOlfativa.AMADERADA,"jordi-pr.jpeg"),
            new ProductoCatalogo("sauge","Sauge","Y Eau de Parfum de Yves Saint Laurent",FamiliaOlfativa.AMADERADA,"sauge.jpeg"),
            new ProductoCatalogo("prada","Prada","Luna Rossa Carbon de Prada",FamiliaOlfativa.AMADERADA,"prada.jpeg"),
            new ProductoCatalogo("extremo","Extremo","Spicebomb Extreme de Viktor & Rolf",FamiliaOlfativa.ORIENTAL,"extremo.jpeg"),
            new ProductoCatalogo("codice","Codice","Armani Code Absolu de Giorgio Armani",FamiliaOlfativa.ORIENTAL,"codice.jpeg"),
            new ProductoCatalogo("roja","Roja","Style in Play de Lacoste",FamiliaOlfativa.CITRICA,"roja.jpeg"),
            new ProductoCatalogo("degrees","Degrees","Fahrenheit de Dior",FamiliaOlfativa.ORIENTAL,"degrees.jpeg"),
            new ProductoCatalogo("alex","Alex","Only The Brave de Diesel",FamiliaOlfativa.AMADERADA,"alex.jpeg"),
            new ProductoCatalogo("unico","Único","The One for Men de Dolce & Gabbana",FamiliaOlfativa.ORIENTAL,"unico.jpeg"),
            new ProductoCatalogo("chef","Chef","Hugo de Hugo Boss",FamiliaOlfativa.CITRICA,"chef.jpeg"),
            new ProductoCatalogo("sette","Sette","Loewe 7 de Loewe",FamiliaOlfativa.AMADERADA,"sette.jpeg")
        );
    }
}
