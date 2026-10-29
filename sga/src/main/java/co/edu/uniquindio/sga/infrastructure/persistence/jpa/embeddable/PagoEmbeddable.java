package co.edu.uniquindio.sga.infrastructure.persistence.jpa.embeddable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.math.BigDecimal;
import java.time.LocalDate;

/** RN-15: pago con medio y fecha registrado contra el folio. */
@Embeddable
public class PagoEmbeddable {

    @Column(name = "id_pago", length = 40)
    private String idPago;

    @Column(name = "monto", precision = 15, scale = 2)
    private BigDecimal monto;

    @Column(name = "medio", length = 20)
    private String medio;

    @Column(name = "fecha")
    private LocalDate fecha;

    protected PagoEmbeddable() {
        // requerido por JPA
    }

    public PagoEmbeddable(String idPago, BigDecimal monto, String medio, LocalDate fecha) {
        this.idPago = idPago;
        this.monto = monto;
        this.medio = medio;
        this.fecha = fecha;
    }

    public String getIdPago() {
        return idPago;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public String getMedio() {
        return medio;
    }

    public LocalDate getFecha() {
        return fecha;
    }
}
