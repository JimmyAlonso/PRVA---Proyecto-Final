package co.edu.uniquindio.sga.infrastructure.persistence.jpa.mapper;

import co.edu.uniquindio.sga.domain.entity.Domo;
import co.edu.uniquindio.sga.domain.valueobject.Dinero;
import co.edu.uniquindio.sga.domain.valueobject.Fogata;
import co.edu.uniquindio.sga.domain.valueobject.IdTemporada;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionDomo;
import co.edu.uniquindio.sga.domain.valueobject.Jacuzzi;
import co.edu.uniquindio.sga.domain.valueobject.Malla;
import co.edu.uniquindio.sga.domain.valueobject.RegistroTarifa;
import co.edu.uniquindio.sga.domain.valueobject.Tarifa;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.embeddable.FogataEmbeddable;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.embeddable.JacuzziEmbeddable;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.embeddable.MallaEmbeddable;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.embeddable.RegistroTarifaEmbeddable;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.embeddable.TarifaEmbeddable;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity.DomoEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

/** Traduce el agregado Domo (con Jacuzzi, Malla, Fogata y tarifas como objetos de valor) a su tabla y de vuelta. */
@Component
public class DomoPersistenceMapper {

    public DomoEntity aEntidad(Domo domo) {
        DomoEntity entidad = new DomoEntity();
        entidad.setIdentificacion(domo.getIdentificacion().valor());
        entidad.setNombre(domo.getNombre());
        entidad.setDescripcion(domo.getDescripcion());
        entidad.setDormitorios(domo.getDormitorios());
        entidad.setCapacidad(domo.getCapacidad());
        entidad.setCaracteristicas(new ArrayList<>(domo.getCaracteristicas()));
        entidad.setJacuzzi(domo.getJacuzzi()
                .map(j -> new JacuzziEmbeddable(j.descripcion(), j.horasPrecalentamiento())).orElse(null));
        entidad.setMalla(domo.getMalla().map(m -> new MallaEmbeddable(m.pesoMaximoKg())).orElse(null));
        entidad.setFogata(domo.getFogata().map(f -> new FogataEmbeddable(f.kitsLena())).orElse(null));
        entidad.setEstadoOperativo(domo.getEstadoOperativo());
        entidad.setActivo(domo.estaActivo());
        entidad.setEliminado(domo.isEliminado());
        entidad.setTarifas(new ArrayList<>(domo.getTarifas().stream()
                .map(t -> new TarifaEmbeddable(t.temporada().valor(), t.valorPorOcupanteNoche().valor()))
                .toList()));
        entidad.setHistorialTarifas(new ArrayList<>(domo.getHistorialTarifas().stream()
                .map(r -> new RegistroTarifaEmbeddable(r.temporada().valor(), r.valorPorOcupanteNoche().valor(),
                        r.reemplazadaEl()))
                .toList()));
        return entidad;
    }

    public Domo aDominio(DomoEntity entidad) {
        JacuzziEmbeddable jacuzzi = entidad.getJacuzzi();
        MallaEmbeddable malla = entidad.getMalla();
        FogataEmbeddable fogata = entidad.getFogata();
        return Domo.reconstruir(
                new IdentificacionDomo(entidad.getIdentificacion()),
                entidad.getNombre(),
                entidad.getDescripcion(),
                entidad.getDormitorios(),
                entidad.getCapacidad(),
                entidad.getCaracteristicas() == null ? null : new ArrayList<>(entidad.getCaracteristicas()),
                jacuzzi == null || jacuzzi.getHorasPrecalentamiento() == null ? null
                        : new Jacuzzi(jacuzzi.getDescripcion(), jacuzzi.getHorasPrecalentamiento()),
                malla == null || malla.getPesoMaximoKg() == null ? null : new Malla(malla.getPesoMaximoKg()),
                fogata == null || fogata.getKitsLena() == null ? null : new Fogata(fogata.getKitsLena()),
                entidad.getEstadoOperativo(),
                entidad.isActivo(),
                entidad.isEliminado(),
                entidad.getTarifas().stream()
                        .map(t -> new Tarifa(new IdTemporada(t.getTemporada()), new Dinero(t.getValorPorOcupanteNoche())))
                        .toList(),
                entidad.getHistorialTarifas().stream()
                        .map(r -> new RegistroTarifa(new IdTemporada(r.getTemporada()),
                                new Dinero(r.getValorPorOcupanteNoche()), r.getReemplazadaEl()))
                        .toList());
    }
}
