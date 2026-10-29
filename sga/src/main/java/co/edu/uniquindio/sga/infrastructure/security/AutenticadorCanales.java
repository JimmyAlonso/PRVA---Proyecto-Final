package co.edu.uniquindio.sga.infrastructure.security;

import co.edu.uniquindio.sga.infrastructure.config.SgaProperties;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Map;

/**
 * 7.8 y 10.3.1: cada canal externo tiene su credencial. La autenticación es infraestructura
 * (3.6), por eso no está en el dominio. Las credenciales llegan por configuración externa y
 * no se versionan (I-06): ver sga.canales.credenciales en application.properties.
 */
@Component
public class AutenticadorCanales {

    private final Map<String, String> credenciales;

    public AutenticadorCanales(SgaProperties propiedades) {
        this.credenciales = propiedades.canales() == null || propiedades.canales().credenciales() == null
                ? Map.of() : propiedades.canales().credenciales();
    }

    public void verificar(String canal, String credencial) {
        if (canal == null || credencial == null) {
            throw new CredencialCanalInvalidaException();
        }
        String esperada = credenciales.entrySet().stream()
                .filter(e -> e.getKey().equalsIgnoreCase(canal.trim()))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElseThrow(CredencialCanalInvalidaException::new);
        // Comparación en tiempo constante para no filtrar información por tiempos de respuesta
        if (!MessageDigest.isEqual(esperada.getBytes(StandardCharsets.UTF_8),
                credencial.getBytes(StandardCharsets.UTF_8))) {
            throw new CredencialCanalInvalidaException();
        }
    }

    public static class CredencialCanalInvalidaException extends RuntimeException {
        public CredencialCanalInvalidaException() {
            super("Canal no registrado o credencial inválida.");
        }
    }
}
