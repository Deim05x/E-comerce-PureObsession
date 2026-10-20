package com.uniquindio.Ecomerce_PureObsession;
import com.uniquindio.Ecomerce_PureObsession.domain.entity.*;
import com.uniquindio.Ecomerce_PureObsession.domain.valueObject.*;
import java.util.*;
public final class DatosPrueba {
    private DatosPrueba() {}
    public static Fragancia fragancia() { return Fragancia.crear(UUID.randomUUID(), "Sauvage", FamiliaOlfativa.AMADERADA,
        Concentracion.EAU_DE_PARFUM, PiramideOlfativa.crear("Bergamota", "Lavanda", "Ambar")); }
    public static BotellaMadre botella() { return BotellaMadre.crear(UUID.randomUUID(), fragancia(), Volumen.crear(100)); }
    public static LineaPedido linea(UUID fraganciaId, String moneda) {
        return LineaPedido.crear(UUID.randomUUID(), UUID.randomUUID(), fraganciaId, "Decant 10 ml", Precio.crear(100.0, moneda), 1);
    }
}
