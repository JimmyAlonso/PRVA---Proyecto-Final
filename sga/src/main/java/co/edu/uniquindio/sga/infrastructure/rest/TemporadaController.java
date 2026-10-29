package co.edu.uniquindio.sga.infrastructure.rest;

import co.edu.uniquindio.sga.application.dto.Pagina;
import co.edu.uniquindio.sga.application.dto.request.RedefinirTemporadaRequest;
import co.edu.uniquindio.sga.application.dto.request.RegistrarTemporadaRequest;
import co.edu.uniquindio.sga.application.dto.response.TemporadaResponse;
import co.edu.uniquindio.sga.application.usecase.GestionarTemporadasUseCase;
import co.edu.uniquindio.sga.domain.entity.Temporada;
import co.edu.uniquindio.sga.domain.valueobject.IdTemporada;
import co.edu.uniquindio.sga.infrastructure.rest.mapper.CatalogoRestMapper;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.time.Clock;
import java.time.LocalDate;

/** 7.4 Temporadas: consulta pública; registro y cambios del administrador. */
@RestController
@RequestMapping("/api/v1/temporadas")
public class TemporadaController {

    private final GestionarTemporadasUseCase temporadasUseCase;
    private final CatalogoRestMapper mapper;
    private final Clock reloj;

    public TemporadaController(GestionarTemporadasUseCase temporadasUseCase, CatalogoRestMapper mapper, Clock reloj) {
        this.temporadasUseCase = temporadasUseCase;
        this.mapper = mapper;
        this.reloj = reloj;
    }

    @GetMapping
    public ResponseEntity<Pagina<TemporadaResponse>> listar(@RequestParam(defaultValue = "0") int pagina) {
        return ResponseEntity.ok(Pagina.de(temporadasUseCase.listar(), pagina).mapear(mapper::aTemporada));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TemporadaResponse> obtener(@PathVariable String id) {
        return ResponseEntity.ok(mapper.aTemporada(temporadasUseCase.consultar(new IdTemporada(id))));
    }

    /** 7.4: temporada nueva, con la tarifa de cada domo activo (todo domo activo tiene tarifa en todas). */
    @PostMapping
    public ResponseEntity<TemporadaResponse> registrar(@Valid @RequestBody RegistrarTemporadaRequest request) {
        Temporada temporada = temporadasUseCase.registrar(mapper.aTemporadaNueva(request),
                mapper.aTarifasPorDomo(request.tarifas()), LocalDate.now(reloj));
        return ResponseEntity.created(URI.create("/api/v1/temporadas/" + temporada.getId().valor()))
                .body(mapper.aTemporada(temporada));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TemporadaResponse> redefinir(@PathVariable String id,
                                                       @Valid @RequestBody RedefinirTemporadaRequest request) {
        return ResponseEntity.ok(mapper.aTemporada(temporadasUseCase.redefinir(new IdTemporada(id), request.nombre(),
                mapper.aPeriodos(request.periodos()))));
    }
}
