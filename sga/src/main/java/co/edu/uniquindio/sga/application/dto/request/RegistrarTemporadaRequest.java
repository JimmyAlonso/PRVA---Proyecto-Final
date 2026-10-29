package co.edu.uniquindio.sga.application.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * 7.4: una temporada nueva con sus rangos de fechas. Como todo domo activo debe tener tarifa en
 * todas las temporadas, llega con la tarifa de cada domo activo.
 */
public record RegistrarTemporadaRequest(
        @NotBlank(message = "El identificador de la temporada es obligatorio")
        @Pattern(regexp = "^[A-Z0-9_]{2,40}$", message = "El identificador admite mayúsculas, números y guion bajo")
        String id,
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 80, message = "El nombre admite máximo 80 caracteres")
        String nombre,
        @NotEmpty(message = "Indique al menos un rango de fechas")
        List<@Valid PeriodoRequest> periodos,
        List<@Valid TarifaDomoRequest> tarifas
) {
}
