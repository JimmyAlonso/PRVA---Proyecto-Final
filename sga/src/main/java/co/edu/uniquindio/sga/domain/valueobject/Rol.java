package co.edu.uniquindio.sga.domain.valueobject;

/**
 * 7.1: roles del sistema. El registro público solo crea HUESPED; los roles internos los
 * asigna un administrador (7.2).
 */
public enum Rol {
    HUESPED,
    RECEPCIONISTA,
    ADMINISTRADOR,
    PERSONAL_SERVICIO;

    public boolean esInterno() {
        return this != HUESPED;
    }
}
