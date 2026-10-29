package co.edu.uniquindio.sga.domain.valueobject;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;

import java.util.Locale;

/** 7.2: correo electrónico con formato válido. Se guarda en minúsculas para que sea único sin importar mayúsculas. */
public record Correo(String valor) {

    public Correo {
        if (valor == null || valor.isBlank()) {
            throw new ReglaDominioException("El correo electrónico es obligatorio.");
        }
        valor = valor.trim().toLowerCase(Locale.ROOT);
        if (!valor.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            throw new ReglaDominioException("El correo electrónico no tiene un formato válido.");
        }
    }
}
