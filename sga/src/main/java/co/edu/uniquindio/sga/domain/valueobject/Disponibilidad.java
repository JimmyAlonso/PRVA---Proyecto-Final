package co.edu.uniquindio.sga.domain.valueobject;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;

public record Disponibilidad(boolean disponible, String motivo) {
    public Disponibilidad {
        if (!disponible && (motivo == null || motivo.isBlank()))
            throw new ReglaDominioException("Una no disponibilidad debe indicar su motivo.");
    }
    public static Disponibilidad libre() { return new Disponibilidad(true, null); }
    public static Disponibilidad ocupada(String motivo) { return new Disponibilidad(false, motivo); }
}
