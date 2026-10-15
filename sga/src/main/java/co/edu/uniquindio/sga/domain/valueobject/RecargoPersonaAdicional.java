package co.edu.uniquindio.sga.domain.valueobject;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;

/**
 * L-20 (Ficha A.6): cuando la ocupación supera la base ({@code ocupantesIncluidos}, 2 personas)
 * sin superar la capacidad máxima, se cobra un recargo del {@code porcentaje} (25 %) sobre la
 * tarifa de la noche por cada ocupante adicional.
 *
 * <p>Decisión tomada literalmente de la ficha: se cuentan todos los ocupantes
 * ({@code ocupantes.size()}), facturables o no.
 */
public record RecargoPersonaAdicional(int ocupantesIncluidos, Porcentaje porcentaje) {

    public RecargoPersonaAdicional {
        if (ocupantesIncluidos < 1) {
            throw new ReglaDominioException("La ocupación base del recargo debe ser al menos 1.");
        }
        if (porcentaje == null) {
            throw new ReglaDominioException("El recargo debe indicar su porcentaje.");
        }
    }

    public int ocupantesAdicionales(int totalOcupantes) {
        return Math.max(0, totalOcupantes - ocupantesIncluidos);
    }

    public Dinero porNoche(Dinero tarifaNoche, int totalOcupantes) {
        int adicionales = ocupantesAdicionales(totalOcupantes);
        if (adicionales == 0) {
            return Dinero.CERO;
        }
        return porcentaje.aplicarA(tarifaNoche).multiplicarPor(adicionales);
    }
}
