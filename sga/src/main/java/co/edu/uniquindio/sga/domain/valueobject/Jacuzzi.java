package co.edu.uniquindio.sga.domain.valueobject;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;

import java.time.LocalTime;

/**
 * Glosario propio: tina de hidromasaje exterior asociada al Domo, con protocolo de
 * precalentamiento previo a la llegada del huésped.
 */
public record Jacuzzi(String descripcion, int horasPrecalentamiento) {

    public Jacuzzi {
        if (horasPrecalentamiento <= 0) {
            throw new ReglaDominioException("Las horas de precalentamiento deben ser mayores que cero.");
        }
        descripcion = (descripcion == null || descripcion.isBlank()) ? "Jacuzzi" : descripcion.trim();
    }

    /**
     * L-21 (Ficha A.6): la hora estimada de llegada debe guardar una diferencia de al menos
     * {@code horasPrecalentamiento} con la hora oficial de entrada. Con entrada 15:00 y 3 horas,
     * la llegada más temprana admitida es 18:00.
     */
    public LocalTime llegadaMasTempranaAdmitida(HorarioAlojamiento horario) {
        return horario.horaEntrada().plusHours(horasPrecalentamiento);
    }

    public boolean alcanzaAlistamiento(HoraEstimadaLlegada llegada, HorarioAlojamiento horario) {
        return !llegada.hora().isBefore(llegadaMasTempranaAdmitida(horario));
    }
}
