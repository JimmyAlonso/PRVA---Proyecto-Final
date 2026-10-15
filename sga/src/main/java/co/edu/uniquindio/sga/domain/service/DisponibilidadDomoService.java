package co.edu.uniquindio.sga.domain.service;

import co.edu.uniquindio.sga.domain.entity.Bloqueo;
import co.edu.uniquindio.sga.domain.entity.Domo;
import co.edu.uniquindio.sga.domain.entity.Reserva;
import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.repository.BloqueoRepository;
import co.edu.uniquindio.sga.domain.repository.ReservaRepository;
import co.edu.uniquindio.sga.domain.valueobject.CodigoReserva;
import co.edu.uniquindio.sga.domain.valueobject.Disponibilidad;
import co.edu.uniquindio.sga.domain.valueobject.Estancia;
import co.edu.uniquindio.sga.domain.valueobject.HorarioAlojamiento;
import co.edu.uniquindio.sga.domain.valueobject.Periodo;
import co.edu.uniquindio.sga.domain.valueobject.TiempoPreparacion;

import java.util.List;

/**
 * Sección 3.3: un domo está disponible para un rango si, en todas sus noches, no tiene
 * reservas activas que solapen (RN-01, sin importar el canal), no tiene bloqueos (RN-07),
 * está activo y su capacidad alcanza para el grupo (RN-02). Además se respeta el tiempo de
 * preparación respecto de las estancias vecinas (RN-20). Las reservas que dejaron de estar
 * activas no cuentan (RN-12).
 *
 * <p>Corrección RN-20: antes se convertían las horas de preparación a días completos
 * ({@code ceilDiv(horas, 24)}), así que con 4 horas se impedía recibir un grupo el mismo día
 * de una salida, aunque la ventana 11:00 → 15:00 de la ficha alcanza justo.
 */
public class DisponibilidadDomoService {

    private final ReservaRepository reservaRepository;
    private final BloqueoRepository bloqueoRepository;
    // L-12 y L-13: configuración del alojamiento, llegan por constructor
    private final TiempoPreparacion tiempoPreparacion;
    private final HorarioAlojamiento horario;

    public DisponibilidadDomoService(ReservaRepository reservaRepository, BloqueoRepository bloqueoRepository,
                                     TiempoPreparacion tiempoPreparacion, HorarioAlojamiento horario) {
        if (tiempoPreparacion == null || horario == null) {
            throw new ReglaDominioException("El tiempo de preparación y el horario son obligatorios.");
        }
        this.reservaRepository = reservaRepository;
        this.bloqueoRepository = bloqueoRepository;
        this.tiempoPreparacion = tiempoPreparacion;
        this.horario = horario;
    }

    /**
     * @param excluir reserva que se está modificando (RN-14): no compite consigo misma. Puede ser null.
     */
    public Disponibilidad evaluar(Domo domo, Estancia estancia, int totalOcupantes, CodigoReserva excluir) {
        if (domo == null || estancia == null) {
            throw new ReglaDominioException("El domo y la estancia son obligatorios para evaluar disponibilidad.");
        }
        if (!domo.estaActivo()) {
            return Disponibilidad.ocupada("El domo " + domo.getIdentificacion().valor() + " no está activo.");
        }
        // RN-02
        if (!domo.admite(totalOcupantes)) {
            return Disponibilidad.ocupada("El grupo de " + totalOcupantes + " personas excede la capacidad del domo ("
                    + domo.getCapacidad() + ").");
        }

        // Se amplía el periodo de consulta para ver también a las estancias vecinas (RN-20)
        Periodo periodoConsulta = new Periodo(estancia.fechaEntrada().minusDays(1), estancia.fechaSalida().plusDays(1));
        // RN-12: las reservas que dejaron de estar activas no ocupan noches
        List<Reserva> activas = reservaRepository.buscarActivasPorDomo(domo.getIdentificacion(), periodoConsulta)
                .stream()
                .filter(Reserva::estaActiva)
                .filter(otra -> !otra.getCodigo().equals(excluir))
                .toList();

        // 7.5 condición 5a — RN-01: sin importar el canal de origen
        for (Reserva otra : activas) {
            if (estancia.seSolapaCon(otra.getEstancia())) {
                return Disponibilidad.ocupada("El domo ya tiene una reserva activa en esas noches (RN-01).");
            }
        }

        // 7.5 condición 5b — RN-07
        Periodo nochesSolicitadas = new Periodo(estancia.fechaEntrada(), estancia.fechaSalida().minusDays(1));
        for (Bloqueo bloqueo : bloqueoRepository.buscarVigentesPorDomo(domo.getIdentificacion(), nochesSolicitadas)) {
            if (bloqueo.afecta(estancia)) {
                return Disponibilidad.ocupada("El domo tiene un bloqueo vigente en esas noches (RN-07).");
            }
        }

        // 7.5 condición 6 — RN-20
        for (Reserva otra : activas) {
            if (violaTiempoPreparacion(estancia, otra.getEstancia())) {
                return Disponibilidad.ocupada("No se respeta el tiempo de preparación de "
                        + tiempoPreparacion.horas() + " horas respecto de otra estancia (RN-20).");
            }
        }
        return Disponibilidad.libre();
    }

    public boolean estaDisponible(Domo domo, Estancia estancia, int totalOcupantes) {
        return evaluar(domo, estancia, totalOcupantes, null).disponible();
    }

    public void verificarDisponibilidad(Domo domo, Estancia estancia, int totalOcupantes, CodigoReserva excluir) {
        Disponibilidad resultado = evaluar(domo, estancia, totalOcupantes, excluir);
        if (!resultado.disponible()) {
            throw new ReglaDominioException(resultado.motivo());
        }
    }

    // RN-20: la estancia que sale primero debe dejar el tiempo de preparación antes de la siguiente
    private boolean violaTiempoPreparacion(Estancia nueva, Estancia otra) {
        if (!nueva.fechaEntrada().isBefore(otra.fechaSalida())) {
            return !tiempoPreparacion.seRespetaEntre(otra.fechaSalida(), nueva.fechaEntrada(), horario);
        }
        if (!otra.fechaEntrada().isBefore(nueva.fechaSalida())) {
            return !tiempoPreparacion.seRespetaEntre(nueva.fechaSalida(), otra.fechaEntrada(), horario);
        }
        return false;
    }
}
