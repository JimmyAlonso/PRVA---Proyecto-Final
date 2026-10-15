package co.edu.uniquindio.sga.application.dto.response;

import co.edu.uniquindio.sga.domain.valueobject.CanalOrigen;
import co.edu.uniquindio.sga.domain.valueobject.EstadoReserva;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public record ReservaDetalleResponse(
        String codigo,                      // "RES-2026-00001", no el objeto CodigoReserva
        EstadoReserva estado,
        CanalOrigen canalOrigen,
        String canalExterno,                // solo reservas del canal EXTERNO
        String idTransaccionExterna,
        String idDomo,
        LocalDate fechaEntrada,
        LocalDate fechaSalida,
        int noches,
        LocalTime horaEstimadaLlegada,      // null hasta que se registre (RN-09)
        TitularResponse titular,
        List<OcupanteResponse> ocupantes,
        int totalOcupantes,
        long valorTotal,                    // pesos, sin decimales (3.4)
        String moneda,                      // "COP"
        List<DetalleNocheResponse> desglose,// congelado al crear (3.5)
        int versionPoliticaCancelacion,     // la congelada en la reserva (RN-22)
        LocalDateTime momentoCreacion,
        String motivoCancelacion            // solo si la reserva fue cancelada
) {
}
