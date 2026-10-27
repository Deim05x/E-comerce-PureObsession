package com.uniquindio.Ecomerce_PureObsession.infrastructure.web;

import com.uniquindio.Ecomerce_PureObsession.application.dto.request.ConsultarCatalogoRequest;
import com.uniquindio.Ecomerce_PureObsession.application.dto.response.FraganciaCatalogoResponse;
import com.uniquindio.Ecomerce_PureObsession.application.dto.response.PresentacionResponse;
import com.uniquindio.Ecomerce_PureObsession.application.port.in.ConsultarCatalogoPort;
import com.uniquindio.Ecomerce_PureObsession.application.port.in.ConsultarPresentacionesPort;
import com.uniquindio.Ecomerce_PureObsession.domain.valueObject.FamiliaOlfativa;
import com.uniquindio.Ecomerce_PureObsession.infrastructure.web.mapper.PresentacionWebMapper;
import java.util.List;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/catalogo")
public class CatalogoController {
    private final ConsultarPresentacionesPort presentaciones;
    private final ConsultarCatalogoPort fragancias;
    private final PresentacionWebMapper mapper;

    public CatalogoController(ConsultarPresentacionesPort presentaciones,
            ConsultarCatalogoPort fragancias, PresentacionWebMapper mapper) {
        this.presentaciones = presentaciones;
        this.fragancias = fragancias;
        this.mapper = mapper;
    }

    @GetMapping
    public List<PresentacionResponse> listar() {
        return mapper.aRespuestas(presentaciones.listarCatalogo());
    }

    @GetMapping("/fragancias")
    public List<FraganciaCatalogoResponse> fragancias(
            @RequestParam(required = false) FamiliaOlfativa familia) {
        return fragancias.ejecutar(new ConsultarCatalogoRequest(familia));
    }
}
