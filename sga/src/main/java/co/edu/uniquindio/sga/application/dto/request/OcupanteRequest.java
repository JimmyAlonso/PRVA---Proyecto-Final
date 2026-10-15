package co.edu.uniquindio.sga.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record OcupanteRequest(
        // Corrección: el rango 7-11 rechazaba tarjetas de identidad de 12 caracteres (TI + 10 dígitos)
        @NotBlank(message = "El documento del ocupante es obligatorio")
        @Size(min = 5, max = 15, message = "El documento debe tener entre 5 y 15 caracteres")
        String documento,

        @NotBlank(message = "El nombre del ocupante es obligatorio")
        String nombre,

        @NotNull(message = "La fecha de nacimiento es obligatoria")
        @Past(message = "La fecha de nacimiento debe ser una fecha pasada")
        LocalDate fechaNacimiento
) {
}
