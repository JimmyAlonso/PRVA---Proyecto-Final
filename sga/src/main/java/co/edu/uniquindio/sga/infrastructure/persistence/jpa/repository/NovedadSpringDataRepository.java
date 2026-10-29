package co.edu.uniquindio.sga.infrastructure.persistence.jpa.repository;

import co.edu.uniquindio.sga.domain.valueobject.TipoNovedad;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity.NovedadEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NovedadSpringDataRepository extends JpaRepository<NovedadEntity, String> {

    List<NovedadEntity> findByDomoOrderByMomentoReporteDesc(String domo);

    List<NovedadEntity> findByTipoOrderByMomentoReporteDesc(TipoNovedad tipo);
}
