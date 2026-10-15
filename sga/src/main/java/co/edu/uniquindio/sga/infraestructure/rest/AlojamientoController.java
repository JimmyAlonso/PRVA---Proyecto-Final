package co.edu.uniquindio.sga.infraestructure.rest;

import co.edu.uniquindio.sga.application.dto.response.OperacionResponses.AlojamientoResponse;
import co.edu.uniquindio.sga.domain.entity.Alojamiento;
import co.edu.uniquindio.sga.infraestructure.rest.mapper.OperacionMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 7.3: datos del alojamiento, horarios, normas, servicios y parámetros configurados. */
@RestController
@RequestMapping("/api/v1/alojamiento")
public class AlojamientoController {

    private final Alojamiento alojamiento;
    private final OperacionMapper mapper;

    public AlojamientoController(Alojamiento alojamiento, OperacionMapper mapper) {
        this.alojamiento = alojamiento;
        this.mapper = mapper;
    }

    @GetMapping
    public ResponseEntity<AlojamientoResponse> consultar() {
        return ResponseEntity.ok(mapper.aAlojamiento(alojamiento));
    }
}
