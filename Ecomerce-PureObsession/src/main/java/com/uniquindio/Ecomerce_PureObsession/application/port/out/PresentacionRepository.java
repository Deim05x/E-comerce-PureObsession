package com.uniquindio.Ecomerce_PureObsession.application.port.out;
import com.uniquindio.Ecomerce_PureObsession.domain.entity.Presentacion;
import java.util.*;
public interface PresentacionRepository {
    Presentacion guardar(Presentacion presentacion);
    Optional<Presentacion> buscarPorId(UUID id);
    List<Presentacion> listarPublicadas();
    boolean estaEnCatalogo(UUID id);
}

