package co.edu.uniquindio.sga.application.dto.response;

import java.time.LocalDate;

// 7.4: fecha, temporada, tarifa aplicada, ocupantes facturables y subtotal
public record DetalleNocheResponse(
        LocalDate fecha,
        String temporada,
        long tarifaPorOcupanteFacturable,
        int ocupantesFacturables,
        long recargoPersonaAdicional,
        long subtotal
) {
}
