package com.uniquindio.Ecomerce_PureObsession.application.port.out;
import com.uniquindio.Ecomerce_PureObsession.application.model.Marketplace.*;
import com.uniquindio.Ecomerce_PureObsession.application.dto.response.PedidoResponse;
import com.uniquindio.Ecomerce_PureObsession.domain.repository.Pagina;
import java.util.*;
import java.time.Instant;
import java.math.BigDecimal;
public interface GestionPedidosRepository {
    record Pago(String referencia,BigDecimal importe,boolean simulado,Instant registradoEn) {}
    record Detalle(PedidoResponse pedido,Instant creadoEn,Instant reservaVenceEn,String motivoCancelacion,Pago pago,List<Envio> envios) {}
    Detalle detalle(UUID pedido);
    Detalle confirmar(UUID pedido,String referencia,boolean simulado);
    Detalle enviar(UUID pedido,UUID vendedor,String transportadora,String guia);
    Detalle recibir(UUID pedido,UUID vendedor);
    Pagina<Venta> ventas(UUID vendedor,int pagina,int tamanio);
    Pagina<Detalle> administrar(int pagina,int tamanio);
    int expirar();
}
