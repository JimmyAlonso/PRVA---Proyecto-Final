package co.edu.uniquindio.sga.application.dto.request;

import java.time.LocalTime;

// POST /api/v1/reservas/{codigo}/confirmar. Opcional si la hora ya se registró al crear.
public record ConfirmarReservaRequest(LocalTime horaEstimadaLlegada) {
}
