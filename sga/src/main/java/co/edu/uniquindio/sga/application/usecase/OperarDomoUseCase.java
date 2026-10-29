package co.edu.uniquindio.sga.application.usecase;

import co.edu.uniquindio.sga.application.dto.Pagina;
import co.edu.uniquindio.sga.domain.entity.Bloqueo;
import co.edu.uniquindio.sga.domain.entity.Domo;
import co.edu.uniquindio.sga.domain.entity.Novedad;
import co.edu.uniquindio.sga.domain.repository.BloqueoRepository;
import co.edu.uniquindio.sga.domain.repository.DomoRepository;
import co.edu.uniquindio.sga.domain.repository.NovedadRepository;
import co.edu.uniquindio.sga.domain.service.CalendarioOcupacionService;
import co.edu.uniquindio.sga.domain.valueobject.EstadoOperativo;
import co.edu.uniquindio.sga.domain.valueobject.GravedadNovedad;
import co.edu.uniquindio.sga.domain.valueobject.IdNovedad;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionDomo;
import co.edu.uniquindio.sga.domain.valueobject.NocheCalendario;
import co.edu.uniquindio.sga.domain.valueobject.Periodo;
import co.edu.uniquindio.sga.domain.valueobject.TipoNovedad;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Consulta del catálogo de domos y operación diaria (7.6) del personal de servicio y la
 * recepción. Cada transición del estado operativo es un método de negocio propio: no existe un
 * "cambiar estado" genérico, el agregado Domo decide si la transición es válida.
 */
@Service
@Transactional
public class OperarDomoUseCase {

    private final DomoRepository domoRepository;
    private final NovedadRepository novedadRepository;
    private final BloqueoRepository bloqueoRepository;
    private final CalendarioOcupacionService calendarioService;

    public OperarDomoUseCase(DomoRepository domoRepository, NovedadRepository novedadRepository,
                             BloqueoRepository bloqueoRepository, CalendarioOcupacionService calendarioService) {
        this.domoRepository = domoRepository;
        this.novedadRepository = novedadRepository;
        this.bloqueoRepository = bloqueoRepository;
        this.calendarioService = calendarioService;
    }

    // ------------------------------------------------------------------ catálogo

    /** 7.3: los listados ignoran los domos eliminados e inactivos. */
    @Transactional(readOnly = true)
    public Pagina<Domo> listar(int pagina) {
        return Pagina.de(domoRepository.listarActivos(), pagina);
    }

    @Transactional(readOnly = true)
    public Domo consultar(IdentificacionDomo id) {
        return Buscar.domo(domoRepository, id);
    }

    /** 7.5: calendario de ocupación por domo y rango de fechas (noches incluidas en ambos extremos). */
    @Transactional(readOnly = true)
    public List<NocheCalendario> calendario(IdentificacionDomo id, Periodo noches) {
        return calendarioService.calendario(Buscar.domo(domoRepository, id), noches);
    }

    // ------------------------------------------------------------------ estado operativo (7.6)

    /** 7.1 Personal de servicio: domos pendientes de preparación o en preparación. */
    @Transactional(readOnly = true)
    public Pagina<Domo> pendientesDePreparacion(int pagina) {
        return Pagina.de(domoRepository.listarNoEliminados().stream()
                .filter(d -> d.getEstadoOperativo() == EstadoOperativo.PENDIENTE_PREPARACION
                        || d.getEstadoOperativo() == EstadoOperativo.EN_PREPARACION)
                .toList(), pagina);
    }

    /** PENDIENTE_PREPARACION → EN_PREPARACION. */
    public Domo iniciarPreparacion(IdentificacionDomo id) {
        Domo domo = Buscar.domo(domoRepository, id);
        domo.iniciarPreparacion();
        domoRepository.guardar(domo);
        return domo;
    }

    /** EN_PREPARACION → PREPARADO. */
    public Domo marcarPreparado(IdentificacionDomo id) {
        Domo domo = Buscar.domo(domoRepository, id);
        domo.marcarPreparado();
        domoRepository.guardar(domo);
        return domo;
    }

    // ------------------------------------------------------------------ novedades (7.6)

    public Novedad registrarNovedad(IdentificacionDomo id, TipoNovedad tipo, String descripcion,
                                    GravedadNovedad gravedad, String autor, LocalDateTime momento) {
        Buscar.domo(domoRepository, id);
        Novedad novedad = new Novedad(new IdNovedad(UUID.randomUUID().toString()), id, tipo, descripcion,
                gravedad, autor, momento);
        novedadRepository.guardar(novedad);
        return novedad;
    }

    @Transactional(readOnly = true)
    public Pagina<Novedad> historialNovedades(IdentificacionDomo id, int pagina) {
        Buscar.domo(domoRepository, id);
        return Pagina.de(novedadRepository.listarPorDomo(id), pagina);
    }

    /** 7.8: conflictos de canal registrados para revisión del administrador (RN-18). */
    @Transactional(readOnly = true)
    public Pagina<Novedad> conflictosDeCanal(int pagina) {
        return Pagina.de(novedadRepository.listarPorTipo(TipoNovedad.CONFLICTO_CANAL), pagina);
    }

    // ------------------------------------------------------------------ bloqueos (consulta)

    @Transactional(readOnly = true)
    public Pagina<Bloqueo> bloqueos(IdentificacionDomo id, int pagina) {
        Buscar.domo(domoRepository, id);
        return Pagina.de(bloqueoRepository.listarPorDomo(id), pagina);
    }
}
