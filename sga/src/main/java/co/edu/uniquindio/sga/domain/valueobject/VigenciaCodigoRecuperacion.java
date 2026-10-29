package co.edu.uniquindio.sga.domain.valueobject;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;

/** 7.2: minutos de vigencia del código de recuperación de contraseña (15 en el enunciado). Es configuración. */
public record VigenciaCodigoRecuperacion(int minutos) {

    public VigenciaCodigoRecuperacion {
        if (minutos <= 0) {
            throw new ReglaDominioException("La vigencia del código de recuperación debe ser mayor que cero.");
        }
    }
}
