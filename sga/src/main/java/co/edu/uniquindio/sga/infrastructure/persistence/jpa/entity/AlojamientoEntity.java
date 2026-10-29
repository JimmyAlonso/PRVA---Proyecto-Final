package co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity;

import co.edu.uniquindio.sga.infrastructure.persistence.jpa.embeddable.ServicioAdicionalEmbeddable;
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
import java.util.ArrayList;
import java.util.List;

/** Datos del único alojamiento (F-13) que gestiona el administrador. Los parámetros de operación son configuración. */
@Entity
@Table(name = "alojamiento")
public class AlojamientoEntity {

    @Id
    @Column(name = "id", length = 20)
    private String id;

    @Column(name = "nombre", nullable = false, length = 150)
    private String nombre;

    @Column(name = "descripcion", length = 1000)
    private String descripcion;

    @Column(name = "ciudad", nullable = false, length = 120)
    private String ciudad;

    @Column(name = "direccion", length = 300)
    private String direccion;

    @Column(name = "latitud", nullable = false, precision = 10, scale = 6)
    private BigDecimal latitud;

    @Column(name = "longitud", nullable = false, precision = 10, scale = 6)
    private BigDecimal longitud;

    @Column(name = "normas_convivencia", length = 1000)
    private String normasConvivencia;

    @Column(name = "acepta_mascotas", nullable = false)
    private boolean aceptaMascotas;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "alojamiento_servicio", joinColumns = @JoinColumn(name = "alojamiento"))
    @OrderColumn(name = "posicion")
    private List<ServicioAdicionalEmbeddable> servicios = new ArrayList<>();

    public AlojamientoEntity() {
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

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getCiudad() {
        return ciudad;
    }

    public void setCiudad(String ciudad) {
        this.ciudad = ciudad;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public BigDecimal getLatitud() {
        return latitud;
    }

    public void setLatitud(BigDecimal latitud) {
        this.latitud = latitud;
    }

    public BigDecimal getLongitud() {
        return longitud;
    }

    public void setLongitud(BigDecimal longitud) {
        this.longitud = longitud;
    }

    public String getNormasConvivencia() {
        return normasConvivencia;
    }

    public void setNormasConvivencia(String normasConvivencia) {
        this.normasConvivencia = normasConvivencia;
    }

    public boolean isAceptaMascotas() {
        return aceptaMascotas;
    }

    public void setAceptaMascotas(boolean aceptaMascotas) {
        this.aceptaMascotas = aceptaMascotas;
    }

    public List<ServicioAdicionalEmbeddable> getServicios() {
        return servicios;
    }

    public void setServicios(List<ServicioAdicionalEmbeddable> servicios) {
        this.servicios = servicios;
    }
}
