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
        if(em.find(CuentaJpa.class,id("cliente"))!=null) return;
        cuenta("admin","Administracion demo","90000001",Rol.ADMIN);
        cuenta("vendedor","Vendedor demo","90000002",Rol.VENDEDOR);
        cuenta("vendedor2","Segundo vendedor demo","90000003",Rol.VENDEDOR);
        Cuenta cliente=cuenta("cliente","Cliente demo","90000004",Rol.CLIENTE);
        Presentacion rosa=oferta("rosa","Rosa de Jardin",FamiliaOlfativa.FLORAL,"vendedor","20000.00");
        Presentacion cedro=oferta("cedro","Cedro Nocturno",FamiliaOlfativa.AMADERADA,"vendedor2","25000.00");
        Presentacion brisa=oferta("brisa","Brisa Citrica",FamiliaOlfativa.CITRICA,"vendedor","15000.00");
        // Mismo producto y tamano con precio de otro vendedor, sobre el inventario central actual.
        Presentacion alternativa=Presentacion.crear(id("oferta-rosa-alternativa"),id("vendedor2"),rosa.getFraganciaId(),
                rosa.getBotellaMadreId(),TipoPresentacion.DECANT,10,new BigDecimal("18000.00"),OrigenProducto.NACIONAL,"CO",false);
        alternativa.publicar(false); presentaciones.guardar(alternativa);
        pedido("confirmado",cliente,rosa,3,EstadoPedido.CONFIRMADO);
        pedido("entregado",cliente,rosa,2,EstadoPedido.ENTREGADO);
        pedido("enviado",cliente,cedro,2,EstadoPedido.ENVIADO);
        pedido("creado",cliente,brisa,1,EstadoPedido.CREADO);
        pedido("cancelado",cliente,brisa,8,EstadoPedido.CANCELADO);
    }

    private Cuenta cuenta(String clave,String nombre,String cedula,Rol rol) {
        return cuentas.guardar(Cuenta.crear(id(clave),nombre,cedula,clave+"@pureobsession.demo",
                encoder.encode("DemoPure2026!"),rol));
    }
    private Presentacion oferta(String clave,String nombre,FamiliaOlfativa familia,String vendedor,String precio) {
        Fragancia f=Fragancia.crear(id("fragancia-"+clave),nombre,familia,Concentracion.EAU_DE_PARFUM,
                PiramideOlfativa.crear("Bergamota","Jazmin","Almizcle")); f.publicar();
        BotellaMadre b=BotellaMadre.crear(id("botella-"+clave),f,Volumen.crear(500)); botellas.guardar(b);
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
}
