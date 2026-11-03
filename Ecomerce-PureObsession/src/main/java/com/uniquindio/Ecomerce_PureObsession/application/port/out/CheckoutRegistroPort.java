package com.uniquindio.Ecomerce_PureObsession.application.port.out;
import com.uniquindio.Ecomerce_PureObsession.application.dto.response.PedidoResponse;
import java.util.function.Supplier;
public interface CheckoutRegistroPort {
    PedidoResponse ejecutar(String actor,String clave,String huella,Supplier<PedidoResponse> compra);
}
