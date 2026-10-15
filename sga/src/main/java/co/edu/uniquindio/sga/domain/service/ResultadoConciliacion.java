package co.edu.uniquindio.sga.domain.service;

import co.edu.uniquindio.sga.domain.entity.Reserva;
import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;

/**
 * Resultado de conciliar una reserva externa (RN-18, RN-19).
 *
 * <p>Corrección del error "Tipo": antes {@code ResultadoConciliacion} estaba declarado como
 * parámetro genérico de la clase del servicio, no como una clase real, así que
 * {@code ResultadoConciliacion.Tipo.DUPLICADA} no compilaba.
 */
public record ResultadoConciliacion(Tipo tipo, Reserva reserva, String motivo) {

    public enum Tipo {
        /** La reserva se creó en estado PENDIENTE. */
        CREADA,
        /** RN-19: el mismo mensaje ya se había recibido; se devuelve la reserva existente. */
        DUPLICADA,
        /** RN-18: colisiona con una reserva vigente o un bloqueo; se rechaza y se registra. */
        CONFLICTO
    }

    public ResultadoConciliacion {
        if (tipo == null) {
            throw new ReglaDominioException("El resultado de la conciliación debe indicar su tipo.");
        }
        if (tipo != Tipo.CONFLICTO && reserva == null) {
            throw new ReglaDominioException("Una conciliación aceptada debe traer la reserva.");
        }
    }

    public static ResultadoConciliacion creada(Reserva reserva) {
        return new ResultadoConciliacion(Tipo.CREADA, reserva, null);
    }

    public static ResultadoConciliacion duplicada(Reserva reserva) {
        return new ResultadoConciliacion(Tipo.DUPLICADA, reserva, "Mensaje repetido: la reserva ya existía.");
    }

    public static ResultadoConciliacion conflicto(String motivo) {
        return new ResultadoConciliacion(Tipo.CONFLICTO, null, motivo);
    }
}
