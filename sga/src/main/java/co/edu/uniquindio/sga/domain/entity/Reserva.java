package co.edu.uniquindio.sga.domain.entity;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.valueobject.CanalOrigen;
import co.edu.uniquindio.sga.domain.valueobject.CodigoReserva;
import co.edu.uniquindio.sga.domain.valueobject.Cotizacion;
import co.edu.uniquindio.sga.domain.valueobject.DetalleNoche;
import co.edu.uniquindio.sga.domain.valueobject.Dinero;
import co.edu.uniquindio.sga.domain.valueobject.EstadoReserva;
import co.edu.uniquindio.sga.domain.valueobject.Estancia;
import co.edu.uniquindio.sga.domain.valueobject.HoraEstimadaLlegada;
import co.edu.uniquindio.sga.domain.valueobject.HoraLimiteNoShow;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionDomo;
import co.edu.uniquindio.sga.domain.valueobject.IdentificadorExterno;
import co.edu.uniquindio.sga.domain.valueobject.PlazoConfirmacion;
import co.edu.uniquindio.sga.domain.valueobject.UmbralEdadFacturable;
import co.edu.uniquindio.sga.domain.valueobject.VersionPolitica;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Raíz del agregado Reserva.
 *
 * <p>Invariantes que garantiza este agregado: RN-02 (capacidad al crear y modificar), RN-03
 * (a través de {@link Estancia}), RN-04 (no se reserva hacia el pasado), RN-06 y 3.2 (el
 * titular es un ocupante facturable), RN-08 (transiciones de estado), RN-09 (hora estimada de
 * llegada antes de confirmar), RN-10 (registro de llegada), RN-14 (revalidación al modificar),
 * RN-21 (vencimiento del plazo de confirmación) y RN-22 (valor y política congelados).
 */
public class Reserva {

    private final CodigoReserva codigo;
    private IdentificacionDomo domo;
    private Estancia estancia;
    private EstadoReserva estado;
    private final CanalOrigen canalOrigen;
    private IdentificadorExterno identificadorExterno;
    private final TitularDeLaReserva titular;
    private List<Ocupante> ocupantes;
    private HoraEstimadaLlegada horaEstimadaLlegada;
    private Dinero valorCongelado;
    private List<DetalleNoche> desgloseCongelado;
    private final VersionPolitica politicaCongelada;
    private final LocalDateTime momentoCreacion;
    private String motivoCancelacion;

    private Reserva(CodigoReserva codigo, IdentificacionDomo domo, Estancia estancia, CanalOrigen canalOrigen,
                    TitularDeLaReserva titular, List<Ocupante> ocupantes, Cotizacion cotizacion,
                    VersionPolitica politicaCongelada, LocalDateTime momentoCreacion) {
        this.codigo = codigo;
        this.domo = domo;
        this.estancia = estancia;
        this.estado = EstadoReserva.PENDIENTE;
        this.canalOrigen = canalOrigen;
        this.titular = titular;
        this.ocupantes = ocupantes;
        this.valorCongelado = cotizacion.total();
        this.desgloseCongelado = cotizacion.noches();
        this.politicaCongelada = politicaCongelada;
        this.momentoCreacion = momentoCreacion;
    }

    /**
     * Nota de diseño: la capacidad entra como parámetro y no se consulta llamando a un
     * objeto {@code Domo}, porque este agregado no lee el estado interno de otro agregado.
     * Quien orquesta la creación es responsable de traer ese dato.
     */
    public static Reserva crear(CodigoReserva codigo,
                                IdentificacionDomo domo,
                                int capacidadDomo,
                                Estancia estancia,
                                TitularDeLaReserva titular,
                                List<Ocupante> ocupantes,
                                CanalOrigen canalOrigen,
                                Cotizacion cotizacion,
                                VersionPolitica politicaCongelada,
                                UmbralEdadFacturable umbral,
                                LocalDateTime momentoActual) {
        if (codigo == null) {
            throw new ReglaDominioException("La reserva debe tener un código.");
        }
        if (domo == null) {
            throw new ReglaDominioException("La reserva debe indicar el domo.");
        }
        if (canalOrigen == null) {
            throw new ReglaDominioException("La reserva debe indicar el canal de origen.");
        }
        if (cotizacion == null) {
            throw new ReglaDominioException("La reserva debe tener un valor calculado.");
        }
        if (politicaCongelada == null) {
            throw new ReglaDominioException("La reserva debe tener una política de cancelación vigente.");
        }
        if (momentoActual == null) {
            throw new ReglaDominioException("Se debe indicar el momento actual para crear la reserva.");
        }
        validarComposicion(estancia, titular, ocupantes, capacidadDomo, umbral, momentoActual.toLocalDate());

        // RN-22: el valor (con su desglose) y la política quedan congelados al crear la reserva
        return new Reserva(codigo, domo, estancia, canalOrigen, titular, List.copyOf(ocupantes),
                cotizacion, politicaCongelada, momentoActual);
    }

    /**
     * Rehidrata una reserva ya persistida (adaptador JPA). No repite las validaciones de
     * creación: una reserva guardada ya las cumplió y puede estar en el pasado o en un estado
     * terminal. Persistir no cambia el dominio: no hay setters ni constructor vacío.
     */
    public static Reserva reconstruir(CodigoReserva codigo,
                                      IdentificacionDomo domo,
                                      Estancia estancia,
                                      EstadoReserva estado,
                                      CanalOrigen canalOrigen,
                                      IdentificadorExterno identificadorExterno,
                                      TitularDeLaReserva titular,
                                      List<Ocupante> ocupantes,
                                      HoraEstimadaLlegada horaEstimadaLlegada,
                                      Dinero valorCongelado,
                                      List<DetalleNoche> desgloseCongelado,
                                      VersionPolitica politicaCongelada,
                                      LocalDateTime momentoCreacion,
                                      String motivoCancelacion) {
        if (codigo == null || domo == null || estancia == null || estado == null || canalOrigen == null
                || titular == null || ocupantes == null || valorCongelado == null || politicaCongelada == null
                || momentoCreacion == null) {
            throw new ReglaDominioException("Los datos guardados de la reserva están incompletos.");
        }
        Reserva reserva = new Reserva(codigo, domo, estancia, canalOrigen, titular, List.copyOf(ocupantes),
                new Cotizacion(desgloseCongelado), politicaCongelada, momentoCreacion);
        // RN-22: se respeta el valor congelado tal como se guardó, no se recalcula desde el desglose
        reserva.valorCongelado = valorCongelado;
        reserva.estado = estado;
        reserva.identificadorExterno = identificadorExterno;
        reserva.horaEstimadaLlegada = horaEstimadaLlegada;
        reserva.motivoCancelacion = motivoCancelacion;
        return reserva;
    }

    /** Validaciones compartidas por la creación y la modificación (RN-14). */
    private static void validarComposicion(Estancia estancia, TitularDeLaReserva titular, List<Ocupante> ocupantes,
                                           int capacidadDomo, UmbralEdadFacturable umbral, LocalDate fechaActual) {
        if (estancia == null) {
            throw new ReglaDominioException("La reserva debe indicar la estancia.");
        }
        if (titular == null) {
            throw new ReglaDominioException("La reserva debe tener un titular.");
        }
        if (ocupantes == null || ocupantes.isEmpty()) {
            throw new ReglaDominioException("La reserva debe tener al menos un ocupante.");
        }
        if (umbral == null) {
            throw new ReglaDominioException("Se debe indicar el umbral de edad facturable.");
        }
        if (new HashSet<>(ocupantes).size() != ocupantes.size()) {
            throw new ReglaDominioException("La reserva no puede tener ocupantes repetidos.");
        }
        // RN-04: no se crean ni se mueven reservas hacia el pasado
        if (estancia.fechaEntrada().isBefore(fechaActual)) {
            throw new ReglaDominioException("La fecha de entrada no puede ser anterior a la fecha actual.");
        }
        // RN-02: el número total de ocupantes no puede exceder la capacidad del domo
        if (ocupantes.size() > capacidadDomo) {
            throw new ReglaDominioException("El número de ocupantes (" + ocupantes.size()
                    + ") excede la capacidad del domo (" + capacidadDomo + ").");
        }
        // 3.2: el titular es siempre un ocupante facturable de la reserva
        Ocupante comoOcupante = ocupantes.stream()
                .filter(ocupante -> ocupante.getDocumento().equals(titular.getDocumento()))
                .findFirst()
                .orElseThrow(() -> new ReglaDominioException("El titular debe estar entre los ocupantes de la reserva."));
        if (!comoOcupante.esFacturableEn(estancia, umbral)) {
            throw new ReglaDominioException("El titular debe ser un ocupante facturable (alcanzar el umbral de edad).");
        }
    }

    public int totalOcupantes() {
        return ocupantes.size();
    }

    /** RN-06: un ocupante es facturable si a la fecha de entrada alcanza el umbral configurado. */
    public int ocupantesFacturables(UmbralEdadFacturable umbral) {
        return (int) ocupantes.stream()
                .filter(ocupante -> ocupante.esFacturableEn(estancia, umbral))
                .count();
    }

    /** RN-09: toda reserva debe registrar hora estimada de llegada antes de confirmarse. */
    public void registrarHoraEstimadaLlegada(HoraEstimadaLlegada hora) {
        if (hora == null) {
            throw new ReglaDominioException("La hora estimada de llegada es obligatoria.");
        }
        if (estado.esTerminal() || estado == EstadoReserva.EN_CURSO) {
            throw new ReglaDominioException("No se puede cambiar la hora estimada de llegada de una reserva " + estado + ".");
        }
        this.horaEstimadaLlegada = hora;
    }

    /** RN-08 y RN-09. Las precondiciones externas (anticipo, L-21) las verifica ConfirmacionReservaService. */
    public void confirmar() {
        if (this.horaEstimadaLlegada == null) {
            throw new ReglaDominioException("La reserva debe registrar hora estimada de llegada para confirmarse.");
        }
        transitarA(EstadoReserva.CONFIRMADA);
    }

    // RN-08 (transición) y RN-12 (al dejar de estar activa libera sus noches de inmediato,
    // porque EstadoReserva.esActiva() pasa a ser falso)
    public void cancelar(String motivo) {
        if (motivo == null || motivo.isBlank()) {
            throw new ReglaDominioException("La cancelación de una reserva debe indicar un motivo.");
        }
        transitarA(EstadoReserva.CANCELADA);
        this.motivoCancelacion = motivo.trim();
    }

    /** RN-21: una reserva PENDIENTE que supera el plazo de confirmación está vencida. */
    public boolean venceElPlazo(PlazoConfirmacion plazo, LocalDateTime momentoActual) {
        return estado == EstadoReserva.PENDIENTE
                && !momentoActual.isBefore(momentoCreacion.plusHours(plazo.horas()));
    }

    /** RN-21: se cancela automáticamente al vencer el plazo de confirmación. */
    public void vencerPorPlazo(PlazoConfirmacion plazo, LocalDateTime momentoActual) {
        if (!venceElPlazo(plazo, momentoActual)) {
            throw new ReglaDominioException("La reserva no ha superado el plazo de confirmación.");
        }
        cancelar("Vencimiento automático del plazo de confirmación (" + plazo.horas() + " horas).");
    }

    /**
     * RN-08 y RN-10: solo se registra la llegada si la reserva está CONFIRMADA y no antes de
     * la fecha de entrada. El estado operativo del domo (RN-11) y el depósito (L-22) son
     * precondiciones externas que verifica EntregaDomoService.
     */
    public void registrarLlegada(LocalDate fechaActual) {
        if (fechaActual == null) {
            throw new ReglaDominioException("Se debe indicar la fecha actual.");
        }
        if (estado != EstadoReserva.CONFIRMADA) {
            throw new ReglaDominioException("Solo se registra la llegada de una reserva CONFIRMADA (estado actual: " + estado + ").");
        }
        if (fechaActual.isBefore(estancia.fechaEntrada())) {
            throw new ReglaDominioException("No se puede registrar la llegada antes de la fecha de entrada.");
        }
        transitarA(EstadoReserva.EN_CURSO);
    }

    /** 7.5: la salida anticipada de un grupo registrado no es una cancelación, es una salida. */
    public void registrarSalida() {
        transitarA(EstadoReserva.FINALIZADA);
    }

    /** RN-08 y L-15: solo desde CONFIRMADA y a partir de la hora límite del día de entrada. */
    public void declararNoShow(LocalDateTime momentoActual, HoraLimiteNoShow horaLimite) {
        if (momentoActual == null || horaLimite == null) {
            throw new ReglaDominioException("Se debe indicar el momento actual y la hora límite de no-show.");
        }
        LocalDateTime limite = estancia.fechaEntrada().atTime(horaLimite.hora());
        if (momentoActual.isBefore(limite)) {
            throw new ReglaDominioException("Aún no se ha alcanzado la hora límite para declarar no-show ("
                    + limite + ").");
        }
        transitarA(EstadoReserva.NO_SHOW);
    }

    /**
     * RN-14 y 7.5 Modificación: fechas, ocupantes o domo, solo en PENDIENTE o CONFIRMADA y antes
     * de iniciar. Repite las validaciones propias y recibe el valor recalculado con las tarifas
     * vigentes (la disponibilidad la revalida el servicio de dominio). La política congelada no
     * cambia (RN-22). Devuelve el valor anterior para que el folio registre el ajuste.
     */
    public Dinero modificar(IdentificacionDomo nuevoDomo, int capacidadNuevoDomo, Estancia nuevaEstancia,
                            List<Ocupante> nuevosOcupantes, Cotizacion nuevaCotizacion,
                            UmbralEdadFacturable umbral, LocalDate fechaActual) {
        if (estado != EstadoReserva.PENDIENTE && estado != EstadoReserva.CONFIRMADA) {
            throw new ReglaDominioException("Solo se puede modificar una reserva PENDIENTE o CONFIRMADA.");
        }
        if (nuevoDomo == null || nuevaCotizacion == null) {
            throw new ReglaDominioException("La modificación debe indicar el domo y el valor recalculado.");
        }
        validarComposicion(nuevaEstancia, titular, nuevosOcupantes, capacidadNuevoDomo, umbral, fechaActual);

        Dinero valorAnterior = this.valorCongelado;
        this.domo = nuevoDomo;
        this.estancia = nuevaEstancia;
        this.ocupantes = List.copyOf(nuevosOcupantes);
        this.valorCongelado = nuevaCotizacion.total();
        this.desgloseCongelado = nuevaCotizacion.noches();
        return valorAnterior;
    }

    /**
     * 3.2 y RN-14: en una modificación el titular sigue siendo ocupante. Recibe los acompañantes
     * nuevos y devuelve la composición completa del grupo con el titular al inicio.
     */
    public List<Ocupante> conAcompanantes(List<Ocupante> acompanantes) {
        List<Ocupante> grupo = new ArrayList<>();
        ocupantes.stream()
                .filter(ocupante -> ocupante.getDocumento().equals(titular.getDocumento()))
                .findFirst()
                .ifPresent(grupo::add);
        if (acompanantes != null) {
            acompanantes.stream().filter(ocupante -> !grupo.contains(ocupante)).forEach(grupo::add);
        }
        return List.copyOf(grupo);
    }

    /** RN-14: nueva estancia a partir de las fechas que cambian; la que no llega se conserva. */
    public Estancia estanciaCon(LocalDate nuevaEntrada, LocalDate nuevaSalida) {
        if (nuevaEntrada == null && nuevaSalida == null) {
            return estancia;
        }
        return new Estancia(nuevaEntrada != null ? nuevaEntrada : estancia.fechaEntrada(),
                nuevaSalida != null ? nuevaSalida : estancia.fechaSalida());
    }

    /** RN-19: la combinación canal + identificador externo es única. */
    public void marcarComoExterna(IdentificadorExterno identificador) {
        if (identificador == null) {
            throw new ReglaDominioException("El identificador externo es obligatorio.");
        }
        if (canalOrigen != CanalOrigen.EXTERNO) {
            throw new ReglaDominioException("Solo una reserva del canal EXTERNO lleva identificador externo.");
        }
        this.identificadorExterno = identificador;
    }

    /** RN-01, RN-12: una reserva activa (PENDIENTE, CONFIRMADA o EN_CURSO) ocupa noches. */
    public boolean estaActiva() {
        return estado.esActiva();
    }

    private void transitarA(EstadoReserva destino) {
        // RN-08: la reserva solo transita entre los estados permitidos
        if (!estado.puedeTransitarA(destino)) {
            throw new ReglaDominioException("No es posible pasar la reserva de " + estado + " a " + destino + ".");
        }
        estado = destino;
    }

    public CodigoReserva getCodigo() {
        return codigo;
    }

    public IdentificacionDomo getDomo() {
        return domo;
    }

    public Estancia getEstancia() {
        return estancia;
    }

    public EstadoReserva getEstado() {
        return estado;
    }

    public CanalOrigen getCanalOrigen() {
        return canalOrigen;
    }

    public Optional<IdentificadorExterno> getIdentificadorExterno() {
        return Optional.ofNullable(identificadorExterno);
    }

    public TitularDeLaReserva getTitular() {
        return titular;
    }

    public List<Ocupante> getOcupantes() {
        return ocupantes;
    }

    public HoraEstimadaLlegada getHoraEstimadaLlegada() {
        return horaEstimadaLlegada;
    }

    public Dinero getValorCongelado() {
        return valorCongelado;
    }

    public List<DetalleNoche> getDesgloseCongelado() {
        return desgloseCongelado;
    }

    public VersionPolitica getPoliticaCongelada() {
        return politicaCongelada;
    }

    public LocalDateTime getMomentoCreacion() {
        return momentoCreacion;
    }

    public String getMotivoCancelacion() {
        return motivoCancelacion;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Reserva reserva)) return false;
        return Objects.equals(codigo, reserva.codigo);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(codigo);
    }
}
