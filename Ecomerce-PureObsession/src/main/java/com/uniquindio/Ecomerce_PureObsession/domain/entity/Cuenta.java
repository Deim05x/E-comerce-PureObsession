package com.uniquindio.Ecomerce_PureObsession.domain.entity;
import com.uniquindio.Ecomerce_PureObsession.domain.valueObject.Rol;
import com.uniquindio.Ecomerce_PureObsession.domain.exception.ReglaDominioException;
import java.util.*;
public final class Cuenta {
    private final UUID id;
    private final String nombre, cedula, email, claveHash;
    private final Rol rol;
    private Cuenta(UUID id,String nombre,String cedula,String email,String claveHash,Rol rol) {
        this.id=id; this.nombre=nombre; this.cedula=cedula; this.email=email; this.claveHash=claveHash; this.rol=rol;
    }
    public static Cuenta crear(UUID id,String nombre,String cedula,String email,String hash,Rol rol) {
        if(id==null || nombre==null || nombre.isBlank() || cedula==null || !cedula.matches("[0-9]{5,15}")
            || email==null || !email.matches("[^\\s@]+@[^\\s@]+\\.[^\\s@]+") || hash==null || hash.isBlank() || rol==null)
            throw new ReglaDominioException("Los datos de la cuenta no son válidos.");
        return new Cuenta(id,nombre.trim(),cedula,email.trim().toLowerCase(Locale.ROOT),hash,rol);
    }
    public UUID getId(){return id;} public String getNombre(){return nombre;}
    public String getCedula(){return cedula;} public String getEmail(){return email;}
    public String getClaveHash(){return claveHash;} public Rol getRol(){return rol;}
}

