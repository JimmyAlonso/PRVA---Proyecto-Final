package co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity;

import co.edu.uniquindio.sga.domain.valueobject.MotivoBloqueo;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.embeddable.PeriodoEmbeddable;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

/** Tabla de bloqueos (RN-07). Un bloqueo levantado queda con vigente = false (eliminación lógica). */
@Entity
@Table(name = "bloqueo", indexes = @Index(name = "ix_bloqueo_domo", columnList = "domo"))
public class BloqueoEntity {

    @Id
    @Column(name = "id", length = 40)
    private String id;

    @Column(name = "domo", nullable = false, length = 20)
    private String domo;

    @Embedded
    private PeriodoEmbeddable periodo;

    @Enumerated(EnumType.STRING)
    @Column(name = "motivo", nullable = false, length = 25)
    private MotivoBloqueo motivo;

    @Column(name = "observacion", length = 300)
    private String observacion;

    @Column(name = "vigente", nullable = false)
    private boolean vigente;

    public BloqueoEntity() {
        // requerido por JPA
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDomo() {
        return domo;
    }

    public void setDomo(String domo) {
        this.domo = domo;
    }

    public PeriodoEmbeddable getPeriodo() {
        return periodo;
    }

    public void setPeriodo(PeriodoEmbeddable periodo) {
        this.periodo = periodo;
    }

    public MotivoBloqueo getMotivo() {
        return motivo;
    }

    public void setMotivo(MotivoBloqueo motivo) {
        this.motivo = motivo;
    }

    public String getObservacion() {
        return observacion;
    }

    public void setObservacion(String observacion) {
        this.observacion = observacion;
    }

    public boolean isVigente() {
        return vigente;
    }

    public void setVigente(boolean vigente) {
        this.vigente = vigente;
    }
}
