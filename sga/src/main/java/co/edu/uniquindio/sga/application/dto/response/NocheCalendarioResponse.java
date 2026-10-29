package co.edu.uniquindio.sga.application.dto.response;

import co.edu.uniquindio.sga.domain.valueobject.EstadoNoche;

import java.time.LocalDate;

/** 7.5 Calendario de ocupación: LIBRE, RESERVADA (con el código de la reserva) o BLOQUEADA (con el id del bloqueo). */
public record NocheCalendarioResponse(LocalDate fecha, EstadoNoche estado, String referencia) {
}
