package co.edu.uniquindio.sga.infrastructure.persistence.jpa.repository;

import co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity.TemporadaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TemporadaSpringDataRepository extends JpaRepository<TemporadaEntity, String> {

    List<TemporadaEntity> findAllByOrderByIdAsc();
}
