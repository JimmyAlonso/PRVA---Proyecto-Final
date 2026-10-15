package co.edu.uniquindio.sga.application.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

// L-17: KIT_FOGATA, DECORACION_ROMANTICA o CENA_DOMO (catálogo en application.properties)
public record RegistrarServicioRequest(
        @NotBlank(message = "El código del servicio es obligatorio")
        String codigoServicio,

        @Min(value = 1, message = "La cantidad debe ser al menos 1")
        int cantidad
) {
}
