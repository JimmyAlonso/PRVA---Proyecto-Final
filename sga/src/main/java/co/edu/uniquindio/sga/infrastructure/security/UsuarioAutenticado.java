package co.edu.uniquindio.sga.infrastructure.security;

import co.edu.uniquindio.sga.domain.valueobject.Rol;

/**
 * Identidad del usuario tomada del JWT. Es infraestructura (3.6): el dominio no la conoce.
 * El documento sirve para verificar la propiedad de las reservas del huésped (7.2).
 */
public record UsuarioAutenticado(String id, String correo, String nombre, String documento, Rol rol) {

    public boolean esPersonalDeRecepcion() {
        return rol == Rol.RECEPCIONISTA || rol == Rol.ADMINISTRADOR;
    }

    public boolean esAdministrador() {
        return rol == Rol.ADMINISTRADOR;
    }

    public boolean esHuesped() {
        return rol == Rol.HUESPED;
    }
}
