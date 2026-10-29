package co.edu.uniquindio.sga.infrastructure.rest;

import co.edu.uniquindio.sga.application.dto.Pagina;
import co.edu.uniquindio.sga.application.dto.request.PublicarPoliticaRequest;
import co.edu.uniquindio.sga.application.dto.response.PoliticaCancelacionResponse;
import co.edu.uniquindio.sga.application.usecase.GestionarPoliticaCancelacionUseCase;
import co.edu.uniquindio.sga.domain.entity.PoliticaCancelacion;
import co.edu.uniquindio.sga.infrastructure.rest.mapper.CatalogoRestMapper;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.time.Clock;
import java.time.LocalDate;

/** F-07 y 7.4: política de cancelación versionada. Publicar una versión nunca sobrescribe las anteriores. */
@RestController
@RequestMapping("/api/v1/politica-cancelacion")
public class PoliticaCancelacionController {

    private final GestionarPoliticaCancelacionUseCase politicaUseCase;
    private final CatalogoRestMapper mapper;
    private final Clock reloj;

    public PoliticaCancelacionController(GestionarPoliticaCancelacionUseCase politicaUseCase, CatalogoRestMapper mapper,
                                         Clock reloj) {
        this.politicaUseCase = politicaUseCase;
        this.mapper = mapper;
        this.reloj = reloj;
    }

    @GetMapping("/vigente")
    public ResponseEntity<PoliticaCancelacionResponse> vigente() {
        return ResponseEntity.ok(mapper.aPolitica(politicaUseCase.vigente(LocalDate.now(reloj))));
    }

    @GetMapping("/versiones")
    public ResponseEntity<Pagina<PoliticaCancelacionResponse>> versiones(@RequestParam(defaultValue = "0") int pagina) {
        return ResponseEntity.ok(Pagina.de(politicaUseCase.versiones(), pagina).mapear(mapper::aPolitica));
    }

    @GetMapping("/versiones/{numero}")
    public ResponseEntity<PoliticaCancelacionResponse> version(@PathVariable int numero) {
        return ResponseEntity.ok(mapper.aPolitica(politicaUseCase.version(numero)));
    }

    @PostMapping("/versiones")
    public ResponseEntity<PoliticaCancelacionResponse> publicar(@Valid @RequestBody PublicarPoliticaRequest request) {
        PoliticaCancelacion nueva = politicaUseCase.publicarVersion(mapper.aTramos(request.tramos()),
                mapper.aPorcentaje(request.retencionNoShow()), request.vigenteDesde());
        return ResponseEntity.created(URI.create("/api/v1/politica-cancelacion/versiones/" + nueva.getVersion().numero()))
                .body(mapper.aPolitica(nueva));
    }
}
