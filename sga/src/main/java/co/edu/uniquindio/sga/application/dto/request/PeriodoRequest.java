package co.edu.uniquindio.sga.application.dto.request;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

/** Rango de fechas de una temporada, ambos extremos incluidos. */
public record PeriodoRequest(
        @NotNull(message = "La fecha inicial del rango es obligatoria") LocalDate desde,
        @NotNull(message = "La fecha final del rango es obligatoria") LocalDate hasta
) {
}
