package co.edu.uniquindio.sga.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** 7.2: nombre, teléfono y foto de perfil. El correo no es editable, por eso no está aquí. */
public record ActualizarPerfilRequest(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 150, message = "El nombre admite máximo 150 caracteres")
        String nombre,
        @NotBlank(message = "El teléfono es obligatorio")
        @Size(max = 30, message = "El teléfono admite máximo 30 caracteres")
        String telefono,
        @Size(max = 500, message = "La URL de la foto admite máximo 500 caracteres")
        String fotoPerfil
) {
}
