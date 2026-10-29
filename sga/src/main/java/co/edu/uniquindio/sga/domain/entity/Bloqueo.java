package co.edu.uniquindio.sga.domain.entity;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.valueobject.Estancia;
import co.edu.uniquindio.sga.domain.valueobject.IdBloqueo;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionDomo;
import co.edu.uniquindio.sga.domain.valueobject.MotivoBloqueo;
import co.edu.uniquindio.sga.domain.valueobject.Periodo;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Raíz del agregado Bloqueo: decisión administrativa que impide vender un domo en un rango
 * de fechas (3.3). No cambia el estado operativo del domo, ni al revés.
 */
public class Bloqueo {

    private final IdBloqueo id;
    private final IdentificacionDomo domo;
    private final Periodo periodo;
    private final MotivoBloqueo motivo;
    private final String observacion;
    private boolean vigente;

    public Bloqueo(IdBloqueo id, IdentificacionDomo domo, Periodo periodo, MotivoBloqueo motivo, String observacion) {
        if (id == null) {
            throw new ReglaDominioException("El bloqueo debe tener un identificador.");
        }
        if (domo == null) {
            throw new ReglaDominioException("El bloqueo debe indicar el domo afectado.");
        }
        if (periodo == null) {
            throw new ReglaDominioException("El bloqueo debe indicar el periodo que cubre.");
        }
        if (motivo == null) {
            throw new ReglaDominioException("El bloqueo debe indicar su motivo.");
        }
        this.id = id;
        this.domo = domo;
        this.periodo = periodo;
        this.motivo = motivo;
        this.observacion = observacion;
        this.vigente = true;
    }

    /** RN-07: un domo con bloqueo vigente sobre una noche no está disponible para esa noche. */
    /** Rehidrata un bloqueo ya persistido, vigente o levantado. */
    public static Bloqueo reconstruir(IdBloqueo id, IdentificacionDomo domo, Periodo periodo, MotivoBloqueo motivo,
                                      String observacion, boolean vigente) {
        Bloqueo bloqueo = new Bloqueo(id, domo, periodo, motivo, observacion);
        bloqueo.vigente = vigente;
        return bloqueo;
    }

    public boolean cubre(LocalDate noche) {
        return vigente && periodo.contiene(noche);
    }

    /** RN-07: una estancia se ve afectada si alguna de sus noches está cubierta por el bloqueo. */
    public boolean afecta(Estancia estancia) {
        return estancia.nochesOcupadas().stream().anyMatch(this::cubre);
    }

    public void levantar() {
        this.vigente = false;
    }

    public IdBloqueo getId() {
        return id;
    }

    public IdentificacionDomo getDomo() {
        return domo;
    }

    public Periodo getPeriodo() {
        return periodo;
    }

    public MotivoBloqueo getMotivo() {
        return motivo;
    }

    public String getObservacion() {
        return observacion;
    }

    public boolean isVigente() {
        return vigente;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Bloqueo bloqueo)) return false;
        return Objects.equals(id, bloqueo.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
