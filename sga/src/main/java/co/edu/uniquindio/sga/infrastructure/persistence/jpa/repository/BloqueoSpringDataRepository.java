package co.edu.uniquindio.sga.infrastructure.persistence.jpa.repository;

import co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity.BloqueoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface BloqueoSpringDataRepository extends JpaRepository<BloqueoEntity, String> {

    /** RN-07: bloqueos vigentes del domo que se solapan con el periodo (ambos extremos incluidos). */
    @Query("""
            select b from BloqueoEntity b
            where b.domo = :domo
              and b.vigente = true
              and b.periodo.desde <= :hasta
              and b.periodo.hasta >= :desde
            """)
    List<BloqueoEntity> buscarVigentesQueSeSolapan(@Param("domo") String domo,
                                                   @Param("desde") LocalDate desde,
                                                   @Param("hasta") LocalDate hasta);

    @Query("select b from BloqueoEntity b where b.domo = :domo order by b.periodo.desde asc")
    List<BloqueoEntity> listarPorDomo(@Param("domo") String domo);
}
