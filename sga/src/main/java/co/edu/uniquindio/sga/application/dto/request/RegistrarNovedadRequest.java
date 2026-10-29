package co.edu.uniquindio.sga.application.dto.request;

import co.edu.uniquindio.sga.domain.valueobject.GravedadNovedad;
import co.edu.uniquindio.sga.domain.valueobject.TipoNovedad;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** 7.6: fecha y autor los pone el sistema (el autor es el usuario autenticado); llegan descripción y gravedad. */
public record RegistrarNovedadRequest(
        @NotNull(message = "El tipo de novedad es obligatorio (DANO, FALTANTE o SITUACION)")
        TipoNovedad tipo,
        @NotBlank(message = "La descripción es obligatoria")
        @Size(max = 1000, message = "La descripción admite máximo 1000 caracteres")
        String descripcion,
        @NotNull(message = "La gravedad es obligatoria (LEVE, MODERADA o GRAVE)")
        GravedadNovedad gravedad
) {
}
