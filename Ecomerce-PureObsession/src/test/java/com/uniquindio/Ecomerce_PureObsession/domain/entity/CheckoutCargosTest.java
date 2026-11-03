package com.uniquindio.Ecomerce_PureObsession.domain.entity;
import com.uniquindio.Ecomerce_PureObsession.domain.valueObject.Precio;
import com.uniquindio.Ecomerce_PureObsession.domain.exception.ReglaDominioException;
import java.math.BigDecimal;
import java.util.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class CheckoutCargosTest {
    @Test void dineroYCargosConservanDecimalesExactosYNoCambianTrasConfirmar() {
        var linea=LineaPedido.crear(UUID.randomUUID(),UUID.randomUUID(),UUID.randomUUID(),"Perfume",Precio.de(new BigDecimal("10.15"),"COP"),3);
        Pedido p=Pedido.crear(UUID.randomUUID(),UUID.randomUUID(),List.of(linea));
        p.aplicarCargos(new BigDecimal("15000"),new BigDecimal("15000"));
        assertEquals(new BigDecimal("30030.45"),p.getTotal().monto());
        p.confirmar();assertThrows(ReglaDominioException.class,()->p.aplicarCargos(BigDecimal.ZERO,BigDecimal.ZERO));
        Pedido copia=Pedido.conCargosReconstituidos(p,p.getEnvio(),p.getRegalo());
        assertEquals(p.getTotal(),copia.getTotal());assertEquals(p.getEstado(),copia.getEstado());
    }
}
