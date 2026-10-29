package co.edu.uniquindio.sga.infrastructure.persistence.jpa.embeddable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

/** Glosario propio: Jacuzzi del domo con su protocolo de precalentamiento (L-21). */
@Embeddable
public class JacuzziEmbeddable {

    @Column(name = "jacuzzi_descripcion", length = 120)
    private String descripcion;

    @Column(name = "jacuzzi_horas_precalentamiento")
    private Integer horasPrecalentamiento;

    protected JacuzziEmbeddable() {
        // requerido por JPA
    }

    public JacuzziEmbeddable(String descripcion, Integer horasPrecalentamiento) {
        this.descripcion = descripcion;
        this.horasPrecalentamiento = horasPrecalentamiento;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public Integer getHorasPrecalentamiento() {
        return horasPrecalentamiento;
    }
}
