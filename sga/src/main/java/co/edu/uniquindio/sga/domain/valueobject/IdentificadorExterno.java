package co.edu.uniquindio.sga.domain.valueobject;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;

/**
 * RN-19: la combinación canal + identificador externo es única.
 *
 * <p>Corrección: antes el canal era {@link CanalOrigen}, pero ese enum solo tiene EXTERNO,
 * así que dos plataformas distintas con el mismo identificador habrían chocado. Aquí el
 * canal es el código de la plataforma externa (por ejemplo "BOOKING_COM").
 */
public record IdentificadorExterno(String canal, String valor) {

    public IdentificadorExterno {
        if (canal == null || canal.isBlank()) {
            throw new ReglaDominioException("El identificador externo debe indicar el canal que lo emitió.");
        }
        if (valor == null || valor.isBlank()) {
            throw new ReglaDominioException("El identificador externo no puede estar vacío.");
        }
        canal = canal.trim().toUpperCase();
        valor = valor.trim();
    }
}
