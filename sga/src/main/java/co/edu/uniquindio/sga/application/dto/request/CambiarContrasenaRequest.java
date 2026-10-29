package co.edu.uniquindio.sga.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record CambiarContrasenaRequest(
        @NotBlank(message = "La contraseña actual es obligatoria")
        String contrasenaActual,
        @NotBlank(message = "La nueva contraseña es obligatoria")
        @Pattern(regexp = ContrasenaSegura.PATRON, message = ContrasenaSegura.MENSAJE)
        String contrasenaNueva
) {
}
