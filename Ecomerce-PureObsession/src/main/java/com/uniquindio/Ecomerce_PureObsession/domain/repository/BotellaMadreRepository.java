package com.uniquindio.Ecomerce_PureObsession.domain.repository;

import com.uniquindio.Ecomerce_PureObsession.domain.entity.BotellaMadre;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BotellaMadreRepository {
    BotellaMadre guardar(BotellaMadre botellaMadre);
    Optional<BotellaMadre> buscarPorId(UUID id);
    List<BotellaMadre> listarTodas();
    void eliminar (UUID id);
}
