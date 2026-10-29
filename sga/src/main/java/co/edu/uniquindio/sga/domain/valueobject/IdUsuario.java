package co.edu.uniquindio.sga.domain.valueobject;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;

public record IdUsuario(String valor) {

    public IdUsuario {
        if (valor == null || valor.isBlank()) {
            throw new ReglaDominioException("El identificador del usuario es obligatorio.");
        }
        valor = valor.trim();
    }
}
