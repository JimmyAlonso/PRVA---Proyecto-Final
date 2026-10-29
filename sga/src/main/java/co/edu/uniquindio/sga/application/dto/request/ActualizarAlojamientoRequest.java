package co.edu.uniquindio.sga.application.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/** 7.3: datos del alojamiento que gestiona el administrador. Los parámetros de la sección 6 son configuración. */
public record ActualizarAlojamientoRequest(
        @Size(max = 1000, message = "La descripción admite máximo 1000 caracteres")
        String descripcion,
        @Size(max = 300, message = "La dirección admite máximo 300 caracteres")
        String direccion,
        @NotNull(message = "La latitud es obligatoria")
        @DecimalMin(value = "-90", message = "Latitud inválida")
        @DecimalMax(value = "90", message = "Latitud inválida")
        BigDecimal latitud,
        @NotNull(message = "La longitud es obligatoria")
        @DecimalMin(value = "-180", message = "Longitud inválida")
        @DecimalMax(value = "180", message = "Longitud inválida")
        BigDecimal longitud,
        @Size(max = 1000, message = "Las normas admiten máximo 1000 caracteres")
        String normasConvivencia
) {
}
