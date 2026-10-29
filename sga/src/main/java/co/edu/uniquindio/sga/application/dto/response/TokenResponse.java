package co.edu.uniquindio.sga.application.dto.response;

import java.time.Instant;

/** 7.2: JWT emitido al iniciar sesión, con su expiración. Se envía como "Authorization: Bearer {token}". */
public record TokenResponse(String token, String tipo, Instant expiraEn, UsuarioResponse usuario) {
}
