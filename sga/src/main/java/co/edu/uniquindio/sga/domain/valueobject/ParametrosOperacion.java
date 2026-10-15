package co.edu.uniquindio.sga.domain.valueobject;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;

/**
 * Sección 6 y 7.3: parámetros configurables del alojamiento. Criterio de diseño derivado:
 * nada de esto se quema en el código; llega desde la configuración externa.
 */
public record ParametrosOperacion(UmbralEdadFacturable umbralEdad,
                                  TiempoPreparacion tiempoPreparacion,
                                  PlazoConfirmacion plazoConfirmacion,
                                  HoraLimiteNoShow horaLimiteNoShow,
                                  Porcentaje anticipo,
                                  Deposito deposito,
                                  RecargoPersonaAdicional recargoPersonaAdicional) {

    public ParametrosOperacion {
        if (umbralEdad == null || tiempoPreparacion == null || plazoConfirmacion == null
                || horaLimiteNoShow == null || anticipo == null || deposito == null
                || recargoPersonaAdicional == null) {
            throw new ReglaDominioException("Faltan parámetros de configuración del alojamiento.");
        }
    }

    /** L-11: anticipo 0 % significa que no se exige. */
    public boolean exigeAnticipo() {
        return !anticipo.esCero();
    }
}
