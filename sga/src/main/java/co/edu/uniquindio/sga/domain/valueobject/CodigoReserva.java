package co.edu.uniquindio.sga.domain.valueobject;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;

// Formato del negocio: RES-AAAA-NNNNN (ejemplo: RES-2026-00042)
public record CodigoReserva(String valor) {

    public CodigoReserva {
        if (valor == null || valor.isBlank()) {
            throw new ReglaDominioException("El código de la reserva es obligatorio.");
        }
        valor = valor.trim().toUpperCase();
    }
}
