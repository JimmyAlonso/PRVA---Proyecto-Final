package co.edu.uniquindio.sga.infrastructure.persistence.jpa.mapper;

import co.edu.uniquindio.sga.domain.entity.Bloqueo;
import co.edu.uniquindio.sga.domain.entity.Novedad;
import co.edu.uniquindio.sga.domain.entity.PoliticaCancelacion;
import co.edu.uniquindio.sga.domain.entity.Temporada;
import co.edu.uniquindio.sga.domain.valueobject.IdBloqueo;
import co.edu.uniquindio.sga.domain.valueobject.IdNovedad;
import co.edu.uniquindio.sga.domain.valueobject.IdTemporada;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionDomo;
import co.edu.uniquindio.sga.domain.valueobject.Periodo;
import co.edu.uniquindio.sga.domain.valueobject.Porcentaje;
import co.edu.uniquindio.sga.domain.valueobject.TramoRetencion;
import co.edu.uniquindio.sga.domain.valueobject.VersionPolitica;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.embeddable.PeriodoEmbeddable;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.embeddable.TramoRetencionEmbeddable;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity.BloqueoEntity;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity.NovedadEntity;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity.PoliticaCancelacionEntity;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity.TemporadaEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

/** Mappers de persistencia de los agregados pequeños: Temporada, Bloqueo, Novedad y Política de cancelación. */
@Component
public class CatalogoPersistenceMapper {

    // ------------------------------------------------------------------ Temporada

    public TemporadaEntity aEntidad(Temporada temporada) {
        TemporadaEntity entidad = new TemporadaEntity();
        entidad.setId(temporada.getId().valor());
        entidad.setNombre(temporada.getNombre());
        entidad.setEsBase(temporada.isEsBase());
        entidad.setPeriodos(new ArrayList<>(temporada.getPeriodos().stream()
                .map(p -> new PeriodoEmbeddable(p.desde(), p.hasta()))
                .toList()));
        return entidad;
    }

    public Temporada aDominio(TemporadaEntity entidad) {
        return new Temporada(new IdTemporada(entidad.getId()), entidad.getNombre(),
                entidad.getPeriodos().stream().map(p -> new Periodo(p.getDesde(), p.getHasta())).toList(),
                entidad.isEsBase());
    }

    // ------------------------------------------------------------------ Bloqueo

    public BloqueoEntity aEntidad(Bloqueo bloqueo) {
        BloqueoEntity entidad = new BloqueoEntity();
        entidad.setId(bloqueo.getId().valor());
        entidad.setDomo(bloqueo.getDomo().valor());
        entidad.setPeriodo(new PeriodoEmbeddable(bloqueo.getPeriodo().desde(), bloqueo.getPeriodo().hasta()));
        entidad.setMotivo(bloqueo.getMotivo());
        entidad.setObservacion(bloqueo.getObservacion());
        entidad.setVigente(bloqueo.isVigente());
        return entidad;
    }

    public Bloqueo aDominio(BloqueoEntity entidad) {
        return Bloqueo.reconstruir(new IdBloqueo(entidad.getId()), new IdentificacionDomo(entidad.getDomo()),
                new Periodo(entidad.getPeriodo().getDesde(), entidad.getPeriodo().getHasta()), entidad.getMotivo(),
                entidad.getObservacion(), entidad.isVigente());
    }

    // ------------------------------------------------------------------ Novedad

    public NovedadEntity aEntidad(Novedad novedad) {
        NovedadEntity entidad = new NovedadEntity();
        entidad.setId(novedad.getId().valor());
        entidad.setDomo(novedad.getDomo().valor());
        entidad.setTipo(novedad.getTipo());
        entidad.setDescripcion(novedad.getDescripcion());
        entidad.setGravedad(novedad.getGravedad());
        entidad.setAutor(novedad.getAutor());
        entidad.setMomentoReporte(novedad.getMomentoReporte());
        entidad.setAtendida(novedad.isAtendida());
        return entidad;
    }

    public Novedad aDominio(NovedadEntity entidad) {
        return Novedad.reconstruir(new IdNovedad(entidad.getId()), new IdentificacionDomo(entidad.getDomo()),
                entidad.getTipo(), entidad.getDescripcion(), entidad.getGravedad(), entidad.getAutor(),
                entidad.getMomentoReporte(), entidad.isAtendida());
    }

    // ------------------------------------------------------------------ Política de cancelación

    public PoliticaCancelacionEntity aEntidad(PoliticaCancelacion politica) {
        PoliticaCancelacionEntity entidad = new PoliticaCancelacionEntity();
        entidad.setNumeroVersion(politica.getVersion().numero());
        entidad.setVigenteDesde(politica.getVersion().vigenteDesde());
        entidad.setTramos(new ArrayList<>(politica.getTramos().stream()
                .map(t -> new TramoRetencionEmbeddable(t.horasAntelacionMinima(), t.retencion().valor()))
                .toList()));
        entidad.setRetencionNoShow(politica.getRetencionNoShow().valor());
        return entidad;
    }

    public PoliticaCancelacion aDominio(PoliticaCancelacionEntity entidad) {
        return new PoliticaCancelacion(new VersionPolitica(entidad.getNumeroVersion(), entidad.getVigenteDesde()),
                entidad.getTramos().stream()
                        .map(t -> new TramoRetencion(t.getHorasAntelacionMinima(), new Porcentaje(t.getPorcentajeRetencion())))
                        .toList(),
                new Porcentaje(entidad.getRetencionNoShow()));
    }
}
