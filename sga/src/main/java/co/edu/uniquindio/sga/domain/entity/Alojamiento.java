package co.edu.uniquindio.sga.domain.entity;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.valueobject.HorarioAlojamiento;
import co.edu.uniquindio.sga.domain.valueobject.ParametrosOperacion;
import co.edu.uniquindio.sga.domain.valueobject.ServicioAdicional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * El alojamiento (F-13: el sistema administra uno solo): JiyJu Glamping, Salento.
 * Reúne los datos de 7.3 y los parámetros de la sección 6.
 *
 * <p>Corrección: antes era {@code Alojamiento<ServicioAdicional, RecargoPersonaAdicional>},
 * es decir, los nombres de clases se habían declarado como parámetros de tipo genérico, y
 * además usaba Lombok dentro del dominio. Ahora no tiene anotaciones ni genéricos.
 */
public class Alojamiento {

    private final String nombre;
    private String descripcion;
    private final String ciudad;
    private String direccion;
    private BigDecimal latitud;
    private BigDecimal longitud;
    private HorarioAlojamiento horario;
    private String normasConvivencia;
    private ParametrosOperacion parametros;
    private boolean aceptaMascotas;
    private List<ServicioAdicional> servicios = List.of();

    public Alojamiento(String nombre, String descripcion, String ciudad, String direccion,
                       BigDecimal latitud, BigDecimal longitud, HorarioAlojamiento horario,
                       String normasConvivencia, ParametrosOperacion parametros, boolean aceptaMascotas) {
        if (nombre == null || nombre.isBlank()) {
            throw new ReglaDominioException("El alojamiento debe tener nombre.");
        }
        if (ciudad == null || ciudad.isBlank()) {
            throw new ReglaDominioException("El alojamiento debe indicar la ciudad.");
        }
        if (latitud == null || longitud == null) {
            throw new ReglaDominioException("Falta la ubicación exacta (latitud y longitud).");
        }
        if (horario == null) {
            throw new ReglaDominioException("Faltan hora de entrada y de salida.");
        }
        if (parametros == null) {
            throw new ReglaDominioException("Faltan los parámetros de operación del alojamiento.");
        }
        if (!parametros.horaLimiteNoShow().hora().isAfter(horario.horaEntrada())) {
            throw new ReglaDominioException("La hora límite de no-show debe ser posterior a la hora de entrada.");
        }
        this.nombre = nombre.trim();
        this.descripcion = descripcion;
        this.ciudad = ciudad.trim();
        this.direccion = direccion;
        this.latitud = latitud;
        this.longitud = longitud;
        this.horario = horario;
        this.normasConvivencia = normasConvivencia;
        this.parametros = parametros;
        this.aceptaMascotas = aceptaMascotas;
    }

    /** L-17: los servicios adicionales tienen código único dentro del alojamiento. */
    /** 7.3: datos del alojamiento que gestiona el administrador. Nombre y ciudad identifican al negocio (L-01, L-02). */
    public void actualizarDatos(String descripcion, String direccion, BigDecimal latitud, BigDecimal longitud,
                                String normasConvivencia) {
        if (latitud == null || longitud == null) {
            throw new ReglaDominioException("Falta la ubicación exacta (latitud y longitud).");
        }
        this.descripcion = descripcion;
        this.direccion = direccion;
        this.latitud = latitud;
        this.longitud = longitud;
        this.normasConvivencia = normasConvivencia;
    }

    /** 7.3: reemplaza un servicio adicional existente o lo agrega si es nuevo. */
    public void definirServicio(ServicioAdicional servicio) {
        if (servicio == null) {
            throw new ReglaDominioException("El servicio adicional es obligatorio.");
        }
        List<ServicioAdicional> nuevos = new ArrayList<>(servicios);
        nuevos.removeIf(s -> s.codigo().equals(servicio.codigo()));
        nuevos.add(servicio);
        this.servicios = List.copyOf(nuevos);
    }

    public void agregarServicio(ServicioAdicional servicio) {
        if (servicio == null) {
            throw new ReglaDominioException("El servicio adicional es obligatorio.");
        }
        if (buscarServicio(servicio.codigo()).isPresent()) {
            throw new ReglaDominioException("Ya existe un servicio con el código " + servicio.codigo() + ".");
        }
        List<ServicioAdicional> nuevos = new ArrayList<>(servicios);
        nuevos.add(servicio);
        this.servicios = List.copyOf(nuevos);
    }

    public Optional<ServicioAdicional> buscarServicio(String codigo) {
        if (codigo == null) {
            return Optional.empty();
        }
        return servicios.stream().filter(s -> s.codigo().equalsIgnoreCase(codigo.trim())).findFirst();
    }

    public void actualizarParametros(ParametrosOperacion nuevos) {
        if (nuevos == null) {
            throw new ReglaDominioException("Los parámetros son obligatorios.");
        }
        this.parametros = nuevos;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getCiudad() {
        return ciudad;
    }

    public String getDireccion() {
        return direccion;
    }

    public BigDecimal getLatitud() {
        return latitud;
    }

    public BigDecimal getLongitud() {
        return longitud;
    }

    public HorarioAlojamiento getHorario() {
        return horario;
    }

    public String getNormasConvivencia() {
        return normasConvivencia;
    }

    public ParametrosOperacion getParametros() {
        return parametros;
    }

    public boolean isAceptaMascotas() {
        return aceptaMascotas;
    }

    public List<ServicioAdicional> getServicios() {
        return servicios;
    }
}
