package co.edu.uniquindio.sga.infrastructure.persistence.jpa.embeddable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.math.BigDecimal;
import java.time.LocalDate;

/** 7.4: histórico de tarifas reemplazadas. */
@Embeddable
public class RegistroTarifaEmbeddable {

    @Column(name = "temporada", length = 40)
    private String temporada;

    @Column(name = "valor_por_ocupante_noche", precision = 15, scale = 2)
    private BigDecimal valorPorOcupanteNoche;

    @Column(name = "reemplazada_el")
    private LocalDate reemplazadaEl;

    protected RegistroTarifaEmbeddable() {
        // requerido por JPA
    }

    public RegistroTarifaEmbeddable(String temporada, BigDecimal valorPorOcupanteNoche, LocalDate reemplazadaEl) {
        this.temporada = temporada;
        this.valorPorOcupanteNoche = valorPorOcupanteNoche;
        this.reemplazadaEl = reemplazadaEl;
    }

    public String getTemporada() {
        return temporada;
    }

    public BigDecimal getValorPorOcupanteNoche() {
        return valorPorOcupanteNoche;
    }

    public LocalDate getReemplazadaEl() {
        return reemplazadaEl;
    }
}
