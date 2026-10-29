package co.edu.uniquindio.sga.infrastructure.persistence.jpa.embeddable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.math.BigDecimal;
import java.time.LocalDate;

/** RN-22: desglose noche por noche congelado al crear la reserva. */
@Embeddable
public class DetalleNocheEmbeddable {

    @Column(name = "fecha")
    private LocalDate fecha;

    @Column(name = "temporada", length = 40)
    private String temporada;

    @Column(name = "nombre_temporada", length = 80)
    private String nombreTemporada;

    @Column(name = "tarifa_aplicada", precision = 15, scale = 2)
    private BigDecimal tarifaAplicada;

    @Column(name = "ocupantes_facturables")
    private Integer ocupantesFacturables;

    @Column(name = "recargo_persona_adicional", precision = 15, scale = 2)
    private BigDecimal recargoPersonaAdicional;

    @Column(name = "subtotal", precision = 15, scale = 2)
    private BigDecimal subtotal;

    protected DetalleNocheEmbeddable() {
        // requerido por JPA
    }

    public DetalleNocheEmbeddable(LocalDate fecha, String temporada, String nombreTemporada, BigDecimal tarifaAplicada, Integer ocupantesFacturables, BigDecimal recargoPersonaAdicional, BigDecimal subtotal) {
        this.fecha = fecha;
        this.temporada = temporada;
        this.nombreTemporada = nombreTemporada;
        this.tarifaAplicada = tarifaAplicada;
        this.ocupantesFacturables = ocupantesFacturables;
        this.recargoPersonaAdicional = recargoPersonaAdicional;
        this.subtotal = subtotal;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public String getTemporada() {
        return temporada;
    }

    public String getNombreTemporada() {
        return nombreTemporada;
    }

    public BigDecimal getTarifaAplicada() {
        return tarifaAplicada;
    }

    public Integer getOcupantesFacturables() {
        return ocupantesFacturables;
    }

    public BigDecimal getRecargoPersonaAdicional() {
        return recargoPersonaAdicional;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }
}
