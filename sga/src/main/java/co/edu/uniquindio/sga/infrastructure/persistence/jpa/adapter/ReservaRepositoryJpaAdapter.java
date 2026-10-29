package co.edu.uniquindio.sga.infrastructure.persistence.jpa.adapter;

import co.edu.uniquindio.sga.domain.entity.Reserva;
import co.edu.uniquindio.sga.domain.repository.CriteriosBusquedaReserva;
import co.edu.uniquindio.sga.domain.repository.Paginacion;
import co.edu.uniquindio.sga.domain.repository.ReservaRepository;
import co.edu.uniquindio.sga.domain.repository.ResultadoPaginado;
import co.edu.uniquindio.sga.domain.valueobject.CodigoReserva;
import co.edu.uniquindio.sga.domain.valueobject.DocumentoIdentidad;
import co.edu.uniquindio.sga.domain.valueobject.EstadoReserva;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionDomo;
import co.edu.uniquindio.sga.domain.valueobject.IdentificadorExterno;
import co.edu.uniquindio.sga.domain.valueobject.Periodo;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity.ReservaEntity;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.mapper.ReservaPersistenceMapper;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.repository.ReservaSpringDataRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/** Adaptador JPA del puerto ReservaRepository (12.2: sustitución de adaptador, el dominio no cambia). */
@Repository
public class ReservaRepositoryJpaAdapter implements ReservaRepository {

    /** 7.5: de la más reciente a la más antigua. */
    private static final Sort MAS_RECIENTE_PRIMERO = Sort.by(Sort.Order.desc("momentoCreacion"), Sort.Order.desc("codigo"));

    private static final List<EstadoReserva> ESTADOS_ACTIVOS = Arrays.stream(EstadoReserva.values())
            .filter(EstadoReserva::esActiva)
            .toList();

    private final ReservaSpringDataRepository repositorio;
    private final ReservaPersistenceMapper mapper;

    public ReservaRepositoryJpaAdapter(ReservaSpringDataRepository repositorio, ReservaPersistenceMapper mapper) {
        this.repositorio = repositorio;
        this.mapper = mapper;
    }

    @Override
    public List<Reserva> buscarActivasPorDomo(IdentificacionDomo domo, Periodo periodo) {
        return aDominio(repositorio.buscarActivasPorDomo(domo.valor(), ESTADOS_ACTIVOS, periodo.desde(), periodo.hasta()));
    }

    @Override
    public List<Reserva> buscarActivasPorDomo(IdentificacionDomo domo) {
        return aDominio(repositorio.findByDomoAndEstadoIn(domo.valor(), ESTADOS_ACTIVOS));
    }

    @Override
    public Optional<Reserva> obtenerPorCodigo(CodigoReserva codigo) {
        return repositorio.findById(codigo.valor()).map(mapper::aDominio);
    }

    @Override
    public List<Reserva> buscarPorEstado(EstadoReserva estado) {
        return aDominio(repositorio.findByEstadoOrderByMomentoCreacionDescCodigoDesc(estado));
    }

    @Override
    public List<Reserva> buscar(CriteriosBusquedaReserva c) {
        return aDominio(repositorio.buscar(c.estado(), valor(c.domo()), c.canal(), valor(c.documentoTitular()),
                c.desde(), c.hasta(), MAS_RECIENTE_PRIMERO));
    }

    @Override
    public ResultadoPaginado<Reserva> buscar(CriteriosBusquedaReserva c, Paginacion paginacion) {
        Page<ReservaEntity> pagina = repositorio.buscar(c.estado(), valor(c.domo()), c.canal(),
                valor(c.documentoTitular()), c.desde(), c.hasta(),
                PageRequest.of(paginacion.pagina(), paginacion.tamano(), MAS_RECIENTE_PRIMERO));
        return new ResultadoPaginado<>(aDominio(pagina.getContent()), paginacion.pagina(), paginacion.tamano(),
                pagina.getTotalElements());
    }

    @Override
    public boolean existenActivasDeTitular(DocumentoIdentidad documentoTitular) {
        return repositorio.contarDeTitularEnEstados(documentoTitular.valor(), ESTADOS_ACTIVOS) > 0;
    }

    @Override
    public Optional<Reserva> buscarPorIdentificadorExterno(IdentificadorExterno identificador) {
        return repositorio.buscarPorIdentificadorExterno(identificador.canal(), identificador.valor())
                .map(mapper::aDominio);
    }

    @Override
    public void guardar(Reserva reserva) {
        repositorio.save(mapper.aEntidad(reserva));
    }

    private List<Reserva> aDominio(List<ReservaEntity> entidades) {
        return entidades.stream().map(mapper::aDominio).toList();
    }

    private static String valor(IdentificacionDomo domo) {
        return domo == null ? null : domo.valor();
    }

    private static String valor(DocumentoIdentidad documento) {
        return documento == null ? null : documento.valor();
    }
}
