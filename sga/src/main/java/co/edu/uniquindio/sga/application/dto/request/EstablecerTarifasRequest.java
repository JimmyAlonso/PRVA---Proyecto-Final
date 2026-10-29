package co.edu.uniquindio.sga.application.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/** 7.4: define o cambia las tarifas de un domo; el valor anterior pasa al histórico. */
public record EstablecerTarifasRequest(
        @NotEmpty(message = "Indique al menos una tarifa")
        List<@Valid TarifaRequest> tarifas
) {
}
