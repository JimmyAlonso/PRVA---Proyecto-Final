package co.edu.uniquindio.sga.domain.service;

import co.edu.uniquindio.sga.domain.entity.Bloqueo;
import co.edu.uniquindio.sga.domain.entity.Domo;
import co.edu.uniquindio.sga.domain.entity.Reserva;
import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.repository.BloqueoRepository;
import co.edu.uniquindio.sga.domain.repository.ReservaRepository;
import co.edu.uniquindio.sga.domain.valueobject.EstadoNoche;
import co.edu.uniquindio.sga.domain.valueobject.NocheCalendario;
import co.edu.uniquindio.sga.domain.valueobject.Periodo;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * 7.5 Calendario de ocupación por domo y rango de fechas. Combina dos agregados (reservas
 * activas y bloqueos vigentes) según la definición de disponibilidad de 3.3, por eso no vive en
 * ninguno de ellos. Solo las reservas activas ocupan noches (RN-12).
 */
public class CalendarioOcupacionService {

    private static final int MAXIMO_NOCHES = 366;

    private final ReservaRepository reservaRepository;
    private final BloqueoRepository bloqueoRepository;

    public CalendarioOcupacionService(ReservaRepository reservaRepository, BloqueoRepository bloqueoRepository) {
        this.reservaRepository = reservaRepository;
        this.bloqueoRepository = bloqueoRepository;
    }

    public List<NocheCalendario> calendario(Domo domo, Periodo noches) {
        if (domo == null || noches == null) {
            throw new ReglaDominioException("El domo y el rango de fechas son obligatorios.");
        }
        long cantidad = noches.hasta().toEpochDay() - noches.desde().toEpochDay() + 1;
        if (cantidad > MAXIMO_NOCHES) {
            throw new ReglaDominioException("El calendario se consulta por rangos de máximo " + MAXIMO_NOCHES + " noches.");
        }
        List<Reserva> activas = reservaRepository.buscarActivasPorDomo(domo.getIdentificacion(), noches).stream()
                .filter(Reserva::estaActiva)
                .toList();
        List<Bloqueo> bloqueos = bloqueoRepository.buscarVigentesPorDomo(domo.getIdentificacion(), noches);
        List<NocheCalendario> resultado = new ArrayList<>();
        for (LocalDate noche = noches.desde(); !noche.isAfter(noches.hasta()); noche = noche.plusDays(1)) {
            resultado.add(estadoDe(noche, activas, bloqueos));
        }
        return List.copyOf(resultado);
    }

    private static NocheCalendario estadoDe(LocalDate noche, List<Reserva> activas, List<Bloqueo> bloqueos) {
        for (Reserva reserva : activas) {
            if (reserva.getEstancia().incluye(noche)) {
                return new NocheCalendario(noche, EstadoNoche.RESERVADA, reserva.getCodigo().valor());
            }
        }
        for (Bloqueo bloqueo : bloqueos) {
            if (bloqueo.cubre(noche)) {
                return new NocheCalendario(noche, EstadoNoche.BLOQUEADA, bloqueo.getId().valor());
            }
        }
        return new NocheCalendario(noche, EstadoNoche.LIBRE, null);
    }
}
