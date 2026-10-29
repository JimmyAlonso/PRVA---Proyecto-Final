package co.edu.uniquindio.sga.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/** RN-05: valor por ocupante facturable por noche, en pesos, para una temporada. */
public record TarifaRequest(
        @NotBlank(message = "La temporada de la tarifa es obligatoria")
        String temporada,
        @NotNull(message = "El valor de la tarifa es obligatorio")
        @Positive(message = "El valor de la tarifa debe ser mayor que cero")
        Long valorPorOcupanteNoche
) {
}
