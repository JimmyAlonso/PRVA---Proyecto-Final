package co.edu.uniquindio.sga.domain.entity;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.valueobject.IdTemporada;
import co.edu.uniquindio.sga.domain.valueobject.Periodo;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

/**
 * Raíz del agregado Temporada.
 *
 * <p>F-05: existe una temporada base obligatoria que cubre todas las fechas no asignadas.
 * L-08: JiyJu Glamping define Base, Alta (15 dic – 15 ene y Semana Santa) y Puentes/Festivos.
 *
 * <p>Corrección: antes la temporada tenía un único {@link Periodo}, pero la temporada Alta
 * de la ficha tiene dos rangos y la de Puentes tiene uno por cada fin de semana festivo.
 * La temporada base no necesita rangos: cubre lo que no cubra ninguna otra.
 */
public class Temporada {

    private final IdTemporada id;
    private String nombre;
    private List<Periodo> periodos;
    private final boolean esBase;

    public Temporada(IdTemporada id, String nombre, List<Periodo> periodos, boolean esBase) {
        if (id == null) {
            throw new ReglaDominioException("La temporada debe tener un identificador.");
        }
        if (nombre == null || nombre.isBlank()) {
            throw new ReglaDominioException("La temporada debe tener un nombre.");
        }
        this.id = id;
        this.esBase = esBase;
        this.nombre = nombre.trim();
        this.periodos = validarPeriodos(nombre, periodos, esBase);
    }

    private static List<Periodo> validarPeriodos(String nombre, List<Periodo> periodos, boolean esBase) {
        List<Periodo> rangos = periodos == null ? List.of() : List.copyOf(periodos);
        if (!esBase && rangos.isEmpty()) {
            throw new ReglaDominioException("Una temporada que no es la base debe indicar al menos un rango de fechas.");
        }
        // 7.4: la temporada base cubre todas las fechas no asignadas a otra; no lleva rangos propios
        if (esBase && !rangos.isEmpty()) {
            throw new ReglaDominioException("La temporada base cubre las fechas no asignadas: no lleva rangos propios.");
        }
        for (int i = 0; i < rangos.size(); i++) {
            for (int j = i + 1; j < rangos.size(); j++) {
                if (rangos.get(i).seSolapaCon(rangos.get(j))) {
                    throw new ReglaDominioException("Los rangos de la temporada " + nombre + " se solapan entre sí.");
                }
            }
        }
        return rangos;
    }

    /**
     * 7.4: cambia el nombre o los rangos de fechas. El no solapamiento con las demás temporadas
     * lo verifica CalendarioTemporadasService, que ve el calendario completo.
     */
    public void redefinir(String nuevoNombre, List<Periodo> nuevosPeriodos) {
        if (nuevoNombre == null || nuevoNombre.isBlank()) {
            throw new ReglaDominioException("La temporada debe tener un nombre.");
        }
        this.periodos = validarPeriodos(nuevoNombre, nuevosPeriodos, esBase);
        this.nombre = nuevoNombre.trim();
    }

    /** Temporada base: cubre por omisión todas las fechas no asignadas (F-05). */
    public static Temporada base(IdTemporada id, String nombre) {
        return new Temporada(id, nombre, List.of(), true);
    }

    /** RN-05: indica si la noche cae en alguno de los rangos de esta temporada. */
    public boolean cubre(LocalDate noche) {
        return periodos.stream().anyMatch(periodo -> periodo.contiene(noche));
    }

    /** 7.4: las temporadas no pueden solaparse entre sí. */
    public boolean seSolapaCon(Temporada otra) {
        return periodos.stream().anyMatch(propio ->
                otra.periodos.stream().anyMatch(propio::seSolapaCon));
    }

    public IdTemporada getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public List<Periodo> getPeriodos() {
        return periodos;
    }

    public boolean isEsBase() {
        return esBase;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Temporada temporada)) return false;
        return Objects.equals(id, temporada.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
