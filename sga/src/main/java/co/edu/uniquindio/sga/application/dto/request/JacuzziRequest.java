package co.edu.uniquindio.sga.application.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/** Glosario propio: el Jacuzzi del domo y sus horas de precalentamiento (L-21). */
public record JacuzziRequest(
        String descripcion,
        @NotNull(message = "Las horas de precalentamiento son obligatorias")
        @Min(value = 1, message = "Las horas de precalentamiento deben ser al menos 1")
        Integer horasPrecalentamiento
) {
}
