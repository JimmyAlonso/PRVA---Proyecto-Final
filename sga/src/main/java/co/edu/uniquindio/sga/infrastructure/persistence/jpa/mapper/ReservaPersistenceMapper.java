package co.edu.uniquindio.sga.infrastructure.persistence.jpa.mapper;

import co.edu.uniquindio.sga.domain.entity.Ocupante;
import co.edu.uniquindio.sga.domain.entity.Reserva;
import co.edu.uniquindio.sga.domain.entity.TitularDeLaReserva;
import co.edu.uniquindio.sga.domain.valueobject.CodigoReserva;
import co.edu.uniquindio.sga.domain.valueobject.DetalleNoche;
import co.edu.uniquindio.sga.domain.valueobject.Dinero;
import co.edu.uniquindio.sga.domain.valueobject.DocumentoIdentidad;
import co.edu.uniquindio.sga.domain.valueobject.Estancia;
import co.edu.uniquindio.sga.domain.valueobject.HoraEstimadaLlegada;
import co.edu.uniquindio.sga.domain.valueobject.IdTemporada;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionDomo;
import co.edu.uniquindio.sga.domain.valueobject.IdentificadorExterno;
import co.edu.uniquindio.sga.domain.valueobject.VersionPolitica;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.embeddable.DetalleNocheEmbeddable;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.embeddable.EstanciaEmbeddable;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.embeddable.IdentificadorExternoEmbeddable;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.embeddable.OcupanteEmbeddable;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.embeddable.TitularEmbeddable;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.embeddable.VersionPoliticaEmbeddable;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity.ReservaEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

/**
 * Traduce el agregado Reserva a su tabla y de vuelta. La reconstrucción usa
 * {@link Reserva#reconstruir}: persistir no le agrega al dominio setters ni constructor vacío.
 */
@Component
public class ReservaPersistenceMapper {

    public ReservaEntity aEntidad(Reserva reserva) {
        ReservaEntity entidad = new ReservaEntity();
        entidad.setCodigo(reserva.getCodigo().valor());
        entidad.setDomo(reserva.getDomo().valor());
        entidad.setEstancia(new EstanciaEmbeddable(reserva.getEstancia().fechaEntrada(),
                reserva.getEstancia().fechaSalida()));
        entidad.setEstado(reserva.getEstado());
        entidad.setCanalOrigen(reserva.getCanalOrigen());
        entidad.setIdentificadorExterno(reserva.getIdentificadorExterno()
                .map(id -> new IdentificadorExternoEmbeddable(id.canal(), id.valor()))
                .orElse(null));
        TitularDeLaReserva titular = reserva.getTitular();
        entidad.setTitular(new TitularEmbeddable(titular.getDocumento().valor(), titular.getNombreCompleto(),
                titular.getCorreo(), titular.getTelefono()));
        entidad.setOcupantes(new ArrayList<>(reserva.getOcupantes().stream()
                .map(o -> new OcupanteEmbeddable(o.getDocumento().valor(), o.getNombreCompleto(), o.getFechaNacimiento()))
                .toList()));
        entidad.setHoraEstimadaLlegada(reserva.getHoraEstimadaLlegada() == null ? null
                : reserva.getHoraEstimadaLlegada().hora());
        entidad.setValorCongelado(reserva.getValorCongelado().valor());
        entidad.setDesgloseCongelado(new ArrayList<>(reserva.getDesgloseCongelado().stream()
                .map(ReservaPersistenceMapper::aEmbeddable)
                .toList()));
        entidad.setPoliticaCongelada(new VersionPoliticaEmbeddable(reserva.getPoliticaCongelada().numero(),
                reserva.getPoliticaCongelada().vigenteDesde()));
        entidad.setMomentoCreacion(reserva.getMomentoCreacion());
        entidad.setMotivoCancelacion(reserva.getMotivoCancelacion());
        return entidad;
    }

    public Reserva aDominio(ReservaEntity entidad) {
        TitularEmbeddable t = entidad.getTitular();
        IdentificadorExternoEmbeddable externo = entidad.getIdentificadorExterno();
        return Reserva.reconstruir(
                new CodigoReserva(entidad.getCodigo()),
                new IdentificacionDomo(entidad.getDomo()),
                new Estancia(entidad.getEstancia().getFechaEntrada(), entidad.getEstancia().getFechaSalida()),
                entidad.getEstado(),
                entidad.getCanalOrigen(),
                externo == null || externo.getCanal() == null ? null
                        : new IdentificadorExterno(externo.getCanal(), externo.getValor()),
                new TitularDeLaReserva(new DocumentoIdentidad(t.getDocumento()), t.getNombreCompleto(),
                        t.getCorreo(), t.getTelefono()),
                entidad.getOcupantes().stream()
                        .map(o -> new Ocupante(new DocumentoIdentidad(o.getDocumento()), o.getNombreCompleto(),
                                o.getFechaNacimiento()))
                        .toList(),
                entidad.getHoraEstimadaLlegada() == null ? null : new HoraEstimadaLlegada(entidad.getHoraEstimadaLlegada()),
                new Dinero(entidad.getValorCongelado()),
                entidad.getDesgloseCongelado().stream().map(ReservaPersistenceMapper::aDetalle).toList(),
                new VersionPolitica(entidad.getPoliticaCongelada().getNumero(),
                        entidad.getPoliticaCongelada().getVigenteDesde()),
                entidad.getMomentoCreacion(),
                entidad.getMotivoCancelacion());
    }

    private static DetalleNocheEmbeddable aEmbeddable(DetalleNoche noche) {
        return new DetalleNocheEmbeddable(noche.fecha(), noche.temporada().valor(), noche.nombreTemporada(),
                noche.tarifaAplicada().valor(), noche.ocupantesFacturables(), noche.recargoPersonaAdicional().valor(),
                noche.subtotal().valor());
    }

    private static DetalleNoche aDetalle(DetalleNocheEmbeddable noche) {
        return new DetalleNoche(noche.getFecha(), new IdTemporada(noche.getTemporada()), noche.getNombreTemporada(),
                new Dinero(noche.getTarifaAplicada()), noche.getOcupantesFacturables(),
                new Dinero(noche.getRecargoPersonaAdicional()), new Dinero(noche.getSubtotal()));
    }
}
