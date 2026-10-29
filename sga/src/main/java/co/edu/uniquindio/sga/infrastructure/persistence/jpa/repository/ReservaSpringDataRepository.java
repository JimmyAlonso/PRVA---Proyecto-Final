package co.edu.uniquindio.sga.infrastructure.persistence.jpa.repository;

import co.edu.uniquindio.sga.domain.valueobject.CanalOrigen;
import co.edu.uniquindio.sga.domain.valueobject.EstadoReserva;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity.ReservaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ReservaSpringDataRepository extends JpaRepository<ReservaEntity, String> {

    String CONDICIONES = """
            where (:estado is null or r.estado = :estado)
              and (:domo is null or r.domo = :domo)
              and (:canal is null or r.canalOrigen = :canal)
              and (:documento is null or r.titular.documento = :documento)
              and (:desde is null or r.estancia.fechaSalida > :desde)
              and (:hasta is null or r.estancia.fechaEntrada <= :hasta)
            """;

    String FILTROS = "select r from ReservaEntity r " + CONDICIONES;

    String CONTEO = "select count(r) from ReservaEntity r " + CONDICIONES;

    /** 7.5 Listados: filtros por fechas, estado, domo, canal y titular, paginados en la consulta (12.4). */
    @Query(value = FILTROS, countQuery = CONTEO)
    Page<ReservaEntity> buscar(@Param("estado") EstadoReserva estado,
                               @Param("domo") String domo,
                               @Param("canal") CanalOrigen canal,
                               @Param("documento") String documento,
                               @Param("desde") LocalDate desde,
                               @Param("hasta") LocalDate hasta,
                               Pageable pageable);

    @Query(FILTROS)
    List<ReservaEntity> buscar(@Param("estado") EstadoReserva estado,
                               @Param("domo") String domo,
                               @Param("canal") CanalOrigen canal,
                               @Param("documento") String documento,
                               @Param("desde") LocalDate desde,
                               @Param("hasta") LocalDate hasta,
                               Sort sort);

    /** RN-01: reservas activas del domo cuyas fechas tocan el periodo. */
    @Query("""
            select r from ReservaEntity r
            where r.domo = :domo
              and r.estado in :estados
              and r.estancia.fechaSalida >= :desde
              and r.estancia.fechaEntrada <= :hasta
            """)
    List<ReservaEntity> buscarActivasPorDomo(@Param("domo") String domo,
                                             @Param("estados") Collection<EstadoReserva> estados,
                                             @Param("desde") LocalDate desde,
                                             @Param("hasta") LocalDate hasta);

    List<ReservaEntity> findByDomoAndEstadoIn(String domo, Collection<EstadoReserva> estados);

    List<ReservaEntity> findByEstadoOrderByMomentoCreacionDescCodigoDesc(EstadoReserva estado);

    @Query("""
            select count(r) from ReservaEntity r
            where r.titular.documento = :documento and r.estado in :estados
            """)
    long contarDeTitularEnEstados(@Param("documento") String documento,
                                      @Param("estados") Collection<EstadoReserva> estados);

    /** RN-19: canal + identificador externo. */
    @Query("""
            select r from ReservaEntity r
            where r.identificadorExterno.canal = :canal and r.identificadorExterno.valor = :valor
            """)
    Optional<ReservaEntity> buscarPorIdentificadorExterno(@Param("canal") String canal, @Param("valor") String valor);

    /** Último código emitido con un prefijo (RES-AAAA-), para generar el siguiente consecutivo. */
    @Query("select max(r.codigo) from ReservaEntity r where r.codigo like concat(:prefijo, '%')")
    Optional<String> ultimoCodigoConPrefijo(@Param("prefijo") String prefijo);
}
