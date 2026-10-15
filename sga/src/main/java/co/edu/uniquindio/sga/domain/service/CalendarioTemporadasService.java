package co.edu.uniquindio.sga.domain.service;

import co.edu.uniquindio.sga.domain.entity.Temporada;
import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;

import java.util.List;

/**
 * 7.4 Temporadas: no pueden solaparse entre sí y existe exactamente una temporada base
 * obligatoria, de modo que ninguna fecha reservable quede sin temporada (F-05).
 */
public class CalendarioTemporadasService {

    public void verificarCalendario(List<Temporada> temporadas) {
        long bases = temporadas.stream().filter(Temporada::isEsBase).count();
        if (bases != 1) {
            throw new ReglaDominioException("Debe existir exactamente una temporada base (hay " + bases + ").");
        }
        List<Temporada> especiales = temporadas.stream().filter(t -> !t.isEsBase()).toList();
        for (int i = 0; i < especiales.size(); i++) {
            for (int j = i + 1; j < especiales.size(); j++) {
                if (especiales.get(i).seSolapaCon(especiales.get(j))) {
                    throw new ReglaDominioException("Las temporadas " + especiales.get(i).getNombre() + " y "
                            + especiales.get(j).getNombre() + " se solapan.");
                }
            }
        }
    }

    /** Verifica que agregar una temporada nueva no rompa el calendario. */
    public void verificarNuevaTemporada(List<Temporada> existentes, Temporada nueva) {
        List<Temporada> todas = new java.util.ArrayList<>(existentes);
        todas.removeIf(t -> t.getId().equals(nueva.getId()));
        todas.add(nueva);
        verificarCalendario(todas);
    }
}
