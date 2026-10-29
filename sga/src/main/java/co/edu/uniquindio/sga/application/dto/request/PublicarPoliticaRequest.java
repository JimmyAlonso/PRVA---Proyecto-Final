package co.edu.uniquindio.sga.application.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** F-07 y 7.4: nueva versión de la política de cancelación; la anterior se conserva. */
public record PublicarPoliticaRequest(
        @NotNull(message = "La fecha desde la que rige es obligatoria")
        LocalDate vigenteDesde,
        @NotNull(message = "Los tramos son obligatorios")
        @Size(min = 2, message = "La política debe tener al menos dos tramos de antelación")
        List<@Valid TramoRetencionRequest> tramos,
        @NotNull(message = "La retención por no-show es obligatoria")
        @DecimalMin(value = "0", message = "El porcentaje debe estar entre 0 y 100")
        @DecimalMax(value = "100", message = "El porcentaje debe estar entre 0 y 100")
        BigDecimal retencionNoShow
) {
}
