package co.edu.uniquindio.sga.domain.repository;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;

/** 12.4: página solicitada (base 0) y su tamaño. Es un concepto de consulta, sin dependencia de Spring. */
public record Paginacion(int pagina, int tamano) {

    public Paginacion {
        if (pagina < 0) {
            throw new ReglaDominioException("El número de página no puede ser negativo.");
        }
        if (tamano < 1) {
            throw new ReglaDominioException("El tamaño de página debe ser al menos 1.");
        }
    }

    public int desplazamiento() {
        return pagina * tamano;
    }
}
