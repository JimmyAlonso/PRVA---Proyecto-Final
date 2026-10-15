package co.edu.uniquindio.sga.application.dto.response;

import co.edu.uniquindio.sga.domain.valueobject.CanalOrigen;
import co.edu.uniquindio.sga.domain.valueobject.EstadoReserva;

import java.time.LocalDate;

// Pensado para GET /api/v1/reservas (listado paginado)
public record ReservaResumenResponse(
        String codigo,
        EstadoReserva estado,
        CanalOrigen canalOrigen,
        String idDomo,
        LocalDate fechaEntrada,
        LocalDate fechaSalida,
        String nombreTitular,
        int totalOcupantes,
        long valorTotal
) {
}
