package co.edu.uniquindio.sga.application.dto.request;

import co.edu.uniquindio.sga.application.usecase.OperarDomoUseCase.AccionEstadoOperativo;
import jakarta.validation.constraints.NotNull;

public record CambiarEstadoOperativoRequest(
        @NotNull(message = "La acción es obligatoria (INICIAR_PREPARACION, MARCAR_PREPARADO o DECLARAR_FUERA_DE_SERVICIO)")
        AccionEstadoOperativo accion
) {
}
