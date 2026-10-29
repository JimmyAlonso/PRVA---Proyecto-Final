package co.edu.uniquindio.sga.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/** L-17: servicio adicional y su cargo. El código va en la ruta. */
public record ServicioAdicionalRequest(
        @NotBlank(message = "El nombre del servicio es obligatorio")
        @Size(max = 150, message = "El nombre admite máximo 150 caracteres")
        String nombre,
        @NotNull(message = "El valor es obligatorio")
        @Positive(message = "El valor debe ser mayor que cero")
        Long valor,
        boolean porPersona
) {
}
