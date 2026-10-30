package com.uniquindio.Ecomerce_PureObsession.infrastructure.web;

import com.uniquindio.Ecomerce_PureObsession.application.port.in.*;
import com.uniquindio.Ecomerce_PureObsession.application.dto.response.*;
import com.uniquindio.Ecomerce_PureObsession.domain.repository.*;
import com.uniquindio.Ecomerce_PureObsession.domain.valueObject.FamiliaOlfativa;
import com.uniquindio.Ecomerce_PureObsession.infrastructure.web.mapper.PresentacionWebMapper;
import java.util.List;
import org.springframework.web.bind.annotation.*;

/** Rutas paginadas aditivas: mantiene los contratos de lista de la API anterior. */
@RestController
@RequestMapping("/api/catalogo")
public class ConsultasCatalogoController {
    private final ConsultarCatalogoPaginadoPort catalogo;
    private final ConsultarRankingPort ranking;
    private final PresentacionWebMapper mapper;
    public ConsultasCatalogoController(ConsultarCatalogoPaginadoPort catalogo, ConsultarRankingPort ranking, PresentacionWebMapper mapper) {
        this.catalogo=catalogo; this.ranking=ranking; this.mapper=mapper;
    }
    @GetMapping("/pagina")
    public Pagina<PresentacionResponse> presentaciones(@RequestParam(required=false) String texto,
            @RequestParam(required=false) FamiliaOlfativa familia,
            @RequestParam(defaultValue="0") int pagina, @RequestParam(defaultValue="20") int tamanio) {
        return catalogo.presentaciones(texto,familia,pagina,tamanio).mapear(mapper::aRespuesta);
    }
    @GetMapping("/fragancias/pagina")
    public Pagina<FraganciaCatalogoResponse> fragancias(@RequestParam(required=false) String texto,
            @RequestParam(required=false) FamiliaOlfativa familia,
            @RequestParam(defaultValue="0") int pagina, @RequestParam(defaultValue="20") int tamanio) {
        return catalogo.fragancias(texto,familia,pagina,tamanio).mapear(f ->
                new FraganciaCatalogoResponse(f.getId(),f.getNombre(),f.getFamiliaOlfativa(),f.getConcentracion()));
    }
    @GetMapping("/ranking")
    public List<RankingFragancia> ranking(@RequestParam(defaultValue="10") int limite) {
        return ranking.ejecutar(limite);
    }
}
