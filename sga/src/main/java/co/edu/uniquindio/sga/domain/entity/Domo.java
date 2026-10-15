package co.edu.uniquindio.sga.domain.entity;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.valueobject.EstadoOperativo;
import co.edu.uniquindio.sga.domain.valueobject.Fogata;
import co.edu.uniquindio.sga.domain.valueobject.HoraEstimadaLlegada;
import co.edu.uniquindio.sga.domain.valueobject.HorarioAlojamiento;
import co.edu.uniquindio.sga.domain.valueobject.IdTemporada;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionDomo;
import co.edu.uniquindio.sga.domain.valueobject.Jacuzzi;
import co.edu.uniquindio.sga.domain.valueobject.Malla;
import co.edu.uniquindio.sga.domain.valueobject.Tarifa;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Raíz del agregado Domo: la unidad vendible completa de JiyJu Glamping (F-01).
 *
 * <p>Jacuzzi, Malla y Fogata son objetos de valor del glosario propio: describen la dotación
 * del domo y alimentan reglas propias (L-21), pero no tienen identidad.
 */
public class Domo {

    private final IdentificacionDomo identificacion;
    private String nombre;
    private String descripcion;
    private int dormitorios;
    private int capacidad;
    private List<String> caracteristicas = List.of();
    private Jacuzzi jacuzzi;
    private Malla malla;
    private Fogata fogata;
    private EstadoOperativo estadoOperativo;
    private boolean activo;
    // Colección interna siempre de solo lectura; toda modificación reemplaza la referencia
    private List<Tarifa> tarifas = List.of();

    public Domo(IdentificacionDomo identificacion, String nombre, int dormitorios, int capacidad) {
        if (identificacion == null) {
            throw new ReglaDominioException("El domo debe tener una identificación.");
        }
        if (nombre == null || nombre.isBlank()) {
            throw new ReglaDominioException("El domo debe tener un nombre.");
        }
        if (dormitorios < 1) {
            throw new ReglaDominioException("El domo debe tener al menos un dormitorio.");
        }
        if (capacidad < 1) {
            throw new ReglaDominioException("El domo debe tener capacidad para al menos un ocupante.");
        }
        this.identificacion = identificacion;
        this.nombre = nombre.trim();
        this.dormitorios = dormitorios;
        this.capacidad = capacidad;
        this.estadoOperativo = EstadoOperativo.PREPARADO;
        this.activo = true;
    }

    // ---------------------------------------------------------------- dotación (L-06)

    public void describir(String descripcion, List<String> caracteristicas) {
        this.descripcion = descripcion;
        this.caracteristicas = caracteristicas == null ? List.of() : List.copyOf(caracteristicas);
    }

    public void equiparJacuzzi(Jacuzzi jacuzzi) {
        this.jacuzzi = jacuzzi;
    }

    public void equiparMalla(Malla malla) {
        this.malla = malla;
    }

    public void equiparFogata(Fogata fogata) {
        this.fogata = fogata;
    }

    public boolean tieneJacuzzi() {
        return jacuzzi != null;
    }

    /**
     * L-21: para confirmar una reserva sobre un domo con Jacuzzi, la hora estimada de llegada
     * debe dejar el tiempo de precalentamiento respecto de la hora oficial de entrada.
     */
    public void verificarAlistamientoJacuzzi(HoraEstimadaLlegada llegada, HorarioAlojamiento horario) {
        if (jacuzzi == null || llegada == null) {
            return;
        }
        if (!jacuzzi.alcanzaAlistamiento(llegada, horario)) {
            throw new ReglaDominioException(
                    "L-21: el domo " + identificacion.valor() + " tiene Jacuzzi y requiere "
                            + jacuzzi.horasPrecalentamiento() + " horas de precalentamiento. "
                            + "La hora estimada de llegada debe ser a partir de las "
                            + jacuzzi.llegadaMasTempranaAdmitida(horario) + ".");
        }
    }

    // ---------------------------------------------------------------- reglas de venta

    /** RN-02: la capacidad es un tope rígido, sin excepciones. Todos los ocupantes cuentan. */
    public boolean admite(int totalOcupantes) {
        return totalOcupantes <= capacidad;
    }

    /** 7.4: todo domo activo debe tener tarifa en todas las temporadas. */
    public boolean tieneTarifasCompletas(List<Temporada> temporadas) {
        return temporadas.stream().allMatch(temporada -> tarifaEn(temporada.getId()).isPresent());
    }

    /** 7.5 condición 3: domo activo y con tarifas completas. */
    public void verificarQueAceptaReservas(List<Temporada> temporadas) {
        if (!activo) {
            throw new ReglaDominioException("El domo " + identificacion.valor() + " no está activo y no admite reservas.");
        }
        if (!tieneTarifasCompletas(temporadas)) {
            throw new ReglaDominioException("El domo " + identificacion.valor()
                    + " no tiene tarifa definida en todas las temporadas.");
        }
    }

    /** RN-11: solo puede recibir un grupo si está activo y en estado operativo PREPARADO. */
    public boolean puedeRecibirGrupo() {
        return activo && estadoOperativo.permiteRegistro();
    }

    /** RN-05: tarifa vigente del domo para una temporada dada. */
    public Optional<Tarifa> tarifaEn(IdTemporada temporada) {
        return tarifas.stream()
                .filter(tarifa -> tarifa.temporada().equals(temporada))
                .findFirst();
    }

    public void definirTarifa(Tarifa tarifa) {
        if (tarifa == null) {
            throw new ReglaDominioException("La tarifa a definir es obligatoria.");
        }
        if (tarifaEn(tarifa.temporada()).isPresent()) {
            throw new ReglaDominioException("Ya existe una tarifa definida para esa temporada.");
        }
        List<Tarifa> actualizadas = new ArrayList<>(tarifas);
        actualizadas.add(tarifa);
        this.tarifas = List.copyOf(actualizadas);
    }

    // ---------------------------------------------------------------- estado operativo (7.6)

    // PREPARADO → OCUPADO (registro)
    public void marcarOcupado() {
        if (estadoOperativo != EstadoOperativo.PREPARADO) {
            throw new ReglaDominioException("Solo se puede ocupar un domo que está PREPARADO.");
        }
        estadoOperativo = EstadoOperativo.OCUPADO;
    }

    // OCUPADO → PENDIENTE_PREPARACION (salida)
    public void marcarPendientePreparacion() {
        if (estadoOperativo != EstadoOperativo.OCUPADO) {
            throw new ReglaDominioException("Solo se puede marcar pendiente de preparación un domo OCUPADO.");
        }
        estadoOperativo = EstadoOperativo.PENDIENTE_PREPARACION;
    }

    // PENDIENTE_PREPARACION → EN_PREPARACION (personal de servicio)
    public void iniciarPreparacion() {
        if (estadoOperativo != EstadoOperativo.PENDIENTE_PREPARACION) {
            throw new ReglaDominioException("Solo se puede iniciar la preparación de un domo PENDIENTE_PREPARACION.");
        }
        estadoOperativo = EstadoOperativo.EN_PREPARACION;
    }

    // EN_PREPARACION → PREPARADO (servicio) o FUERA_DE_SERVICIO → PREPARADO (administrador)
    public void marcarPreparado() {
        if (estadoOperativo != EstadoOperativo.EN_PREPARACION && estadoOperativo != EstadoOperativo.FUERA_DE_SERVICIO) {
            throw new ReglaDominioException("Solo se puede dejar PREPARADO un domo EN_PREPARACION o FUERA_DE_SERVICIO.");
        }
        estadoOperativo = EstadoOperativo.PREPARADO;
    }

    /**
     * 7.6: FUERA_DE_SERVICIO desde cualquier estado NO ocupado, solo por administrador.
     * Corrección: antes se permitía declarar fuera de servicio un domo OCUPADO.
     */
    public void declararFueraDeServicio() {
        if (estadoOperativo == EstadoOperativo.OCUPADO) {
            throw new ReglaDominioException("No se puede declarar fuera de servicio un domo OCUPADO.");
        }
        if (estadoOperativo == EstadoOperativo.FUERA_DE_SERVICIO) {
            throw new ReglaDominioException("El domo ya está fuera de servicio.");
        }
        estadoOperativo = EstadoOperativo.FUERA_DE_SERVICIO;
    }

    // ---------------------------------------------------------------- ciclo de vida

    /** 7.3: la eliminación es lógica. Quien la invoca verifica que no tenga reservas activas ni futuras. */
    public void desactivar() {
        activo = false;
    }

    /** 7.4: el sistema impide activar un domo sin tarifas completas. */
    public void activar(List<Temporada> temporadas) {
        if (!tieneTarifasCompletas(temporadas)) {
            throw new ReglaDominioException("No se puede activar un domo sin tarifas en todas las temporadas.");
        }
        activo = true;
    }

    // ---------------------------------------------------------------- getters

    public IdentificacionDomo getIdentificacion() {
        return identificacion;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public int getDormitorios() {
        return dormitorios;
    }

    // Cambiar la capacidad no afecta las reservas ya creadas (RN-22): no vuelven a leer este campo.
    public int getCapacidad() {
        return capacidad;
    }

    public List<String> getCaracteristicas() {
        return caracteristicas;
    }

    public Optional<Jacuzzi> getJacuzzi() {
        return Optional.ofNullable(jacuzzi);
    }

    public Optional<Malla> getMalla() {
        return Optional.ofNullable(malla);
    }

    public Optional<Fogata> getFogata() {
        return Optional.ofNullable(fogata);
    }

    public EstadoOperativo getEstadoOperativo() {
        return estadoOperativo;
    }

    public boolean estaActivo() {
        return activo;
    }

    public List<Tarifa> getTarifas() {
        return tarifas;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Domo that)) return false;
        return Objects.equals(identificacion, that.identificacion);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(identificacion);
    }
}
