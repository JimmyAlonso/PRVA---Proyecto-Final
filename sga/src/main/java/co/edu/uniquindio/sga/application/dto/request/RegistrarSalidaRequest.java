package co.edu.uniquindio.sga.application.dto.request;

import jakarta.validation.constraints.Size;

/**
 * POST /api/v1/reservas/{codigo}/check-out. Si el saldo del folio no es cero, la salida exige
 * autorización explícita de un administrador (RN-17): el autor es el administrador autenticado
 * y aquí solo llega el motivo.
 */
public record RegistrarSalidaRequest(
        @Size(min = 5, max = 300, message = "El motivo de la autorización debe tener entre 5 y 300 caracteres")
        String motivoAutorizacion
) {
}
