package com.uniquindio.Ecomerce_PureObsession.domain.entity;
import com.uniquindio.Ecomerce_PureObsession.domain.valueObject.*;
import com.uniquindio.Ecomerce_PureObsession.domain.exception.ReglaDominioException;
import org.junit.jupiter.api.Test;
import java.util.UUID;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;
class PresentacionTest {
    private Presentacion crear(int ml,String precio,OrigenProducto origen,String pais,boolean original) {
        return Presentacion.crear(UUID.randomUUID(),UUID.randomUUID(),UUID.randomUUID(),UUID.randomUUID(),
            TipoPresentacion.DECANT,ml,new BigDecimal(precio),origen,pais,original);
    }
    @Test void admiteTamanosVariablesYPreciosIndependientes() {
        assertEquals(new BigDecimal("45000.00"),crear(30,"45000",OrigenProducto.NACIONAL,"CO",false).getPrecio());
        assertEquals(47,crear(47,"73200.50",OrigenProducto.IMPORTADO,"fr",true).getVolumenMl());
        assertEquals(100,crear(100,"150000",OrigenProducto.IMPORTADO,"US",true).getVolumenMl());
    }
    @Test void rechazaPrecioInvalidoSinRedondearSilenciosamente() {
        for(String valor:new String[]{"0","-1","1.001","1000000000000"})
            assertThrows(ReglaDominioException.class,()->Presentacion.validarPrecio(new BigDecimal(valor)));
        assertThrows(ReglaDominioException.class,()->Presentacion.validarPrecio(null));
    }
    @Test void validaOrigenSinInferirCalidad() {
        assertThrows(ReglaDominioException.class,()->crear(30,"1",OrigenProducto.NACIONAL,"FR",false));
        assertThrows(ReglaDominioException.class,()->crear(30,"1",OrigenProducto.IMPORTADO,"CO",false));
        assertThrows(ReglaDominioException.class,()->crear(30,"1",OrigenProducto.IMPORTADO,"XX",false));
    }
    @Test void originalNecesitaCertificadoAprobado() {
        var p=crear(30,"100",OrigenProducto.NACIONAL,"CO",true);
        assertThrows(ReglaDominioException.class,()->p.publicar(false));
        assertFalse(p.isPublicada()); p.publicar(true); assertTrue(p.isPublicada());
    }
    @Test void testerNoAdmiteRegaloPeroOtrasPresentacionesSi() {
        assertThrows(ReglaDominioException.class,()->TipoPresentacion.TESTER.validarEmpaque(TipoEmpaque.REGALO));
        assertDoesNotThrow(()->TipoPresentacion.TESTER.validarEmpaque(TipoEmpaque.ESTANDAR));
        assertDoesNotThrow(()->TipoPresentacion.DECANT.validarEmpaque(TipoEmpaque.REGALO));
        assertThrows(ReglaDominioException.class,()->TipoPresentacion.PERFUME.validarEmpaque(null));
    }
}

