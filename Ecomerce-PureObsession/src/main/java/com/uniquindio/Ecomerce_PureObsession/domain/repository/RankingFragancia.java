package com.uniquindio.Ecomerce_PureObsession.domain.repository;

import java.util.UUID;

/** Unidades de presentaciones vendidas, no mililitros ni valor monetario. */
public record RankingFragancia(UUID fraganciaId, String nombre, long cantidadVendida) {}
