package co.edu.uniquindio.sga.domain.valueobject;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;

import java.math.BigDecimal;

public record Porcentaje(BigDecimal valor) {

    public Porcentaje {
        if (valor == null) {
            throw new ReglaDominioException("El porcentaje es obligatorio.");
        }
        if (valor.compareTo(BigDecimal.ZERO) < 0 || valor.compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new ReglaDominioException("El porcentaje debe estar entre 0 y 100.");
        }
    }

    public static Porcentaje de(long valor) {
        return new Porcentaje(BigDecimal.valueOf(valor));
    }

    /** Aplica el porcentaje sin redondear: quien calcula el cargo decide cuándo redondear (3.4). */
    public Dinero aplicarA(Dinero monto) {
        BigDecimal resultado = monto.valor().multiply(valor).divide(BigDecimal.valueOf(100));
        return new Dinero(resultado);
    }

    public boolean esCero() {
        return valor.signum() == 0;
    }
}
