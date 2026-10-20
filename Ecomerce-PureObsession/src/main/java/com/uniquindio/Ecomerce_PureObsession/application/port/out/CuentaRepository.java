package com.uniquindio.Ecomerce_PureObsession.application.port.out;
import com.uniquindio.Ecomerce_PureObsession.domain.entity.Cuenta;
import java.util.*;
public interface CuentaRepository {
    Cuenta guardar(Cuenta cuenta);
    Optional<Cuenta> buscarPorEmail(String email);
    Optional<Cuenta> buscarPorId(UUID id);
    boolean existeCedula(String cedula);
}

