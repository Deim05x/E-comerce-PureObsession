package com.uniquindio.Ecomerce_PureObsession.infrastructure.persistence;

import com.uniquindio.Ecomerce_PureObsession.domain.entity.BotellaMadre;
import com.uniquindio.Ecomerce_PureObsession.domain.repository.BotellaMadreRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class BotellaMadreRepositoryEnMemoria implements BotellaMadreRepository {

    private final Map<UUID, BotellaMadre> almacen = new HashMap<>();

    @Override
    public BotellaMadre guardar(BotellaMadre botellaMadre) {
        almacen.put(botellaMadre.getId(), botellaMadre);
        return botellaMadre;
    }

    @Override
    public Optional<BotellaMadre> buscarPorId(UUID id) {
        return Optional.ofNullable(almacen.get(id));
    }

    @Override
    public List<BotellaMadre> listarTodas() {
        return new ArrayList<>(almacen.values());
    }

    @Override
    public void eliminar(UUID id) {
        almacen.remove(id);
    }
}
