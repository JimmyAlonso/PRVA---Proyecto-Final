package co.edu.uniquindio.sga.domain.valueobject;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;

/**
 * L-17: servicios adicionales y su cargo (Ficha A.5): kit de fogata adicional, decoración
 * romántica y cena en el domo. Si {@code porPersona} es verdadero el valor se multiplica.
 */
public record ServicioAdicional(String codigo, String nombre, Dinero valor, boolean porPersona) {

    public ServicioAdicional {
        if (codigo == null || codigo.isBlank()) {
            throw new ReglaDominioException("El servicio adicional debe tener un código.");
        }
        if (nombre == null || nombre.isBlank()) {
            throw new ReglaDominioException("El servicio adicional debe tener un nombre.");
        }
        if (valor == null || valor.esCero()) {
            throw new ReglaDominioException("El servicio adicional debe tener un valor mayor que cero.");
        }
        codigo = codigo.trim().toUpperCase();
    }

    public Dinero cargoPara(int cantidad) {
        if (cantidad < 1) {
            throw new ReglaDominioException("La cantidad del servicio debe ser al menos 1.");
        }
        return valor.multiplicarPor(cantidad);
    }
}
