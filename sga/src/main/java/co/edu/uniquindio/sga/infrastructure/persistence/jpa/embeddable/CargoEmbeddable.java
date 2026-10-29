package co.edu.uniquindio.sga.infrastructure.persistence.jpa.embeddable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.math.BigDecimal;
import java.time.LocalDate;

/** RN-16: cargo del folio; nunca se edita ni se borra, la corrección es un movimiento inverso. */
@Embeddable
public class CargoEmbeddable {

    @Column(name = "concepto", length = 20)
    private String concepto;

    @Column(name = "valor", precision = 15, scale = 2)
    private BigDecimal valor;

    @Column(name = "fecha")
    private LocalDate fecha;

    @Column(name = "descripcion", length = 300)
    private String descripcion;

    @Column(name = "inverso")
    private Boolean inverso;

    protected CargoEmbeddable() {
        // requerido por JPA
    }

    public CargoEmbeddable(String concepto, BigDecimal valor, LocalDate fecha, String descripcion, Boolean inverso) {
        this.concepto = concepto;
        this.valor = valor;
        this.fecha = fecha;
        this.descripcion = descripcion;
        this.inverso = inverso;
    }

    public String getConcepto() {
        return concepto;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public Boolean getInverso() {
        return inverso;
    }
}
