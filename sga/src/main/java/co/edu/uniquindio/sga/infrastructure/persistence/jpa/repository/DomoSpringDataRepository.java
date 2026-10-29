package co.edu.uniquindio.sga.infrastructure.persistence.jpa.repository;

import co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity.DomoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DomoSpringDataRepository extends JpaRepository<DomoEntity, String> {

    /** 7.3: los listados ignoran los domos eliminados (eliminación lógica). */
    List<DomoEntity> findByEliminadoFalseAndActivoTrueOrderByIdentificacionAsc();

    List<DomoEntity> findByEliminadoFalseOrderByIdentificacionAsc();

    List<DomoEntity> findAllByOrderByIdentificacionAsc();
}
