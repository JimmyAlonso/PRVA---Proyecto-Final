package co.edu.uniquindio.sga.infrastructure.persistence.jpa.repository;

import co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity.PoliticaCancelacionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PoliticaCancelacionSpringDataRepository extends JpaRepository<PoliticaCancelacionEntity, Integer> {

    /** RN-22: la versión más reciente que ya rige en la fecha. */
    Optional<PoliticaCancelacionEntity> findFirstByVigenteDesdeLessThanEqualOrderByVigenteDesdeDescNumeroVersionDesc(
            LocalDate fecha);

    Optional<PoliticaCancelacionEntity> findFirstByOrderByNumeroVersionDesc();

    List<PoliticaCancelacionEntity> findAllByOrderByNumeroVersionDesc();
}
