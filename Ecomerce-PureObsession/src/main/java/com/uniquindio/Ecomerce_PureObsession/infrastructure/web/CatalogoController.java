package com.uniquindio.Ecomerce_PureObsession.infrastructure.web;
import com.uniquindio.Ecomerce_PureObsession.application.port.in.*;
import com.uniquindio.Ecomerce_PureObsession.application.dto.request.*;
import com.uniquindio.Ecomerce_PureObsession.application.dto.response.*;
import com.uniquindio.Ecomerce_PureObsession.domain.valueObject.FamiliaOlfativa;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController
@RequestMapping("/api/catalogo")
public class CatalogoController {
    private final PresentacionesPort presentaciones;
    private final ConsultarCatalogoPort fragancias;
    public CatalogoController(PresentacionesPort p,ConsultarCatalogoPort f){presentaciones=p;fragancias=f;}
    @GetMapping public List<PresentacionResponse> listar(){return presentaciones.catalogo();}
    @GetMapping("/fragancias") public List<FraganciaCatalogoResponse> fragancias(@RequestParam(required=false) FamiliaOlfativa familia){
        return fragancias.ejecutar(new ConsultarCatalogoRequest(familia));
    }
}

