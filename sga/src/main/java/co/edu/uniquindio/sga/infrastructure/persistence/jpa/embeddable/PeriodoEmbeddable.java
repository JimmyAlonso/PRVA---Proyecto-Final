package co.edu.uniquindio.sga.infrastructure.persistence.jpa.embeddable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.time.LocalDate;

/** Objeto de valor Periodo: rango de fechas con ambos extremos incluidos. */
@Embeddable
public class PeriodoEmbeddable {

    @Column(name = "desde")
    private LocalDate desde;

    @Column(name = "hasta")
    private LocalDate hasta;

    protected PeriodoEmbeddable() {
        // requerido por JPA
    }

    public PeriodoEmbeddable(LocalDate desde, LocalDate hasta) {
        this.desde = desde;
        this.hasta = hasta;
    }

    public LocalDate getDesde() {
        return desde;
    }

    public LocalDate getHasta() {
        return hasta;
    }
}
