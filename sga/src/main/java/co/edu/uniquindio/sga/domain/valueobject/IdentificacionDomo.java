package co.edu.uniquindio.sga.domain.valueobject;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;

// L-04: la identificación de cada Domo es única y estable (DOM-01 ... DOM-05)
public record IdentificacionDomo(String valor) {

    public IdentificacionDomo {
        if (valor == null || valor.isBlank()) {
            throw new ReglaDominioException("La identificación del domo es obligatoria.");
        }
        valor = valor.trim().toUpperCase();
    }
}
