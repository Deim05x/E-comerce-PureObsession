package com.uniquindio.Ecomerce_PureObsession.application.usecase;
import com.uniquindio.Ecomerce_PureObsession.application.dto.request.*;
import com.uniquindio.Ecomerce_PureObsession.application.dto.response.PedidoResponse;
import com.uniquindio.Ecomerce_PureObsession.application.port.in.ComprarPort;
import com.uniquindio.Ecomerce_PureObsession.application.port.out.*;
import com.uniquindio.Ecomerce_PureObsession.domain.exception.ReglaDominioException;
import com.uniquindio.Ecomerce_PureObsession.domain.valueObject.*;
import java.math.BigDecimal;
import java.util.*;
public final class CheckoutUseCase {
    public record Cotizacion(BigDecimal subtotal,BigDecimal envio,BigDecimal regalo,BigDecimal total,String moneda,long vendedores) {}
    private final ComprarPort compras;private final MarketplaceRepository productos;private final CheckoutRegistroPort registro;
    private final BigDecimal envio,regalo;
    public CheckoutUseCase(ComprarPort compras,MarketplaceRepository productos,CheckoutRegistroPort registro,BigDecimal envio,BigDecimal regalo) {
        this.compras=compras;this.productos=productos;this.registro=registro;this.envio=Precio.de(envio,"COP").monto();this.regalo=Precio.de(regalo,"COP").monto();
    }
    public Cotizacion cotizar(List<ItemCheckoutRequest> items) {
        if(items==null || items.isEmpty() || items.size()>50) throw new ReglaDominioException("Seleccione entre 1 y 50 productos.");
        Set<UUID> ids=new HashSet<>(),vendedores=new HashSet<>();BigDecimal subtotal=BigDecimal.ZERO,costoRegalo=BigDecimal.ZERO;
        for(ItemCheckoutRequest item:items) {
            if(item==null || item.presentacionId()==null || item.cantidad()<1 || item.cantidad()>1000 || !ids.add(item.presentacionId())) throw new ReglaDominioException("Items invalidos o repetidos.");
            var p=productos.producto(item.presentacionId(),null);p.tipo().validarEmpaque(item.empaque());
            if(p.stockDisponible()<item.cantidad()) throw new ReglaDominioException("Stock insuficiente.");
            subtotal=subtotal.add(p.precio().multiply(BigDecimal.valueOf(item.cantidad())));vendedores.add(p.vendedorId());
            if(item.empaque()==TipoEmpaque.REGALO)costoRegalo=costoRegalo.add(regalo.multiply(BigDecimal.valueOf(item.cantidad())));
        }
        BigDecimal costoEnvio=envio.multiply(BigDecimal.valueOf(vendedores.size()));
        return new Cotizacion(subtotal,costoEnvio,costoRegalo,subtotal.add(costoEnvio).add(costoRegalo),"COP",vendedores.size());
    }
    public PedidoResponse comprar(String actor,CrearPedidoRequest datos,String clave) {
        if(datos==null)throw new ReglaDominioException("Solicitud obligatoria.");
        String huella=huella(datos);
        return registro.ejecutar(actor,clave,huella,()->{cotizar(datos.items());return compras.ejecutar(actor,datos);});
    }
    private String huella(CrearPedidoRequest d) {
        StringBuilder s=new StringBuilder();
        for(String valor:List.of(Objects.toString(d.nombre(),""),Objects.toString(d.correo(),""),Objects.toString(d.telefono(),""),Objects.toString(d.direccion(),""))) s.append(valor.length()).append(':').append(valor);
        if(d.items()!=null) d.items().stream().filter(Objects::nonNull).sorted(Comparator.comparing(i->Objects.toString(i.presentacionId(),"")))
                .forEach(i->s.append('|').append(i.presentacionId()).append(':').append(i.cantidad()).append(':').append(i.empaque()));
        try {return HexFormat.of().formatHex(java.security.MessageDigest.getInstance("SHA-256").digest(s.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8)));}
        catch(java.security.NoSuchAlgorithmException e){throw new IllegalStateException(e);}
    }
}
