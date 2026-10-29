package co.edu.uniquindio.sga.application.usecase;

import co.edu.uniquindio.sga.domain.entity.Reserva;
import co.edu.uniquindio.sga.domain.repository.CriteriosBusquedaReserva;
import co.edu.uniquindio.sga.domain.repository.ReservaRepository;
import co.edu.uniquindio.sga.domain.valueobject.EstadoReserva;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/** 7.6: llegadas y salidas previstas para un día (también insumo del resumen IA de la ficha). */
@Service
@Transactional(readOnly = true)
public class ConsultarMovimientosDelDiaUseCase {

    public record MovimientosDelDia(LocalDate fecha, List<Reserva> llegadas, List<Reserva> salidas) {
    }

    private final ReservaRepository reservaRepository;

    public ConsultarMovimientosDelDiaUseCase(ReservaRepository reservaRepository) {
        this.reservaRepository = reservaRepository;
    }

    public MovimientosDelDia ejecutar(LocalDate fecha) {
        List<Reserva> todas = reservaRepository.buscar(CriteriosBusquedaReserva.sinFiltros());
        List<Reserva> llegadas = todas.stream()
                .filter(r -> r.getEstado() == EstadoReserva.CONFIRMADA || r.getEstado() == EstadoReserva.PENDIENTE)
                .filter(r -> r.getEstancia().fechaEntrada().equals(fecha))
                .toList();
        List<Reserva> salidas = todas.stream()
                .filter(r -> r.getEstado() == EstadoReserva.EN_CURSO)
                .filter(r -> r.getEstancia().fechaSalida().equals(fecha))
                .toList();
        return new MovimientosDelDia(fecha, llegadas, salidas);
    }
}
