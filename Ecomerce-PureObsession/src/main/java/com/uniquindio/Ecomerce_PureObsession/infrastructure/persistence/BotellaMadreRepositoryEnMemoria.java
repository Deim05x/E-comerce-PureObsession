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
    private final java.util.Set<UUID> eliminadas = new java.util.HashSet<>();

    @Override
    public BotellaMadre guardar(BotellaMadre botellaMadre) {
        if (eliminadas.contains(botellaMadre.getId()))
            throw new com.uniquindio.Ecomerce_PureObsession.domain.exception.ReglaDominioException("La botella fue eliminada.");
        almacen.put(botellaMadre.getId(), botellaMadre);
        return botellaMadre;
    }

    @Override
    public Optional<BotellaMadre> buscarPorId(UUID id) {
        return eliminadas.contains(id) ? Optional.empty() : Optional.ofNullable(almacen.get(id));
    }

    @Override
    public List<BotellaMadre> listarTodas() {
        return almacen.values().stream().filter(b -> !eliminadas.contains(b.getId())).toList();
    }

    @Override
    public void eliminar(UUID id) {
        if (almacen.containsKey(id)) eliminadas.add(id);
    }
}
