package co.edu.uniquindio.sga.domain.service;

import co.edu.uniquindio.sga.domain.entity.Domo;
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
    /** 7.4: una temporada nueva no reutiliza el identificador de otra y no se solapa con las demás. */
    public void verificarRegistro(List<Temporada> existentes, Temporada nueva) {
        if (existentes.stream().anyMatch(t -> t.getId().equals(nueva.getId()))) {
            throw new ReglaDominioException("Ya existe una temporada con el identificador " + nueva.getId().valor() + ".");
        }
        verificarNuevaTemporada(existentes, nueva);
    }

    public void verificarNuevaTemporada(List<Temporada> existentes, Temporada nueva) {
        List<Temporada> todas = new java.util.ArrayList<>(existentes);
        todas.removeIf(t -> t.getId().equals(nueva.getId()));
        todas.add(nueva);
        verificarCalendario(todas);
    }

    /**
     * 7.4: todo domo activo debe tener tarifa en todas las temporadas. Se verifica al crear o
     * cambiar el calendario para que ninguna fecha reservable quede sin tarifa.
     */
    public void verificarTarifasCompletas(List<Domo> domosActivos, List<Temporada> temporadas) {
        for (Domo domo : domosActivos) {
            for (Temporada temporada : temporadas) {
                if (domo.tarifaEn(temporada.getId()).isEmpty()) {
                    throw new ReglaDominioException("El domo activo " + domo.getIdentificacion().valor()
                            + " no tiene tarifa para la temporada " + temporada.getNombre()
                            + ": defina su tarifa al registrar la temporada.");
                }
            }
        }
    }
}
