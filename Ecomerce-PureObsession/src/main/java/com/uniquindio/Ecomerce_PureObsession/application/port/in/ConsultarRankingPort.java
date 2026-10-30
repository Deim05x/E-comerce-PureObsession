package com.uniquindio.Ecomerce_PureObsession.application.port.in;

import com.uniquindio.Ecomerce_PureObsession.domain.repository.RankingFragancia;
import java.util.List;

public interface ConsultarRankingPort {
    List<RankingFragancia> ejecutar(int limite);
}
