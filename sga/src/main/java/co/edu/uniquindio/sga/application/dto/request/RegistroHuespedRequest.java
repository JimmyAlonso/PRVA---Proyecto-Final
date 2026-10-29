package co.edu.uniquindio.sga.application.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * 7.2 Registro público. Campos obligatorios: nombre, correo (único), contraseña, teléfono,
 * documento y fecha de nacimiento. Contraseña segura: mínimo 8 caracteres, con mayúsculas y números.
 */
public record RegistroHuespedRequest(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 150, message = "El nombre admite máximo 150 caracteres")
        String nombre,
        @NotBlank(message = "El correo es obligatorio")
        @Email(message = "El correo no tiene un formato válido")
        String correo,
        @NotBlank(message = "La contraseña es obligatoria")
        @Pattern(regexp = ContrasenaSegura.PATRON, message = ContrasenaSegura.MENSAJE)
        String contrasena,
        @NotBlank(message = "El teléfono es obligatorio")
        @Size(max = 30, message = "El teléfono admite máximo 30 caracteres")
        String telefono,
        @NotBlank(message = "El documento de identidad es obligatorio")
        @Size(min = 5, max = 15, message = "El documento debe tener entre 5 y 15 caracteres")
        String documento,
        @NotNull(message = "La fecha de nacimiento es obligatoria")
        @Past(message = "La fecha de nacimiento debe ser una fecha pasada")
        LocalDate fechaNacimiento
) {
}
