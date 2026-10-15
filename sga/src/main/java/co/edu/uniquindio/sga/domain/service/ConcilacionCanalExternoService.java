package co.edu.uniquindio.sga.domain.service;

import co.edu.uniquindio.sga.domain.entity.Domo;
import co.edu.uniquindio.sga.domain.entity.Novedad;
import co.edu.uniquindio.sga.domain.entity.Reserva;
import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.repository.DomoRepository;
import co.edu.uniquindio.sga.domain.repository.NovedadRepository;
import co.edu.uniquindio.sga.domain.repository.ReservaRepository;
import co.edu.uniquindio.sga.domain.valueobject.*;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

// ConciliacionCanalExternoService.java — RN-18, RN-19
public class ConciliacionCanalExternoService<ResultadoConciliacion> {
    private final ReservaRepository reservaRepository;
    private final DomoRepository domoRepository;
    private final NovedadRepository novedadRepository;
    private final DisponibilidadDomoService disponibilidadService;
    private final TiempoPreparacion tiempoPreparacion;

    public ConciliacionCanalExternoService(ReservaRepository reservaRepository, DomoRepository domoRepository,
                                           NovedadRepository novedadRepository,
                                           DisponibilidadDomoService disponibilidadService,
                                           TiempoPreparacion tiempoPreparacion) {
        this.reservaRepository = reservaRepository;
        this.domoRepository = domoRepository;
        this.novedadRepository = novedadRepository;
        this.disponibilidadService = disponibilidadService;
        this.tiempoPreparacion = tiempoPreparacion;
    }

    public ResultadoConciliacion conciliar(IdentificadorExterno identificador, DatosReservaExterna datos,
                                           LocalDate fechaActual) {
        if (identificador == null || datos == null || fechaActual == null)
            throw new ReglaDominioException("Identificador, datos y fecha actual son obligatorios.");
        if (identificador.canal() != CanalOrigen.EXTERNO)
            throw new ReglaDominioException("El identificador externo debe pertenecer al canal EXTERNO.");

        // RN-19: el mismo mensaje dos veces no crea dos reservas
        Optional<Reserva> existente = reservaRepository.buscarPorIdentificadorExterno(identificador);
        if (existente.isPresent())
            return new ResultadoConciliacion(ResultadoConciliacion.Tipo.DUPLICADA, existente.get());

        Domo domo = domoRepository.obtenerPorIdentificacion(datos.domo())
                .orElseThrow(() -> new ReglaDominioException("No existe el domo solicitado."));

        // RN-18: si colisiona, se rechaza y se registra; nunca sobrescribe
        if (!domo.estaActivo()
                || !disponibilidadService.estaDisponible(datos.domo(), datos.estancia(), tiempoPreparacion)) {
            novedadRepository.guardar(new Novedad(
                    new IdNovedad(UUID.randomUUID().toString()), datos.domo(),
                    "Conflicto de canal: la reserva externa " + identificador.valor()
                            + " colisiona con una reserva o bloqueo vigente.",
                    GravedadNovedad.MODERADA, fechaActual));
            return new ResultadoConciliacion(ResultadoConciliacion.Tipo.CONFLICTO, null);
        }

        Reserva reserva = Reserva.crear(datos.codigo(), datos.domo(), domo.getCapacidad(),
                datos.estancia(), datos.titular(), datos.ocupantes(), CanalOrigen.EXTERNO,
                datos.valor(), datos.politica(), fechaActual);
        reserva.marcarComoExterna(identificador);
        reservaRepository.guardar(reserva);
        return new ResultadoConciliacion(ResultadoConciliacion.Tipo.CREADA, reserva);
    }
}
