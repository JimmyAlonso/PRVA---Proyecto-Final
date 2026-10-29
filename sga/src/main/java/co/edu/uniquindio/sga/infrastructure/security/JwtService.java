package co.edu.uniquindio.sga.infrastructure.security;

import co.edu.uniquindio.sga.domain.entity.Usuario;
import co.edu.uniquindio.sga.domain.valueobject.Rol;
import co.edu.uniquindio.sga.infrastructure.config.SgaProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import java.util.logging.Logger;
import javax.crypto.SecretKey;

/**
 * 7.2: emite y valida los JWT (HS256) con expiración. La clave sale de la variable de entorno
 * SGA_JWT_SECRETO (mínimo 32 caracteres) y nunca se versiona. Sin la variable se genera una
 * clave aleatoria al arrancar: sirve para desarrollo, pero los tokens dejan de valer al reiniciar.
 */
@Component
public class JwtService {

    private static final Logger LOG = Logger.getLogger(JwtService.class.getName());
    private static final int LONGITUD_MINIMA_SECRETO = 32;

    public record TokenEmitido(String token, Instant expiraEn) {
    }

    private final SecretKey clave;
    private final Duration vigencia;
    private final Clock reloj;

    public JwtService(SgaProperties propiedades, Clock reloj) {
        SgaProperties.Jwt jwt = propiedades.seguridad() == null ? null : propiedades.seguridad().jwt();
        this.clave = Keys.hmacShaKeyFor(secreto(jwt == null ? null : jwt.secreto()));
        this.vigencia = Duration.ofMinutes(jwt == null || jwt.expiracionMinutos() <= 0 ? 120 : jwt.expiracionMinutos());
        this.reloj = reloj;
    }

    private static byte[] secreto(String configurado) {
        if (configurado == null || configurado.isBlank()) {
            LOG.warning("SGA_JWT_SECRETO no está definida: se usa una clave aleatoria (solo para desarrollo).");
            byte[] aleatorio = new byte[64];
            new SecureRandom().nextBytes(aleatorio);
            return aleatorio;
        }
        byte[] bytes = configurado.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < LONGITUD_MINIMA_SECRETO) {
            throw new IllegalStateException("SGA_JWT_SECRETO debe tener al menos " + LONGITUD_MINIMA_SECRETO
                    + " caracteres para firmar con HS256.");
        }
        return bytes;
    }

    public TokenEmitido emitir(Usuario usuario) {
        Instant ahora = reloj.instant();
        Instant expira = ahora.plus(vigencia);
        String token = Jwts.builder()
                .subject(usuario.getId().valor())
                .claim("correo", usuario.getCorreo().valor())
                .claim("nombre", usuario.getNombre())
                .claim("documento", usuario.getDocumento().valor())
                .claim("rol", usuario.getRol().name())
                .issuedAt(Date.from(ahora))
                .expiration(Date.from(expira))
                .signWith(clave)
                .compact();
        return new TokenEmitido(token, expira);
    }

    /** Devuelve la identidad si el token es auténtico y no ha expirado. */
    public Optional<UsuarioAutenticado> validar(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(clave)
                    .clock(() -> Date.from(reloj.instant()))
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return Optional.of(new UsuarioAutenticado(
                    claims.getSubject(),
                    claims.get("correo", String.class),
                    claims.get("nombre", String.class),
                    claims.get("documento", String.class),
                    Rol.valueOf(claims.get("rol", String.class))));
        } catch (JwtException | IllegalArgumentException | NullPointerException invalido) {
            return Optional.empty();
        }
    }
}
