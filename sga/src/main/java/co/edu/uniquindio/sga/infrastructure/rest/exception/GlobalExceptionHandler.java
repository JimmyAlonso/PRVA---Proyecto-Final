package co.edu.uniquindio.sga.infrastructure.rest.exception;

import co.edu.uniquindio.sga.application.exception.CredencialesInvalidasException;
import co.edu.uniquindio.sga.application.exception.RecursoNoEncontradoException;
import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.infrastructure.security.AutenticadorCanales.CredencialCanalInvalidaException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Traduce cada excepción a su código HTTP con significado de negocio, en un único ErrorResponse:
 * <ul>
 *   <li>400: la forma del mensaje (Bean Validation, JSON mal formado, parámetros inválidos).</li>
 *   <li>401: credenciales incorrectas o canal sin credencial válida.</li>
 *   <li>403: el rol o el propietario no permiten la operación.</li>
 *   <li>404: el recurso no existe (excepciones de la capa de aplicación).</li>
 *   <li>409: una regla del negocio se viola (ReglaDominioException, lanzada por el dominio).</li>
 * </ul>
 * Nunca se envía una traza técnica al cliente: el error inesperado se registra en el log.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOG = Logger.getLogger(GlobalExceptionHandler.class.getName());

    private final Clock reloj;

    public GlobalExceptionHandler(Clock reloj) {
        this.reloj = reloj;
    }

    @ExceptionHandler(ReglaDominioException.class)
    public ResponseEntity<ErrorResponse> reglaDeDominio(ReglaDominioException ex, HttpServletRequest request) {
        return responder(HttpStatus.CONFLICT, "REGLA_DE_NEGOCIO", ex.getMessage(), request);
    }

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<ErrorResponse> noEncontrado(RecursoNoEncontradoException ex, HttpServletRequest request) {
        return responder(HttpStatus.NOT_FOUND, "NO_ENCONTRADO", ex.getMessage(), request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> datosInvalidos(MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<String> detalles = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .toList();
        return ResponseEntity.badRequest().body(new ErrorResponse("DATOS_INVALIDOS",
                "La solicitud tiene datos inválidos.", detalles, request.getRequestURI(), ahora()));
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ErrorResponse> parametrosInvalidos(HandlerMethodValidationException ex,
                                                             HttpServletRequest request) {
        return responder(HttpStatus.BAD_REQUEST, "DATOS_INVALIDOS", "Los parámetros de la solicitud no son válidos.",
                request);
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class,
            MissingServletRequestParameterException.class, MissingRequestHeaderException.class})
    public ResponseEntity<ErrorResponse> solicitudMalFormada(Exception ex, HttpServletRequest request) {
        return responder(HttpStatus.BAD_REQUEST, "SOLICITUD_MAL_FORMADA",
                "Revise el formato de la solicitud (fechas AAAA-MM-DD, horas HH:mm, valores de los enumerados y "
                        + "parámetros obligatorios).", request);
    }

    /** Solicitudes incompletas que solo se detectan al traducir el DTO (por ejemplo, un grupo sin integrantes). */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> solicitudIncompleta(IllegalArgumentException ex, HttpServletRequest request) {
        return responder(HttpStatus.BAD_REQUEST, "SOLICITUD_INCOMPLETA", ex.getMessage(), request);
    }

    @ExceptionHandler({CredencialesInvalidasException.class, CredencialCanalInvalidaException.class,
            AuthenticationException.class})
    public ResponseEntity<ErrorResponse> noAutenticado(RuntimeException ex, HttpServletRequest request) {
        return responder(HttpStatus.UNAUTHORIZED, "NO_AUTENTICADO", ex.getMessage(), request);
    }

    /** 403 de @PreAuthorize (verificación por propietario) y de las verificaciones de canal del controller. */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> accesoDenegado(AccessDeniedException ex, HttpServletRequest request) {
        return responder(HttpStatus.FORBIDDEN, "ACCESO_DENEGADO", "No tiene permiso sobre este recurso.", request);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> metodoNoPermitido(HttpRequestMethodNotSupportedException ex,
                                                           HttpServletRequest request) {
        return responder(HttpStatus.METHOD_NOT_ALLOWED, "METODO_NO_PERMITIDO",
                "El método " + ex.getMethod() + " no está disponible en esta ruta.", request);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> rutaInexistente(NoResourceFoundException ex, HttpServletRequest request) {
        return responder(HttpStatus.NOT_FOUND, "NO_ENCONTRADO", "La ruta solicitada no existe.", request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> inesperado(Exception ex, HttpServletRequest request) {
        LOG.log(Level.SEVERE, "Error no controlado en " + request.getRequestURI(), ex);
        return responder(HttpStatus.INTERNAL_SERVER_ERROR, "ERROR_INTERNO",
                "Ocurrió un error inesperado. Intente de nuevo.", request);
    }

    private ResponseEntity<ErrorResponse> responder(HttpStatus estado, String codigo, String mensaje,
                                                    HttpServletRequest request) {
        return ResponseEntity.status(estado).body(ErrorResponse.de(codigo, mensaje, request.getRequestURI(), ahora()));
    }

    private LocalDateTime ahora() {
        return LocalDateTime.now(reloj);
    }
}
