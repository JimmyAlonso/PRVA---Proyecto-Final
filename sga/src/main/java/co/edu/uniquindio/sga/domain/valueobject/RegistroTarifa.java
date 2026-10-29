package co.edu.uniquindio.sga.domain.valueobject;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;

import java.time.LocalDate;

/**
 * 7.4 Histórico de tarifas: valor que tuvo un domo en una temporada hasta que fue reemplazado.
 * Las reservas ya creadas no se afectan porque congelan su valor (RN-22).
 */
public record RegistroTarifa(IdTemporada temporada, Dinero valorPorOcupanteNoche, LocalDate reemplazadaEl) {

    public RegistroTarifa {
        if (temporada == null || valorPorOcupanteNoche == null || reemplazadaEl == null) {
            throw new ReglaDominioException("El registro histórico de la tarifa está incompleto.");
        }
    }
}
