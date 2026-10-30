package com.uniquindio.Ecomerce_PureObsession.application.usecase;

import com.uniquindio.Ecomerce_PureObsession.application.port.in.ConsultarRankingPort;
import com.uniquindio.Ecomerce_PureObsession.application.port.out.ConsultaCatalogoRepository;
import com.uniquindio.Ecomerce_PureObsession.domain.repository.RankingFragancia;
import com.uniquindio.Ecomerce_PureObsession.domain.exception.ReglaDominioException;
import java.util.List;

public final class ConsultarRankingUseCase implements ConsultarRankingPort {
    private final ConsultaCatalogoRepository consultas;
    public ConsultarRankingUseCase(ConsultaCatalogoRepository consultas) { this.consultas=consultas; }
    public List<RankingFragancia> ejecutar(int limite) {
        if(limite<1 || limite>100) throw new ReglaDominioException("El limite del ranking debe estar entre 1 y 100.");
        return consultas.ranking(limite);
    }
}
