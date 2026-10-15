package co.edu.uniquindio.sga.domain.valueobject;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Sección 3.4: moneda única COP, tipo de precisión exacta (prohibido float/double).
 * El redondeo al peso se aplica al final del cálculo de cada cargo, nunca noche por noche:
 * por eso el constructor conserva dos decimales y el redondeo es una operación explícita.
 *
 * <p>Un Dinero nunca es negativo. Lo que sí puede ser negativo es el {@link Saldo} del folio.
 */
public record Dinero(BigDecimal valor) {

    public static final Dinero CERO = new Dinero(BigDecimal.ZERO);

    public Dinero {
        if (valor == null) {
            throw new ReglaDominioException("El valor monetario es obligatorio.");
        }
        if (valor.signum() < 0) {
            throw new ReglaDominioException("El valor monetario no puede ser negativo.");
        }
        valor = valor.setScale(2, RoundingMode.HALF_UP);
    }

    public static Dinero de(long pesos) {
        return new Dinero(BigDecimal.valueOf(pesos));
    }

    public Dinero sumar(Dinero otro) {
        return new Dinero(valor.add(otro.valor));
    }

    public Dinero restar(Dinero otro) {
        return new Dinero(valor.subtract(otro.valor));
    }

    public Dinero multiplicarPor(int factor) {
        return new Dinero(valor.multiply(BigDecimal.valueOf(factor)));
    }

    /** 3.4: redondeo al peso más cercano, aplicado una sola vez al final de cada cargo. */
    public Dinero redondearAlPeso() {
        return new Dinero(valor.setScale(0, RoundingMode.HALF_UP));
    }

    public boolean esCero() {
        return valor.signum() == 0;
    }

    public boolean esMayorQue(Dinero otro) {
        return valor.compareTo(otro.valor) > 0;
    }

    public boolean esMenorQue(Dinero otro) {
        return valor.compareTo(otro.valor) < 0;
    }

    /** Pesos enteros, para exponer en la API (3.4: sin decimales). */
    public long enPesos() {
        return valor.setScale(0, RoundingMode.HALF_UP).longValueExact();
    }
}
