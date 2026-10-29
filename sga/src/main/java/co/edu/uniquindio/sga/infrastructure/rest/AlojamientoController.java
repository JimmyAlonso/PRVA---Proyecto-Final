package co.edu.uniquindio.sga.infrastructure.rest;

import co.edu.uniquindio.sga.application.dto.request.ActualizarAlojamientoRequest;
import co.edu.uniquindio.sga.application.dto.request.ServicioAdicionalRequest;
import co.edu.uniquindio.sga.application.dto.response.OperacionResponses.AlojamientoResponse;
import co.edu.uniquindio.sga.application.usecase.GestionarAlojamientoUseCase;
import co.edu.uniquindio.sga.infrastructure.rest.mapper.CatalogoRestMapper;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 7.3: datos del alojamiento, horarios, normas, servicios y parámetros configurados. */
@RestController
@RequestMapping("/api/v1/alojamiento")
public class AlojamientoController {

    private final GestionarAlojamientoUseCase alojamientoUseCase;
    private final CatalogoRestMapper mapper;

    public AlojamientoController(GestionarAlojamientoUseCase alojamientoUseCase, CatalogoRestMapper mapper) {
        this.alojamientoUseCase = alojamientoUseCase;
        this.mapper = mapper;
    }

    @GetMapping
    public ResponseEntity<AlojamientoResponse> consultar() {
        return ResponseEntity.ok(mapper.aAlojamiento(alojamientoUseCase.consultar()));
    }

    @PutMapping
    public ResponseEntity<AlojamientoResponse> actualizar(@Valid @RequestBody ActualizarAlojamientoRequest request) {
        return ResponseEntity.ok(mapper.aAlojamiento(alojamientoUseCase.actualizarDatos(request.descripcion(),
                request.direccion(), request.latitud(), request.longitud(), request.normasConvivencia())));
    }

    /** L-17: define o reemplaza un servicio adicional y su cargo. */
    @PutMapping("/servicios/{codigo}")
    public ResponseEntity<AlojamientoResponse> definirServicio(@PathVariable String codigo,
                                                               @Valid @RequestBody ServicioAdicionalRequest request) {
        return ResponseEntity.ok(mapper.aAlojamiento(alojamientoUseCase.definirServicio(
                mapper.aServicioAdicional(codigo, request))));
    }
}
