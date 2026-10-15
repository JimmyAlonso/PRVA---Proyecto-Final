package co.edu.uniquindio.sga.application.usecase;

import co.edu.uniquindio.sga.application.dto.Pagina;
import co.edu.uniquindio.sga.domain.entity.Reserva;
import co.edu.uniquindio.sga.domain.repository.CriteriosBusquedaReserva;
import co.edu.uniquindio.sga.domain.repository.ReservaRepository;

/**
 * 7.5 Listados: reservas ordenadas de la más reciente a la más antigua, con filtros por
 * fechas, estado, domo, canal y titular, paginadas de 10 en 10.
 * (Reemplaza y amplía ConsultarReservasPorEstadoUseCase del repositorio guía.)
 */
public class ConsultarReservasUseCase {

    private final ReservaRepository reservaRepository;

    public ConsultarReservasUseCase(ReservaRepository reservaRepository) {
        this.reservaRepository = reservaRepository;
    }

    public Pagina<Reserva> ejecutar(CriteriosBusquedaReserva criterios, int pagina) {
        CriteriosBusquedaReserva filtro = criterios == null ? CriteriosBusquedaReserva.sinFiltros() : criterios;
        return Pagina.de(reservaRepository.buscar(filtro), pagina);
    }
}
