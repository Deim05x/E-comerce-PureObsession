package com.uniquindio.Ecomerce_PureObsession.domain.entity;

import com.uniquindio.Ecomerce_PureObsession.domain.exception.ReglaDominioException;
import com.uniquindio.Ecomerce_PureObsession.domain.valueObject.Concentracion;
import com.uniquindio.Ecomerce_PureObsession.domain.valueObject.Volumen;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PerfumeTest {

    @Test
    void creaDecantYDescuentaElVolumenDeLaBotellaMadre() {
        Perfume perfume = new Perfume("Dior Sauvage", Concentracion.EAU_DE_PARFUM, new Volumen(100));

        Decant decant = perfume.crearDecant(new Volumen(10));

        assertEquals("Dior Sauvage", decant.fragancia());
        assertEquals(Concentracion.EAU_DE_PARFUM, decant.concentracion());
        assertEquals(10, decant.volumen().mililitros());
        assertEquals(90, perfume.getVolumenDisponibleEnMl());
    }

    @Test
    void noCreaUnDecantIgualALaPresentacionOriginal() {
        Perfume perfume = new Perfume("Dior Sauvage", Concentracion.EAU_DE_PARFUM, new Volumen(100));

        assertThrows(ReglaDominioException.class, () -> perfume.crearDecant(new Volumen(100)));
    }

    @Test
    void noCreaUnDecantCuandoNoHayVolumenDisponible() {
        Perfume perfume = new Perfume("Dior Sauvage", Concentracion.EAU_DE_PARFUM, new Volumen(100));
        perfume.crearDecant(new Volumen(60));

        assertThrows(ReglaDominioException.class, () -> perfume.crearDecant(new Volumen(50)));
    }
}
