package co.edu.uniquindio.sga.infrastructure.rest.exception;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 12.4: formato único de error de la API, con significado de negocio y sin trazas técnicas.
 * El mismo formato lo escriben RespuestasSeguridad (401/403 del filtro) y GlobalExceptionHandler.
 */
public record ErrorResponse(String codigo, String mensaje, List<String> detalles, String ruta, LocalDateTime momento) {

    public static ErrorResponse de(String codigo, String mensaje, String ruta, LocalDateTime momento) {
        return new ErrorResponse(codigo, mensaje, List.of(), ruta, momento);
    }
}
