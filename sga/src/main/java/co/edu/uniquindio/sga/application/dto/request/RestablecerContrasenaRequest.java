package co.edu.uniquindio.sga.application.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record RestablecerContrasenaRequest(
        @NotBlank(message = "El correo es obligatorio")
        @Email(message = "El correo no tiene un formato válido")
        String correo,
        @NotBlank(message = "El código de recuperación es obligatorio")
        @Pattern(regexp = "^\\d{6}$", message = "El código de recuperación tiene 6 dígitos")
        String codigo,
        @NotBlank(message = "La nueva contraseña es obligatoria")
        @Pattern(regexp = ContrasenaSegura.PATRON, message = ContrasenaSegura.MENSAJE)
        String contrasenaNueva
) {
}
