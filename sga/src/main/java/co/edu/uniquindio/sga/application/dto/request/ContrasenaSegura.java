package co.edu.uniquindio.sga.application.dto.request;

/** 7.2: forma de una contraseña segura, compartida por los DTOs que reciben contraseñas nuevas. */
public final class ContrasenaSegura {

    public static final String PATRON = "^(?=.*[A-Z])(?=.*\\d).{8,72}$";
    public static final String MENSAJE =
            "La contraseña debe tener mínimo 8 caracteres e incluir al menos una mayúscula y un número";

    private ContrasenaSegura() {
    }
}
