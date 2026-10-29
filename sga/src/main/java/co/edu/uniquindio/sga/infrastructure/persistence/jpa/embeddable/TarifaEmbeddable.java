package co.edu.uniquindio.sga.infrastructure.persistence.jpa.embeddable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.math.BigDecimal;

/** RN-05: tarifa por ocupante facturable por noche en una temporada. */
@Embeddable
public class TarifaEmbeddable {

    @Column(name = "temporada", length = 40)
    private String temporada;

    @Column(name = "valor_por_ocupante_noche", precision = 15, scale = 2)
    private BigDecimal valorPorOcupanteNoche;

    protected TarifaEmbeddable() {
        // requerido por JPA
    }

    public TarifaEmbeddable(String temporada, BigDecimal valorPorOcupanteNoche) {
        this.temporada = temporada;
        this.valorPorOcupanteNoche = valorPorOcupanteNoche;
    }

    public String getTemporada() {
        return temporada;
    }

    public BigDecimal getValorPorOcupanteNoche() {
        return valorPorOcupanteNoche;
    }
}
