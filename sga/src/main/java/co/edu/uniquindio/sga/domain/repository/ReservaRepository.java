package co.edu.uniquindio.sga.domain.repository;

import co.edu.uniquindio.sga.domain.entity.Reserva;
import co.edu.uniquindio.sga.domain.valueobject.CodigoReserva;
import co.edu.uniquindio.sga.domain.valueobject.DocumentoIdentidad;
import co.edu.uniquindio.sga.domain.valueobject.EstadoReserva;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionDomo;
import co.edu.uniquindio.sga.domain.valueobject.IdentificadorExterno;
import co.edu.uniquindio.sga.domain.valueobject.Periodo;

import java.util.List;
import java.util.Optional;

public interface ReservaRepository {

    /** RN-01: reservas activas del domo que podrían tocar el periodo indicado. */
    List<Reserva> buscarActivasPorDomo(IdentificacionDomo domo, Periodo periodo);

    /** 7.3: todas las reservas activas del domo (para retirarlo de la venta o advertir un cambio de capacidad). */
    List<Reserva> buscarActivasPorDomo(IdentificacionDomo domo);

    Optional<Reserva> obtenerPorCodigo(CodigoReserva codigo);

    List<Reserva> buscarPorEstado(EstadoReserva estado);

    /** 7.5 Listados: filtros por fechas, estado, domo, canal y titular; más reciente primero. */
    List<Reserva> buscar(CriteriosBusquedaReserva criterios);

    /** 7.5 y 12.4: la misma búsqueda, paginada en la consulta. */
    ResultadoPaginado<Reserva> buscar(CriteriosBusquedaReserva criterios, Paginacion paginacion);

    /** 7.2: un usuario titular de reservas activas no puede eliminarse. */
    boolean existenActivasDeTitular(DocumentoIdentidad documentoTitular);

    /** RN-19: canal + identificador externo es único. */
    Optional<Reserva> buscarPorIdentificadorExterno(IdentificadorExterno identificador);

    void guardar(Reserva reserva);
}
