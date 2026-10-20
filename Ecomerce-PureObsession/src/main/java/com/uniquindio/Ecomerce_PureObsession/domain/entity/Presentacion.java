package com.uniquindio.Ecomerce_PureObsession.domain.entity;
import com.uniquindio.Ecomerce_PureObsession.domain.exception.ReglaDominioException;
import com.uniquindio.Ecomerce_PureObsession.domain.valueObject.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

public final class Presentacion {
    private final UUID id, vendedorId, fraganciaId, botellaMadreId;
    private final TipoPresentacion tipo;
    private final int volumenMl;
    private final BigDecimal precio;
    private final OrigenProducto origen;
    private final String paisOrigen;
    private final boolean original;
    private boolean publicada;

    private Presentacion(UUID id, UUID vendedorId, UUID fraganciaId, UUID botellaMadreId,
            TipoPresentacion tipo, int volumenMl, BigDecimal precio, OrigenProducto origen,
            String paisOrigen, boolean original, boolean publicada) {
        this.id=id; this.vendedorId=vendedorId; this.fraganciaId=fraganciaId; this.botellaMadreId=botellaMadreId;
        this.tipo=tipo; this.volumenMl=volumenMl; this.precio=precio; this.origen=origen;
        this.paisOrigen=paisOrigen; this.original=original; this.publicada=publicada;
    }
    public static Presentacion crear(UUID id, UUID vendedorId, UUID fraganciaId, UUID botellaMadreId,
            TipoPresentacion tipo, int volumenMl, BigDecimal precio, OrigenProducto origen,
            String paisOrigen, boolean original) {
        validar(id,vendedorId,fraganciaId,botellaMadreId,tipo,volumenMl,origen,paisOrigen);
        return new Presentacion(id,vendedorId,fraganciaId,botellaMadreId,tipo,volumenMl,
            validarPrecio(precio),origen,paisOrigen.trim().toUpperCase(Locale.ROOT),original,false);
    }
    private static void validar(UUID id, UUID vendedorId, UUID fraganciaId, UUID botellaMadreId,
            TipoPresentacion tipo, int ml, OrigenProducto origen, String pais) {
        if(id==null || vendedorId==null || fraganciaId==null || tipo==null || origen==null)
            throw new ReglaDominioException("Identidades, tipo y origen son obligatorios.");
        if(ml<=0) throw new ReglaDominioException("El volumen debe ser positivo.");
        if(tipo==TipoPresentacion.DECANT && botellaMadreId==null)
            throw new ReglaDominioException("Un decant debe identificar su botella madre.");
        if(pais==null) throw new ReglaDominioException("El país de origen es obligatorio.");
        String codigo=pais.trim().toUpperCase(Locale.ROOT);
        if(!Set.of(Locale.getISOCountries()).contains(codigo))
            throw new ReglaDominioException("Use un país ISO de dos letras.");
        if((origen==OrigenProducto.NACIONAL) != codigo.equals("CO"))
            throw new ReglaDominioException("Nacional corresponde a Colombia; importado debe indicar otro país.");
    }
    public static BigDecimal validarPrecio(BigDecimal precio) {
        if(precio==null || precio.signum()<=0 || precio.precision()-precio.scale()>12)
            throw new ReglaDominioException("El precio debe ser positivo y admitir como máximo 12 enteros.");
        try { return precio.setScale(2,RoundingMode.UNNECESSARY); }
        catch(ArithmeticException e) { throw new ReglaDominioException("El precio admite máximo dos decimales."); }
    }
    public void publicar(boolean certificadoAprobado) {
        if(original && !certificadoAprobado)
            throw new ReglaDominioException("Una presentación original necesita certificado aprobado.");
        publicada=true;
    }
    public void despublicar() { publicada=false; }
    public UUID getId(){return id;} public UUID getVendedorId(){return vendedorId;}
    public UUID getFraganciaId(){return fraganciaId;} public UUID getBotellaMadreId(){return botellaMadreId;}
    public TipoPresentacion getTipo(){return tipo;} public int getVolumenMl(){return volumenMl;}
    public BigDecimal getPrecio(){return precio;} public OrigenProducto getOrigen(){return origen;}
    public String getPaisOrigen(){return paisOrigen;} public boolean isOriginal(){return original;}
    public boolean isPublicada(){return publicada;}
}

