package co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity;

import co.edu.uniquindio.sga.infrastructure.persistence.jpa.embeddable.TramoRetencionEmbeddable;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Tabla de versiones de la política de cancelación (F-07): ninguna versión se sobrescribe. */
@Entity
@Table(name = "politica_cancelacion")
public class PoliticaCancelacionEntity {

    @Id
    @Column(name = "numero_version")
    private Integer numeroVersion;

    @Column(name = "vigente_desde", nullable = false)
    private LocalDate vigenteDesde;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "politica_tramo", joinColumns = @JoinColumn(name = "numero_version"))
    @OrderColumn(name = "posicion")
    private List<TramoRetencionEmbeddable> tramos = new ArrayList<>();

    @Column(name = "retencion_no_show", nullable = false, precision = 5, scale = 2)
    private BigDecimal retencionNoShow;

    public PoliticaCancelacionEntity() {
        // requerido por JPA
    }

    public Integer getNumeroVersion() {
        return numeroVersion;
    }

    public void setNumeroVersion(Integer numeroVersion) {
        this.numeroVersion = numeroVersion;
    }

    public LocalDate getVigenteDesde() {
        return vigenteDesde;
    }

    public void setVigenteDesde(LocalDate vigenteDesde) {
        this.vigenteDesde = vigenteDesde;
    }

    public List<TramoRetencionEmbeddable> getTramos() {
        return tramos;
    }

    public void setTramos(List<TramoRetencionEmbeddable> tramos) {
        this.tramos = tramos;
    }

    public BigDecimal getRetencionNoShow() {
        return retencionNoShow;
    }

    public void setRetencionNoShow(BigDecimal retencionNoShow) {
        this.retencionNoShow = retencionNoShow;
    }
}
