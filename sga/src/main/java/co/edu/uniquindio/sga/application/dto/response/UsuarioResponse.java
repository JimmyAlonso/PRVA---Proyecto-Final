package co.edu.uniquindio.sga.application.dto.response;

import co.edu.uniquindio.sga.domain.valueobject.Rol;

import java.time.LocalDate;

/** Datos públicos de un usuario. La contraseña cifrada nunca sale de la API. */
public record UsuarioResponse(
        String id,
        String nombre,
        String correo,
        String telefono,
        String documento,
        LocalDate fechaNacimiento,
        Rol rol,
        String fotoPerfil
) {
}
