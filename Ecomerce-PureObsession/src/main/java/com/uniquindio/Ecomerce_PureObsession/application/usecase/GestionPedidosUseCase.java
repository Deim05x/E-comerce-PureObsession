package com.uniquindio.Ecomerce_PureObsession.application.usecase;
import com.uniquindio.Ecomerce_PureObsession.application.model.Marketplace.Venta;
import com.uniquindio.Ecomerce_PureObsession.application.port.in.ConsultarPedidoPort;
import com.uniquindio.Ecomerce_PureObsession.application.port.out.*;
import com.uniquindio.Ecomerce_PureObsession.application.port.out.GestionPedidosRepository.Detalle;
import com.uniquindio.Ecomerce_PureObsession.domain.entity.Cuenta;
import com.uniquindio.Ecomerce_PureObsession.domain.valueObject.Rol;
import com.uniquindio.Ecomerce_PureObsession.domain.repository.Pagina;
import com.uniquindio.Ecomerce_PureObsession.domain.exception.ReglaDominioException;
import java.util.UUID;
public final class GestionPedidosUseCase {
    private final CuentaRepository cuentas;private final ConsultarPedidoPort consultas;private final GestionPedidosRepository gestion;
    public GestionPedidosUseCase(CuentaRepository cuentas,ConsultarPedidoPort consultas,GestionPedidosRepository gestion) {this.cuentas=cuentas;this.consultas=consultas;this.gestion=gestion;}
    private Cuenta rol(String actor,Rol rol) {
        Cuenta c=cuentas.buscarPorEmail(actor).orElseThrow(()->new ReglaDominioException("La cuenta no existe."));
        if(c.getRol()!=rol) throw new ReglaDominioException("Rol no autorizado.");return c;
    }
    public Detalle detalle(UUID id,String actor,String clave) {consultas.consultar(id,actor,clave);return gestion.detalle(id);}
    public Detalle confirmar(UUID id,String actor,String referencia) {rol(actor,Rol.ADMIN);return gestion.confirmar(id,referencia,false);}
    public Detalle simular(UUID id,String actor,String clave) {consultas.consultar(id,actor,clave);return gestion.confirmar(id,"DEMO-"+id,true);}
    public void enviar(UUID id,String actor,String transportadora,String guia) {
        UUID vendedor=rol(actor,Rol.VENDEDOR).getId();gestion.enviar(id,vendedor,transportadora,guia);
        // El vendedor recibe solo su venta, nunca el pedido completo de otros vendedores.
    }
    public Detalle recibir(UUID id,UUID vendedor,String actor,String clave) {consultas.consultar(id,actor,clave);return gestion.recibir(id,vendedor);}
    public Pagina<Venta> ventas(String actor,int pagina,int tamanio) {Pagina.validar(pagina,tamanio);return gestion.ventas(rol(actor,Rol.VENDEDOR).getId(),pagina,tamanio);}
    public Pagina<Detalle> administrar(String actor,int pagina,int tamanio) {rol(actor,Rol.ADMIN);Pagina.validar(pagina,tamanio);return gestion.administrar(pagina,tamanio);}
}
