package co.edu.uniquindio.sga.application.dto.response;

import java.time.LocalDate;
import java.util.List;

// 7.7: folio con su detalle completo. saldo > 0 debe el huésped; < 0 saldo a favor.
public record FolioResponse(
        String codigoFolio,
        String codigoReserva,
        List<Movimiento> cargos,
        List<PagoResponse> pagos,
        long totalCargos,
        long totalPagos,
        long saldo,
        boolean depositoRegistrado,
        boolean cerrado,
        String autorizadoPor,
        String motivoAutorizacion
) {

    public record Movimiento(String concepto, long valor, boolean esInverso, LocalDate fecha, String descripcion) {
    }

    public record PagoResponse(String idPago, long monto, String medio, LocalDate fecha) {
    }
}
