package co.edu.uniquindio.sga.domain.valueobject;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * RN-15: Saldo = Suma(cargos) - Suma(pagos). A diferencia de {@link Dinero}, puede ser
 * positivo (debe el huésped), cero o negativo (saldo a favor del huésped), sección 7.7.
 */
public record Saldo(BigDecimal valor) {

    public Saldo {
        if (valor == null) {
            throw new ReglaDominioException("El saldo es obligatorio.");
        }
        valor = valor.setScale(0, RoundingMode.HALF_UP);
    }

    public static Saldo de(Dinero cargos, Dinero pagos) {
        return new Saldo(cargos.valor().subtract(pagos.valor()));
    }

    public boolean esCero() {
        return valor.signum() == 0;
    }

    public boolean debeHuesped() {
        return valor.signum() > 0;
    }

    public boolean esSaldoAFavor() {
        return valor.signum() < 0;
    }

    public long enPesos() {
        return valor.longValueExact();
    }
}
