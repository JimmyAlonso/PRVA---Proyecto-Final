package co.edu.uniquindio.sga.infrastructure.persistence.jpa.repository;

import co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity.FolioEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FolioSpringDataRepository extends JpaRepository<FolioEntity, String> {

    Optional<FolioEntity> findByCodigoReserva(String codigoReserva);
}
