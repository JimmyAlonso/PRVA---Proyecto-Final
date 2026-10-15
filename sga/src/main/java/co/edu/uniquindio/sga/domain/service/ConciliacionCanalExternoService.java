package co.edu.uniquindio.sga.domain.service;

import co.edu.uniquindio.sga.domain.entity.Domo;
import co.edu.uniquindio.sga.domain.entity.PoliticaCancelacion;
import co.edu.uniquindio.sga.domain.entity.Reserva;
import co.edu.uniquindio.sga.domain.entity.Temporada;
import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.repository.ReservaRepository;
import co.edu.uniquindio.sga.domain.valueobject.CanalOrigen;
import co.edu.uniquindio.sga.domain.valueobject.CodigoReserva;
import co.edu.uniquindio.sga.domain.valueobject.Cotizacion;
import co.edu.uniquindio.sga.domain.valueobject.DatosReservaExterna;
import co.edu.uniquindio.sga.domain.valueobject.Disponibilidad;
import co.edu.uniquindio.sga.domain.valueobject.UmbralEdadFacturable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * RN-18 y RN-19: la detección de duplicados y de conflicto de canal exige consultar el
 * conjunto completo de reservas; ninguna reserva individual puede garantizarla.
 *
 * <p>Corrección: el nombre de la clase tenía una errata ("Concilacion") y la clase estaba
 * declarada con {@code <ResultadoConciliacion>} como genérico. Además guardaba directamente;
 * ahora devuelve el resultado y el caso de uso persiste la reserva, el folio o la novedad.
 */
public class ConciliacionCanalExternoService {

    private final ReservaRepository reservaRepository;
    private final DisponibilidadDomoService disponibilidadService;
    private final CotizadorEstanciaService cotizador;
    private final UmbralEdadFacturable umbral;

    public ConciliacionCanalExternoService(ReservaRepository reservaRepository,
                                           DisponibilidadDomoService disponibilidadService,
                                           CotizadorEstanciaService cotizador,
                                           UmbralEdadFacturable umbral) {
        this.reservaRepository = reservaRepository;
        this.disponibilidadService = disponibilidadService;
        this.cotizador = cotizador;
        this.umbral = umbral;
    }

    public ResultadoConciliacion conciliar(DatosReservaExterna datos, Domo domo, List<Temporada> temporadas,
                                           PoliticaCancelacion politicaVigente, CodigoReserva codigoNuevo,
                                           LocalDateTime momentoActual) {
        if (datos == null || domo == null || politicaVigente == null || codigoNuevo == null || momentoActual == null) {
            throw new ReglaDominioException("Datos, domo, política, código y momento actual son obligatorios.");
        }

        // RN-19: el mismo mensaje dos veces no crea dos reservas
        Optional<Reserva> existente = reservaRepository.buscarPorIdentificadorExterno(datos.identificador());
        if (existente.isPresent()) {
            return ResultadoConciliacion.duplicada(existente.get());
        }

        // RN-18: si colisiona, se rechaza y se registra; nunca sobrescribe la reserva vigente
        Disponibilidad disponibilidad = disponibilidadService.evaluar(domo, datos.estancia(), datos.ocupantes().size(), null);
        if (!disponibilidad.disponible()) {
            return ResultadoConciliacion.conflicto(disponibilidad.motivo());
        }

        domo.verificarQueAceptaReservas(temporadas);
        Cotizacion cotizacion = cotizador.cotizar(domo, datos.estancia(), datos.ocupantes(), temporadas);
        Reserva reserva = Reserva.crear(codigoNuevo, domo.getIdentificacion(), domo.getCapacidad(), datos.estancia(),
                datos.titular(), datos.ocupantes(), CanalOrigen.EXTERNO, cotizacion, politicaVigente.getVersion(),
                umbral, momentoActual);
        reserva.marcarComoExterna(datos.identificador());
        if (datos.horaEstimadaLlegada() != null) {
            reserva.registrarHoraEstimadaLlegada(datos.horaEstimadaLlegada());
        }
        return ResultadoConciliacion.creada(reserva);
    }
}
