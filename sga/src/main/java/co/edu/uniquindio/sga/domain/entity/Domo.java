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
import co.edu.uniquindio.sga.domain.valueobject.RegistroTarifa;
import co.edu.uniquindio.sga.domain.valueobject.Tarifa;

import java.time.LocalDate;
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
    // 7.3: eliminación lógica. Un domo retirado de la venta no vuelve a activarse y los listados lo ignoran,
    // pero las reservas históricas lo siguen referenciando.
    private boolean eliminado;
    // Colecciones internas siempre de solo lectura; toda modificación reemplaza la referencia
    private List<Tarifa> tarifas = List.of();
    private List<RegistroTarifa> historialTarifas = List.of();

    public Domo(IdentificacionDomo identificacion, String nombre, int dormitorios, int capacidad) {
        if (identificacion == null) {
            throw new ReglaDominioException("El domo debe tener una identificación.");
        }
        validarDatos(nombre, dormitorios, capacidad);
        this.identificacion = identificacion;
        this.nombre = nombre.trim();
        this.dormitorios = dormitorios;
        this.capacidad = capacidad;
        this.estadoOperativo = EstadoOperativo.PREPARADO;
        this.activo = true;
        this.eliminado = false;
    }

    /**
     * 7.3 y 7.4: un domo nuevo se registra inactivo; solo se activa cuando tiene tarifa en todas
     * las temporadas (ver {@link #activar(List)}).
     */
    public static Domo registrar(IdentificacionDomo identificacion, String nombre, int dormitorios, int capacidad) {
        Domo domo = new Domo(identificacion, nombre, dormitorios, capacidad);
        domo.activo = false;
        return domo;
    }

    /** Rehidrata un domo ya persistido (adaptador JPA), sin pasar por las reglas de creación. */
    public static Domo reconstruir(IdentificacionDomo identificacion, String nombre, String descripcion,
                                   int dormitorios, int capacidad, List<String> caracteristicas, Jacuzzi jacuzzi,
                                   Malla malla, Fogata fogata, EstadoOperativo estadoOperativo, boolean activo,
                                   boolean eliminado, List<Tarifa> tarifas, List<RegistroTarifa> historialTarifas) {
        Domo domo = new Domo(identificacion, nombre, dormitorios, capacidad);
        domo.describir(descripcion, caracteristicas);
        domo.jacuzzi = jacuzzi;
        domo.malla = malla;
        domo.fogata = fogata;
        domo.estadoOperativo = estadoOperativo == null ? EstadoOperativo.PREPARADO : estadoOperativo;
        domo.activo = activo;
        domo.eliminado = eliminado;
        domo.tarifas = tarifas == null ? List.of() : List.copyOf(tarifas);
        domo.historialTarifas = historialTarifas == null ? List.of() : List.copyOf(historialTarifas);
        return domo;
    }

    private static void validarDatos(String nombre, int dormitorios, int capacidad) {
        if (nombre == null || nombre.isBlank()) {
            throw new ReglaDominioException("El domo debe tener un nombre.");
        }
        if (dormitorios < 1) {
            throw new ReglaDominioException("El domo debe tener al menos un dormitorio.");
        }
        if (capacidad < 1) {
            throw new ReglaDominioException("El domo debe tener capacidad para al menos un ocupante.");
        }
    }

    /**
     * 7.3: actualiza los atributos descriptivos. Cambiar la capacidad no afecta las reservas ya
     * creadas, aunque queden por encima de la nueva capacidad: el sistema las advierte
     * (InventarioDomoService.reservasQueSuperanCapacidad).
     */
    public void actualizarDatos(String nombre, String descripcion, int dormitorios, int capacidad,
                                List<String> caracteristicas) {
        verificarNoEliminado();
        validarDatos(nombre, dormitorios, capacidad);
        this.nombre = nombre.trim();
        this.dormitorios = dormitorios;
        this.capacidad = capacidad;
        describir(descripcion, caracteristicas);
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

    /**
     * 7.4: define o reemplaza la tarifa de una temporada. El valor anterior pasa al histórico;
     * las reservas ya creadas no cambian porque congelaron su valor (RN-22).
     */
    public void establecerTarifa(Tarifa nueva, LocalDate fecha) {
        if (nueva == null || fecha == null) {
            throw new ReglaDominioException("La tarifa y la fecha del cambio son obligatorias.");
        }
        verificarNoEliminado();
        Optional<Tarifa> anterior = tarifaEn(nueva.temporada());
        if (anterior.isEmpty()) {
            definirTarifa(nueva);
            return;
        }
        if (anterior.get().equals(nueva)) {
            return;
        }
        List<RegistroTarifa> historial = new ArrayList<>(historialTarifas);
        historial.add(new RegistroTarifa(anterior.get().temporada(), anterior.get().valorPorOcupanteNoche(), fecha));
        this.historialTarifas = List.copyOf(historial);
        List<Tarifa> actualizadas = new ArrayList<>(tarifas);
        actualizadas.remove(anterior.get());
        actualizadas.add(nueva);
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

    // EN_PREPARACION → PREPARADO (personal de servicio)
    public void marcarPreparado() {
        if (estadoOperativo != EstadoOperativo.EN_PREPARACION) {
            throw new ReglaDominioException("Solo se puede dejar PREPARADO un domo EN_PREPARACION.");
        }
        estadoOperativo = EstadoOperativo.PREPARADO;
    }

    // FUERA_DE_SERVICIO → PREPARADO (decisión administrativa, como la de sacarlo de servicio)
    public void restablecerServicio() {
        if (estadoOperativo != EstadoOperativo.FUERA_DE_SERVICIO) {
            throw new ReglaDominioException("Solo se puede restablecer un domo FUERA_DE_SERVICIO.");
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

    /** Saca el domo de la venta sin eliminarlo (por ejemplo, mientras se completan sus tarifas). */
    public void desactivar() {
        activo = false;
    }

    /** 7.4: el sistema impide activar un domo sin tarifas completas. */
    public void activar(List<Temporada> temporadas) {
        verificarNoEliminado();
        if (!tieneTarifasCompletas(temporadas)) {
            throw new ReglaDominioException("No se puede activar un domo sin tarifas en todas las temporadas.");
        }
        activo = true;
    }

    /**
     * 7.3 y 12.4: retiro de la venta con eliminación lógica. La condición de no tener reservas
     * activas ni futuras la verifica InventarioDomoService, porque este agregado no ve las reservas.
     */
    public void retirarDeVenta() {
        verificarNoEliminado();
        activo = false;
        eliminado = true;
    }

    private void verificarNoEliminado() {
        if (eliminado) {
            throw new ReglaDominioException("El domo " + identificacion.valor() + " fue retirado de la venta.");
        }
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

    public boolean isEliminado() {
        return eliminado;
    }

    public List<Tarifa> getTarifas() {
        return tarifas;
    }

    public List<RegistroTarifa> getHistorialTarifas() {
        return historialTarifas;
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
