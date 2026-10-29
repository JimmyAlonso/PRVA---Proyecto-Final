package co.edu.uniquindio.sga.domain.entity;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.valueobject.GravedadNovedad;
import co.edu.uniquindio.sga.domain.valueobject.IdNovedad;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionDomo;
import co.edu.uniquindio.sga.domain.valueobject.TipoNovedad;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Raíz del agregado Novedad: reporte de un daño, faltante o situación en un domo (7.6), con
 * fecha, autor, descripción y gravedad. También registra los conflictos de canal (RN-18).
 */
public class Novedad {

    private final IdNovedad id;
    private final IdentificacionDomo domo;
    private final TipoNovedad tipo;
    private final String descripcion;
    private GravedadNovedad gravedad;
    private final String autor;
    private final LocalDateTime momentoReporte;
    private boolean atendida;

    public Novedad(IdNovedad id, IdentificacionDomo domo, TipoNovedad tipo, String descripcion,
                   GravedadNovedad gravedad, String autor, LocalDateTime momentoReporte) {
        if (id == null) {
            throw new ReglaDominioException("La novedad debe tener un identificador.");
        }
        if (domo == null) {
            throw new ReglaDominioException("La novedad debe indicar el domo afectado.");
        }
        if (tipo == null) {
            throw new ReglaDominioException("La novedad debe indicar su tipo.");
        }
        if (descripcion == null || descripcion.isBlank()) {
            throw new ReglaDominioException("La novedad debe tener una descripción.");
        }
        if (gravedad == null) {
            throw new ReglaDominioException("La novedad debe indicar su gravedad.");
        }
        if (autor == null || autor.isBlank()) {
            throw new ReglaDominioException("La novedad debe indicar su autor.");
        }
        if (momentoReporte == null) {
            throw new ReglaDominioException("La novedad debe indicar el momento del reporte.");
        }
        this.id = id;
        this.domo = domo;
        this.tipo = tipo;
        this.descripcion = descripcion.trim();
        this.gravedad = gravedad;
        this.autor = autor.trim();
        this.momentoReporte = momentoReporte;
        this.atendida = false;
    }

    /** Rehidrata una novedad ya persistida, atendida o no. */
    public static Novedad reconstruir(IdNovedad id, IdentificacionDomo domo, TipoNovedad tipo, String descripcion,
                                      GravedadNovedad gravedad, String autor, LocalDateTime momentoReporte,
                                      boolean atendida) {
        Novedad novedad = new Novedad(id, domo, tipo, descripcion, gravedad, autor, momentoReporte);
        novedad.atendida = atendida;
        return novedad;
    }

    public void reclasificar(GravedadNovedad nuevaGravedad) {
        if (nuevaGravedad == null) {
            throw new ReglaDominioException("La gravedad es obligatoria.");
        }
        this.gravedad = nuevaGravedad;
    }

    public void marcarAtendida() {
        this.atendida = true;
    }

    public IdNovedad getId() {
        return id;
    }

    public IdentificacionDomo getDomo() {
        return domo;
    }

    public TipoNovedad getTipo() {
        return tipo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public GravedadNovedad getGravedad() {
        return gravedad;
    }

    public String getAutor() {
        return autor;
    }

    public LocalDateTime getMomentoReporte() {
        return momentoReporte;
    }

    public boolean isAtendida() {
        return atendida;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Novedad novedad)) return false;
        return Objects.equals(id, novedad.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
