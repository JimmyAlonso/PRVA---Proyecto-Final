package co.edu.uniquindio.sga.application.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

/** 7.4: cambia el nombre o los rangos de una temporada (la base no lleva rangos). */
public record RedefinirTemporadaRequest(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 80, message = "El nombre admite máximo 80 caracteres")
        String nombre,
        List<@Valid PeriodoRequest> periodos
) {
}
