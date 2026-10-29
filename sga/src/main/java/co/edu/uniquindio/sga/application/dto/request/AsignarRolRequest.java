package co.edu.uniquindio.sga.application.dto.request;

import co.edu.uniquindio.sga.domain.valueobject.Rol;
import jakarta.validation.constraints.NotNull;

public record AsignarRolRequest(
        @NotNull(message = "El rol es obligatorio (HUESPED, RECEPCIONISTA, ADMINISTRADOR o PERSONAL_SERVICIO)")
        Rol rol
) {
}
