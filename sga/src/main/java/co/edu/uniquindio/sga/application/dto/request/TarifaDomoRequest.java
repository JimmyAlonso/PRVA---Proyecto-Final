package co.edu.uniquindio.sga.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/** Tarifa de un domo para la temporada que se está registrando. */
public record TarifaDomoRequest(
        @NotBlank(message = "La identificación del domo es obligatoria")
        String idDomo,
        @NotNull(message = "El valor de la tarifa es obligatorio")
        @Positive(message = "El valor de la tarifa debe ser mayor que cero")
        Long valorPorOcupanteNoche
) {
}
