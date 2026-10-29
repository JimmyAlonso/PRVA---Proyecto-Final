package co.edu.uniquindio.sga.application.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * 7.3: identificación única y estable (L-04), nombre, descripción, dormitorios, capacidad y
 * dotación. Las tarifas son opcionales: el domo queda activo solo si tiene tarifa en todas las temporadas.
 */
public record RegistrarDomoRequest(
        @NotBlank(message = "La identificación del domo es obligatoria")
        @Pattern(regexp = "^[A-Za-z0-9-]{3,20}$", message = "La identificación admite letras, números y guiones (3 a 20)")
        String identificacion,
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 120, message = "El nombre admite máximo 120 caracteres")
        String nombre,
        @Size(max = 1000, message = "La descripción admite máximo 1000 caracteres")
        String descripcion,
        @NotNull(message = "El número de dormitorios es obligatorio")
        @Min(value = 1, message = "El domo debe tener al menos un dormitorio")
        Integer dormitorios,
        @NotNull(message = "La capacidad es obligatoria")
        @Min(value = 1, message = "La capacidad debe ser al menos 1")
        Integer capacidad,
        List<@NotBlank String> caracteristicas,
        @Valid JacuzziRequest jacuzzi,
        @Min(value = 1, message = "El peso máximo de la malla debe ser mayor que cero")
        Integer mallaPesoMaximoKg,
        @Min(value = 1, message = "La fogata debe tener al menos un kit de leña")
        Integer fogataKitsLena,
        List<@Valid TarifaRequest> tarifas
) {
}
