package com.uniquindio.Ecomerce_PureObsession.application.usecase;
import com.uniquindio.Ecomerce_PureObsession.application.port.out.PerfilRepository;
import com.uniquindio.Ecomerce_PureObsession.application.port.out.PerfilRepository.*;
import java.util.*;
public final class PerfilUseCase {
    private final PerfilRepository repository;
    public PerfilUseCase(PerfilRepository repository) {this.repository=repository;}
    public Perfil perfil(String actor){return repository.perfil(actor);}
    public Perfil guardar(String actor,DatosPerfil datos){return repository.guardar(actor,datos);}
    public List<Direccion> direcciones(String actor){return repository.direcciones(actor);}
    public Direccion direccion(String actor,UUID id,Direccion datos){return repository.direccion(actor,id,datos);}
    public void eliminarDireccion(String actor,UUID id){repository.eliminarDireccion(actor,id);}
    public void cambiarClave(String actor,String actual,String nueva){repository.cambiarClave(actor,actual,nueva);}
    public String recuperar(String email){return repository.recuperar(email);}
    public void restablecer(String token,String nueva){repository.restablecer(token,nueva);}
}
