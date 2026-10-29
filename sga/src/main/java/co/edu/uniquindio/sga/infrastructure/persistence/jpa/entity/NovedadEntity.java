package co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity;

import co.edu.uniquindio.sga.domain.valueobject.GravedadNovedad;
import co.edu.uniquindio.sga.domain.valueobject.TipoNovedad;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/** Tabla de novedades (7.6) y de conflictos de canal registrados (RN-18). */
@Entity
@Table(name = "novedad", indexes = @Index(name = "ix_novedad_domo", columnList = "domo"))
public class NovedadEntity {

    @Id
    @Column(name = "id", length = 40)
    private String id;

    @Column(name = "domo", nullable = false, length = 20)
    private String domo;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 20)
    private TipoNovedad tipo;

    @Column(name = "descripcion", nullable = false, length = 1000)
    private String descripcion;

    @Enumerated(EnumType.STRING)
    @Column(name = "gravedad", nullable = false, length = 15)
    private GravedadNovedad gravedad;

    @Column(name = "autor", nullable = false, length = 150)
    private String autor;

    @Column(name = "momento_reporte", nullable = false)
    private LocalDateTime momentoReporte;

    @Column(name = "atendida", nullable = false)
    private boolean atendida;

    public NovedadEntity() {
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

    public TipoNovedad getTipo() {
        return tipo;
    }

    public void setTipo(TipoNovedad tipo) {
        this.tipo = tipo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public GravedadNovedad getGravedad() {
        return gravedad;
    }

    public void setGravedad(GravedadNovedad gravedad) {
        this.gravedad = gravedad;
    }

    public String getAutor() {
        return autor;
    }

    public void setAutor(String autor) {
        this.autor = autor;
    }

    public LocalDateTime getMomentoReporte() {
        return momentoReporte;
    }

    public void setMomentoReporte(LocalDateTime momentoReporte) {
        this.momentoReporte = momentoReporte;
    }

    public boolean isAtendida() {
        return atendida;
    }

    public void setAtendida(boolean atendida) {
        this.atendida = atendida;
    }
}
