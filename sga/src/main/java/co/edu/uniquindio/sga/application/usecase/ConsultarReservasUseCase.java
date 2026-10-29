package co.edu.uniquindio.sga.application.usecase;

import co.edu.uniquindio.sga.application.dto.Pagina;
import co.edu.uniquindio.sga.domain.entity.Reserva;
import co.edu.uniquindio.sga.domain.repository.CriteriosBusquedaReserva;
import co.edu.uniquindio.sga.domain.repository.ReservaRepository;
import co.edu.uniquindio.sga.domain.valueobject.DocumentoIdentidad;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 7.5 Listados: filtros por fechas, estado, domo, canal y titular, de la más reciente a la más antigua. */
@Service
@Transactional(readOnly = true)
public class ConsultarReservasUseCase {

    private final ReservaRepository reservaRepository;

    public ConsultarReservasUseCase(ReservaRepository reservaRepository) {
        this.reservaRepository = reservaRepository;
    }

    public Pagina<Reserva> ejecutar(CriteriosBusquedaReserva criterios, int pagina) {
        CriteriosBusquedaReserva filtro = criterios == null ? CriteriosBusquedaReserva.sinFiltros() : criterios;
        return Pagina.desde(reservaRepository.buscar(filtro, Pagina.solicitud(pagina)));
    }

    /** 7.1 Huésped: su historial de reservas (las que tienen su documento como titular). */
    public Pagina<Reserva> deTitular(DocumentoIdentidad documento, int pagina) {
        CriteriosBusquedaReserva filtro = new CriteriosBusquedaReserva(null, null, null, documento, null, null);
        return Pagina.desde(reservaRepository.buscar(filtro, Pagina.solicitud(pagina)));
    }
}
