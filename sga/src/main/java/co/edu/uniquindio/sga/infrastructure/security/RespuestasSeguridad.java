package co.edu.uniquindio.sga.infrastructure.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.LocalDateTime;

/**
 * 401 y 403 producidos por el filtro de seguridad, con el mismo formato ErrorResponse que el
 * resto de la API (codigo, mensaje, detalles, ruta, momento) y sin trazas técnicas.
 */
@Component
public class RespuestasSeguridad implements AuthenticationEntryPoint, AccessDeniedHandler {

    private final Clock reloj;

    public RespuestasSeguridad(Clock reloj) {
        this.reloj = reloj;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        escribir(response, HttpServletResponse.SC_UNAUTHORIZED, "NO_AUTENTICADO",
                "Debe iniciar sesión y enviar un token válido en el encabezado Authorization.", request.getRequestURI());
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {
        escribir(response, HttpServletResponse.SC_FORBIDDEN, "ACCESO_DENEGADO",
                "Su rol no tiene permiso para esta operación.", request.getRequestURI());
    }

    private void escribir(HttpServletResponse response, int estado, String codigo, String mensaje, String ruta)
            throws IOException {
        response.setStatus(estado);
        response.setContentType("application/json");
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write("{\"codigo\":\"" + codigo + "\",\"mensaje\":\"" + escapar(mensaje)
                + "\",\"detalles\":[],\"ruta\":\"" + escapar(ruta) + "\",\"momento\":\""
                + LocalDateTime.now(reloj) + "\"}");
    }

    private static String escapar(String texto) {
        return texto == null ? "" : texto.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
