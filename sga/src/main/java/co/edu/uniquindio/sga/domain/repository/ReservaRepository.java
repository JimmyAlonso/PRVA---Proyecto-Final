package co.edu.uniquindio.sga.domain.repository;

import co.edu.uniquindio.sga.domain.entity.Reserva;
import co.edu.uniquindio.sga.domain.valueobject.CodigoReserva;
import co.edu.uniquindio.sga.domain.valueobject.EstadoReserva;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionDomo;
import co.edu.uniquindio.sga.domain.valueobject.IdentificadorExterno;
import co.edu.uniquindio.sga.domain.valueobject.Periodo;

import java.util.List;
import java.util.Optional;

public interface ReservaRepository {

    /** RN-01: reservas activas del domo que podrían tocar el periodo indicado. */
    List<Reserva> buscarActivasPorDomo(IdentificacionDomo domo, Periodo periodo);

    Optional<Reserva> obtenerPorCodigo(CodigoReserva codigo);

    List<Reserva> buscarPorEstado(EstadoReserva estado);

    /** 7.5 Listados: filtros por fechas, estado, domo, canal y titular; más reciente primero. */
    List<Reserva> buscar(CriteriosBusquedaReserva criterios);

    /** RN-19: canal + identificador externo es único. */
    Optional<Reserva> buscarPorIdentificadorExterno(IdentificadorExterno identificador);

    void guardar(Reserva reserva);
}
