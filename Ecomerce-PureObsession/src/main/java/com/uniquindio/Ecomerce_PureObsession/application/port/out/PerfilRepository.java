package com.uniquindio.Ecomerce_PureObsession.application.port.out;
import java.util.*;
import java.time.LocalDate;
import com.uniquindio.Ecomerce_PureObsession.domain.valueObject.Rol;
public interface PerfilRepository {
    record Perfil(UUID id,String nombre,String email,Rol rol,String telefono,LocalDate nacimiento) {}
    record DatosPerfil(String nombre,String telefono,LocalDate nacimiento) {}
    record Direccion(UUID id,String etiqueta,String receptor,String telefono,String departamento,String ciudad,String direccion,String indicaciones,boolean predeterminada) {}
    Perfil perfil(String actor);
    Perfil guardar(String actor,DatosPerfil datos);
    List<Direccion> direcciones(String actor);
    Direccion direccion(String actor,UUID id,Direccion datos);
    void eliminarDireccion(String actor,UUID id);
    void cambiarClave(String actor,String actual,String nueva);
    String recuperar(String email);
    void restablecer(String token,String nueva);
}
