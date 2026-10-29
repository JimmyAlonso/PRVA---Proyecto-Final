package co.edu.uniquindio.sga.domain.valueobject;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;

import java.time.LocalDateTime;

/**
 * 7.2 Recuperación de contraseña: código enviado al correo registrado, con vigencia y de un
 * solo uso. La vigencia (15 minutos) es configuración y llega al emitir el código.
 */
public record CodigoRecuperacion(String valor, LocalDateTime expiraEn, boolean usado) {

    public CodigoRecuperacion {
        if (valor == null || valor.isBlank()) {
            throw new ReglaDominioException("El código de recuperación es obligatorio.");
        }
        if (expiraEn == null) {
            throw new ReglaDominioException("El código de recuperación debe indicar su vencimiento.");
        }
        valor = valor.trim();
    }

    public static CodigoRecuperacion emitir(String valor, LocalDateTime momento, int minutosVigencia) {
        if (minutosVigencia <= 0) {
            throw new ReglaDominioException("La vigencia del código de recuperación debe ser mayor que cero.");
        }
        return new CodigoRecuperacion(valor, momento.plusMinutes(minutosVigencia), false);
    }

    public boolean esValido(String intento, LocalDateTime momento) {
        return !usado && intento != null && valor.equals(intento.trim()) && momento.isBefore(expiraEn);
    }

    public CodigoRecuperacion marcarUsado() {
        return new CodigoRecuperacion(valor, expiraEn, true);
    }
}
