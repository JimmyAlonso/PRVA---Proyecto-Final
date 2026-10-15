package co.edu.uniquindio.sga.application.dto.response;

import java.util.List;

// 12.4: la API devuelve errores con significado de negocio, no trazas técnicas
public record ErrorRespuesta(String codigoError, String mensaje, List<String> detalles) {

    public static ErrorRespuesta de(String codigo, String mensaje) {
        return new ErrorRespuesta(codigo, mensaje, List.of());
    }
}
