package co.edu.uniquindio.sga.application.dto.request;

/**
 * POST /api/v1/reservas/{codigo}/check-out. La autorización (autor y motivo) solo se exige si
 * el saldo del folio no es cero (RN-17). Que el autor sea administrador lo verificará la
 * seguridad (Corte 2).
 */
public record RegistrarSalidaRequest(String autorizadoPor, String motivoAutorizacion) {
}
