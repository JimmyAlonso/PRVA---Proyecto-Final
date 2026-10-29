package co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity;

import co.edu.uniquindio.sga.infrastructure.persistence.jpa.embeddable.PeriodoEmbeddable;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;

/** Tabla de temporadas (F-05), con sus rangos de fechas. */
@Entity
@Table(name = "temporada")
public class TemporadaEntity {

    @Id
    @Column(name = "id", length = 40)
    private String id;

    @Column(name = "nombre", nullable = false, length = 80)
    private String nombre;

    @Column(name = "es_base", nullable = false)
    private boolean esBase;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "temporada_periodo", joinColumns = @JoinColumn(name = "temporada"))
    @OrderColumn(name = "posicion")
    private List<PeriodoEmbeddable> periodos = new ArrayList<>();

    public TemporadaEntity() {
        // requerido por JPA
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public boolean isEsBase() {
        return esBase;
    }

    public void setEsBase(boolean esBase) {
        this.esBase = esBase;
    }

    public List<PeriodoEmbeddable> getPeriodos() {
        return periodos;
    }

    public void setPeriodos(List<PeriodoEmbeddable> periodos) {
        this.periodos = periodos;
    }
}
