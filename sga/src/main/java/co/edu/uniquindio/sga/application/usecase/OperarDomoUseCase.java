package co.edu.uniquindio.sga.application.usecase;

import co.edu.uniquindio.sga.application.dto.Pagina;
import co.edu.uniquindio.sga.domain.entity.Bloqueo;
import co.edu.uniquindio.sga.domain.entity.Domo;
import co.edu.uniquindio.sga.domain.entity.Novedad;
import co.edu.uniquindio.sga.domain.entity.Reserva;
import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.repository.BloqueoRepository;
import co.edu.uniquindio.sga.domain.repository.DomoRepository;
import co.edu.uniquindio.sga.domain.repository.NovedadRepository;
import co.edu.uniquindio.sga.domain.repository.ReservaRepository;
import co.edu.uniquindio.sga.domain.valueobject.EstadoOperativo;
import co.edu.uniquindio.sga.domain.valueobject.GravedadNovedad;
import co.edu.uniquindio.sga.domain.valueobject.IdBloqueo;
import co.edu.uniquindio.sga.domain.valueobject.IdNovedad;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionDomo;
import co.edu.uniquindio.sga.domain.valueobject.MotivoBloqueo;
import co.edu.uniquindio.sga.domain.valueobject.Periodo;
import co.edu.uniquindio.sga.domain.valueobject.TipoNovedad;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 7.3 y 7.6: consultar domos, cambiar su estado operativo, registrar novedades y bloqueos.
 * La autorización por rol (el personal de servicio no declara FUERA_DE_SERVICIO ni bloquea)
 * pertenece a la seguridad (Corte 2); aquí se validan las reglas de negocio.
 */
public class OperarDomoUseCase {

    /** 7.6: transiciones de estado operativo que se solicitan explícitamente. */
    public enum AccionEstadoOperativo {
        INICIAR_PREPARACION,
        MARCAR_PREPARADO,
        DECLARAR_FUERA_DE_SERVICIO
    }

    private final DomoRepository domoRepository;
    private final NovedadRepository novedadRepository;
    private final BloqueoRepository bloqueoRepository;
    private final ReservaRepository reservaRepository;

    public OperarDomoUseCase(DomoRepository domoRepository, NovedadRepository novedadRepository,
                             BloqueoRepository bloqueoRepository, ReservaRepository reservaRepository) {
        this.domoRepository = domoRepository;
        this.novedadRepository = novedadRepository;
        this.bloqueoRepository = bloqueoRepository;
        this.reservaRepository = reservaRepository;
    }

    public Pagina<Domo> listar(int pagina) {
        return Pagina.de(domoRepository.listarActivos(), pagina);
    }

    public Domo consultar(IdentificacionDomo id) {
        return Buscar.domo(domoRepository, id);
    }

    /** 7.1 Personal de servicio: domos pendientes de preparación. */
    public List<Domo> pendientesDePreparacion() {
        return domoRepository.listarActivos().stream()
                .filter(d -> d.getEstadoOperativo() == EstadoOperativo.PENDIENTE_PREPARACION
                        || d.getEstadoOperativo() == EstadoOperativo.EN_PREPARACION)
                .toList();
    }

    public Domo cambiarEstado(IdentificacionDomo id, AccionEstadoOperativo accion) {
        if (accion == null) {
            throw new ReglaDominioException("Se debe indicar la acción sobre el estado operativo.");
        }
        Domo domo = Buscar.domo(domoRepository, id);
        switch (accion) {
            case INICIAR_PREPARACION -> domo.iniciarPreparacion();
            case MARCAR_PREPARADO -> domo.marcarPreparado();
            case DECLARAR_FUERA_DE_SERVICIO -> domo.declararFueraDeServicio();
        }
        domoRepository.guardar(domo);
        return domo;
    }

    public Novedad registrarNovedad(IdentificacionDomo id, TipoNovedad tipo, String descripcion,
                                    GravedadNovedad gravedad, String autor, LocalDateTime momento) {
        Buscar.domo(domoRepository, id);
        Novedad novedad = new Novedad(new IdNovedad(UUID.randomUUID().toString()), id, tipo, descripcion,
                gravedad, autor, momento);
        novedadRepository.guardar(novedad);
        return novedad;
    }

    public Pagina<Novedad> historialNovedades(IdentificacionDomo id, int pagina) {
        Buscar.domo(domoRepository, id);
        return Pagina.de(novedadRepository.listarPorDomo(id), pagina);
    }

    public List<Novedad> conflictosDeCanal() {
        return novedadRepository.listarPorTipo(TipoNovedad.CONFLICTO_CANAL);
    }

    /** 7.3: no puede registrarse un bloqueo sobre noches que ya tengan reservas activas. */
    public Bloqueo registrarBloqueo(IdentificacionDomo id, LocalDate desde, LocalDate hasta,
                                    MotivoBloqueo motivo, String observacion) {
        Buscar.domo(domoRepository, id);
        Periodo periodo = new Periodo(desde, hasta);
        boolean chocaConReserva = reservaRepository.buscarActivasPorDomo(id, periodo).stream()
                .filter(Reserva::estaActiva)
                .anyMatch(reserva -> reserva.getEstancia().nochesOcupadas().stream().anyMatch(periodo::contiene));
        if (chocaConReserva) {
            throw new ReglaDominioException("No se puede bloquear: el domo tiene reservas activas en esas noches.");
        }
        Bloqueo bloqueo = new Bloqueo(new IdBloqueo(UUID.randomUUID().toString()), id, periodo, motivo, observacion);
        bloqueoRepository.guardar(bloqueo);
        return bloqueo;
    }

    public List<Bloqueo> bloqueos(IdentificacionDomo id) {
        Buscar.domo(domoRepository, id);
        return bloqueoRepository.listarPorDomo(id);
    }
}
