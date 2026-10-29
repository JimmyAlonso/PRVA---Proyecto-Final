package co.edu.uniquindio.sga.application.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/** L-10: a partir de cuántas horas de antelación aplica el tramo y qué porcentaje se retiene. */
public record TramoRetencionRequest(
        @NotNull(message = "La antelación mínima del tramo es obligatoria")
        @Min(value = 0, message = "La antelación mínima no puede ser negativa")
        Integer horasAntelacionMinima,
        @NotNull(message = "El porcentaje de retención es obligatorio")
        @DecimalMin(value = "0", message = "El porcentaje debe estar entre 0 y 100")
        @DecimalMax(value = "100", message = "El porcentaje debe estar entre 0 y 100")
        BigDecimal porcentajeRetencion
) {
}
