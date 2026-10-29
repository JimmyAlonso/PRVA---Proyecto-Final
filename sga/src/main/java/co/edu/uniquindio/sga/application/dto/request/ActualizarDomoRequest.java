package co.edu.uniquindio.sga.application.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

/** 7.3: actualización completa de los datos y la dotación del domo (la identificación no cambia). */
public record ActualizarDomoRequest(
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
        Integer fogataKitsLena
) {
}
