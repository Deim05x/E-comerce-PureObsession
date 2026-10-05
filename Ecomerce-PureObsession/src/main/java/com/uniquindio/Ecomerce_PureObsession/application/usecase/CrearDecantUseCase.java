package com.uniquindio.Ecomerce_PureObsession.application.usecase;

import com.uniquindio.Ecomerce_PureObsession.application.dto.request.CrearDecantRequest;
import com.uniquindio.Ecomerce_PureObsession.application.dto.response.DecantResponse;
import com.uniquindio.Ecomerce_PureObsession.domain.entity.BotellaMadre;
import com.uniquindio.Ecomerce_PureObsession.domain.entity.Decant;
import com.uniquindio.Ecomerce_PureObsession.domain.exception.ReglaDominioException;
import com.uniquindio.Ecomerce_PureObsession.domain.repository.BotellaMadreRepository;
import com.uniquindio.Ecomerce_PureObsession.domain.valueObject.Volumen;
import java.util.Objects;
import java.util.UUID;

public class CrearDecantUseCase {
    private final BotellaMadreRepository repository;
    public CrearDecantUseCase(BotellaMadreRepository repository) {
        this.repository = Objects.requireNonNull(repository);
    }
    public DecantResponse ejecutar(CrearDecantRequest request) {
        if (request == null || request.botellaMadreId() == null) {
            throw new ReglaDominioException("La solicitud debe identificar la botella madre.");
        }
        BotellaMadre botella = repository.buscarPorId(request.botellaMadreId())
                .orElseThrow(() -> new ReglaDominioException("La botella madre no existe."));
        Decant decant = botella.crearDecant(UUID.randomUUID(), new Volumen(request.volumenMl()));
        repository.guardar(botella);
        return new DecantResponse(decant.getId(), botella.getId(), decant.getFragancia().getId(),
                decant.getFragancia().getNombre(), decant.getFragancia().getConcentracion(),
                decant.getVolumen().mililitros(), botella.getVolumenDisponible().mililitros());
    }
}
