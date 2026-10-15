package co.edu.uniquindio.sga.domain.valueobject;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;

// RN-17 y 7.7: el cierre con saldo distinto de cero queda registrado con autor y motivo.
public record AutorizacionCierre(String autor, String motivo) {

    public AutorizacionCierre {
        if (autor == null || autor.isBlank()) {
            throw new ReglaDominioException("La autorización de cierre debe indicar su autor.");
        }
        if (motivo == null || motivo.isBlank()) {
            throw new ReglaDominioException("La autorización de cierre debe indicar su motivo.");
        }
        autor = autor.trim();
        motivo = motivo.trim();
    }
}
