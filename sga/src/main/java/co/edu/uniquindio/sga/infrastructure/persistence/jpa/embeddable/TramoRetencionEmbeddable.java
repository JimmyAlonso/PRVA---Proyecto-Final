package co.edu.uniquindio.sga.infrastructure.persistence.jpa.embeddable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.math.BigDecimal;

/** L-10: tramo de antelación con su porcentaje de retención. */
@Embeddable
public class TramoRetencionEmbeddable {

    @Column(name = "horas_antelacion_minima")
    private Integer horasAntelacionMinima;

    @Column(name = "porcentaje_retencion", precision = 5, scale = 2)
    private BigDecimal porcentajeRetencion;

    protected TramoRetencionEmbeddable() {
        // requerido por JPA
    }

    public TramoRetencionEmbeddable(Integer horasAntelacionMinima, BigDecimal porcentajeRetencion) {
        this.horasAntelacionMinima = horasAntelacionMinima;
        this.porcentajeRetencion = porcentajeRetencion;
    }

    public Integer getHorasAntelacionMinima() {
        return horasAntelacionMinima;
    }

    public BigDecimal getPorcentajeRetencion() {
        return porcentajeRetencion;
    }
}
