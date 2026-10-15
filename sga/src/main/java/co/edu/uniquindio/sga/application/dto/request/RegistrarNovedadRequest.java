package co.edu.uniquindio.sga.application.dto.request;

import co.edu.uniquindio.sga.domain.valueobject.GravedadNovedad;
import co.edu.uniquindio.sga.domain.valueobject.TipoNovedad;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

// 7.6: fecha (la pone el sistema), autor, descripción y gravedad
public record RegistrarNovedadRequest(
        @NotNull(message = "El tipo de novedad es obligatorio (DANO, FALTANTE o SITUACION)")
        TipoNovedad tipo,

        @NotBlank(message = "La descripción es obligatoria")
        String descripcion,

        @NotNull(message = "La gravedad es obligatoria (LEVE, MODERADA o GRAVE)")
        GravedadNovedad gravedad,

        @NotBlank(message = "El autor es obligatorio")
        String autor
) {
}
