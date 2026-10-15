package co.edu.uniquindio.sga.application.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * El titular también es ocupante (3.2): si no viene repetido en la lista de ocupantes, el
 * mapper lo agrega. El correo y el teléfono son para las notificaciones (7.5, 7.8).
 */
public record TitularRequest(
        @NotBlank(message = "El documento del titular es obligatorio")
        @Size(min = 5, max = 15, message = "El documento debe tener entre 5 y 15 caracteres")
        String documento,

        @NotBlank(message = "El nombre del titular es obligatorio")
        String nombre,

        @NotNull(message = "La fecha de nacimiento del titular es obligatoria")
        @Past(message = "La fecha de nacimiento debe ser una fecha pasada")
        LocalDate fechaNacimiento,

        @NotBlank(message = "El correo del titular es obligatorio")
        @Email(message = "El correo del titular no es válido")
        String correo,

        @NotBlank(message = "El teléfono del titular es obligatorio")
        String telefono
) {
}
