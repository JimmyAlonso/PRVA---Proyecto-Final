package co.edu.uniquindio.sga.infraestructure.rest;

import co.edu.uniquindio.sga.application.dto.response.ErrorRespuesta;
import co.edu.uniquindio.sga.application.exception.RecursoNoEncontradoException;
import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.infraestructure.security.AutenticadorCanales.CredencialCanalInvalidaException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 12.4: códigos HTTP adecuados y errores con significado de negocio, nunca trazas técnicas.
 * Formato ErrorRespuesta definido en docs/api/openapi.yaml.
 */
@RestControllerAdvice
public class ManejadorErrores {

    private static final Logger LOG = Logger.getLogger(ManejadorErrores.class.getName());

    @ExceptionHandler(ReglaDominioException.class)
    public ResponseEntity<ErrorRespuesta> reglaDeDominio(ReglaDominioException ex) {
        // 422: la solicitud está bien formada pero viola una regla del negocio
        return ResponseEntity.status(422)
                .body(ErrorRespuesta.de("REGLA_DOMINIO_VIOLADA", ex.getMessage()));
    }

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<ErrorRespuesta> noEncontrado(RecursoNoEncontradoException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorRespuesta.de("NO_ENCONTRADO", ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorRespuesta> datosInvalidos(MethodArgumentNotValidException ex) {
        List<String> detalles = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .toList();
        return ResponseEntity.badRequest()
                .body(new ErrorRespuesta("DATOS_INVALIDOS", "La solicitud tiene datos inválidos.", detalles));
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class,
            MissingServletRequestParameterException.class, MissingRequestHeaderException.class})
    public ResponseEntity<ErrorRespuesta> solicitudMalFormada(Exception ex) {
        return ResponseEntity.badRequest().body(ErrorRespuesta.de("SOLICITUD_MAL_FORMADA",
                "Revise el formato de la solicitud (fechas AAAA-MM-DD, horas HH:mm, valores de los enumerados y "
                        + "parámetros obligatorios)."));
    }

    @ExceptionHandler(CredencialCanalInvalidaException.class)
    public ResponseEntity<ErrorRespuesta> credencialCanal(CredencialCanalInvalidaException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorRespuesta.de("CANAL_NO_AUTORIZADO", ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorRespuesta> inesperado(Exception ex) {
        LOG.log(Level.SEVERE, "Error no controlado", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorRespuesta.de("ERROR_INTERNO", "Ocurrió un error inesperado. Intente de nuevo."));
    }
}
