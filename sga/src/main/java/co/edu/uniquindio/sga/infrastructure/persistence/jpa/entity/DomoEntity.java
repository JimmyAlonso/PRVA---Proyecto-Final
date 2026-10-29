package co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity;

import co.edu.uniquindio.sga.domain.valueobject.EstadoOperativo;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.embeddable.FogataEmbeddable;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.embeddable.JacuzziEmbeddable;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.embeddable.MallaEmbeddable;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.embeddable.RegistroTarifaEmbeddable;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.embeddable.TarifaEmbeddable;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;

/** Tabla de domos (F-01). Eliminación lógica con el indicador eliminado (12.4). */
@Entity
@Table(name = "domo")
public class DomoEntity {

    @Id
    @Column(name = "identificacion", length = 20)
    private String identificacion;

    @Column(name = "nombre", nullable = false, length = 120)
    private String nombre;

    @Column(name = "descripcion", length = 1000)
    private String descripcion;

    @Column(name = "dormitorios", nullable = false)
    private int dormitorios;

    @Column(name = "capacidad", nullable = false)
    private int capacidad;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "domo_caracteristica", joinColumns = @JoinColumn(name = "domo"))
    @OrderColumn(name = "posicion")
    @Column(name = "caracteristica", length = 200)
    private List<String> caracteristicas = new ArrayList<>();

    @Embedded
    private JacuzziEmbeddable jacuzzi;

    @Embedded
    private MallaEmbeddable malla;

    @Embedded
    private FogataEmbeddable fogata;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_operativo", nullable = false, length = 25)
    private EstadoOperativo estadoOperativo;

    @Column(name = "activo", nullable = false)
    private boolean activo;

    @Column(name = "eliminado", nullable = false)
    private boolean eliminado;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "domo_tarifa", joinColumns = @JoinColumn(name = "domo"))
    @OrderColumn(name = "posicion")
    private List<TarifaEmbeddable> tarifas = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "domo_tarifa_historial", joinColumns = @JoinColumn(name = "domo"))
    @OrderColumn(name = "posicion")
    private List<RegistroTarifaEmbeddable> historialTarifas = new ArrayList<>();

    public DomoEntity() {
        // requerido por JPA
    }

    public String getIdentificacion() {
        return identificacion;
    }

    public void setIdentificacion(String identificacion) {
        this.identificacion = identificacion;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public int getDormitorios() {
        return dormitorios;
    }

    public void setDormitorios(int dormitorios) {
        this.dormitorios = dormitorios;
    }

    public int getCapacidad() {
        return capacidad;
    }

    public void setCapacidad(int capacidad) {
        this.capacidad = capacidad;
    }

    public List<String> getCaracteristicas() {
        return caracteristicas;
    }

    public void setCaracteristicas(List<String> caracteristicas) {
        this.caracteristicas = caracteristicas;
    }

    public JacuzziEmbeddable getJacuzzi() {
        return jacuzzi;
    }

    public void setJacuzzi(JacuzziEmbeddable jacuzzi) {
        this.jacuzzi = jacuzzi;
    }

    public MallaEmbeddable getMalla() {
        return malla;
    }

    public void setMalla(MallaEmbeddable malla) {
        this.malla = malla;
    }

    public FogataEmbeddable getFogata() {
        return fogata;
    }

    public void setFogata(FogataEmbeddable fogata) {
        this.fogata = fogata;
    }

    public EstadoOperativo getEstadoOperativo() {
        return estadoOperativo;
    }

    public void setEstadoOperativo(EstadoOperativo estadoOperativo) {
        this.estadoOperativo = estadoOperativo;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public boolean isEliminado() {
        return eliminado;
    }

    public void setEliminado(boolean eliminado) {
        this.eliminado = eliminado;
    }

    public List<TarifaEmbeddable> getTarifas() {
        return tarifas;
    }

    public void setTarifas(List<TarifaEmbeddable> tarifas) {
        this.tarifas = tarifas;
    }

    public List<RegistroTarifaEmbeddable> getHistorialTarifas() {
        return historialTarifas;
    }

    public void setHistorialTarifas(List<RegistroTarifaEmbeddable> historialTarifas) {
        this.historialTarifas = historialTarifas;
    }
}
