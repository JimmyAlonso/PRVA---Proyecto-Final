package co.edu.uniquindio.sga.infraestructure.rest.mapper;

import co.edu.uniquindio.sga.application.dto.request.OcupanteRequest;
import co.edu.uniquindio.sga.application.dto.request.TitularRequest;
import co.edu.uniquindio.sga.application.dto.response.CotizacionDomoResponse;
import co.edu.uniquindio.sga.application.dto.response.DetalleNocheResponse;
import co.edu.uniquindio.sga.application.dto.response.OcupanteResponse;
import co.edu.uniquindio.sga.application.dto.response.ReservaDetalleResponse;
import co.edu.uniquindio.sga.application.dto.response.ReservaResumenResponse;
import co.edu.uniquindio.sga.application.dto.response.TitularResponse;
import co.edu.uniquindio.sga.domain.entity.Domo;
import co.edu.uniquindio.sga.domain.entity.Ocupante;
import co.edu.uniquindio.sga.domain.entity.Reserva;
import co.edu.uniquindio.sga.domain.entity.TitularDeLaReserva;
import co.edu.uniquindio.sga.domain.valueobject.Cotizacion;
import co.edu.uniquindio.sga.domain.valueobject.DetalleNoche;
import co.edu.uniquindio.sga.domain.valueobject.DocumentoIdentidad;
import co.edu.uniquindio.sga.domain.valueobject.HoraEstimadaLlegada;
import co.edu.uniquindio.sga.domain.valueobject.IdentificadorExterno;
import co.edu.uniquindio.sga.domain.valueobject.UmbralEdadFacturable;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Traducción DTO ↔ dominio en el adaptador REST (Guía 07: el caso de uso recibe tipos del
 * dominio). Escrito a mano; cuando llegue la Guía 08 puede reemplazarse por MapStruct.
 *
 * <p>Corrección: en el repositorio guía {@code aDetalle} no tenía cuerpo ni return y no compilaba.
 */
public class ReservaMapper {

    private static final String MONEDA = "COP";
    private final UmbralEdadFacturable umbral;

    public ReservaMapper(UmbralEdadFacturable umbral) {
        this.umbral = umbral;
    }

    // ------------------------------------------------------------ DTO → dominio

    public TitularDeLaReserva aTitular(TitularRequest request) {
        return new TitularDeLaReserva(new DocumentoIdentidad(request.documento()), request.nombre(),
                request.correo(), request.telefono());
    }

    public Ocupante aOcupante(OcupanteRequest request) {
        return new Ocupante(new DocumentoIdentidad(request.documento()), request.nombre(), request.fechaNacimiento());
    }

    /** 3.2: el titular es ocupante. Si no viene en la lista de acompañantes, se agrega al inicio. */
    public List<Ocupante> aOcupantes(TitularRequest titular, List<OcupanteRequest> acompanantes) {
        List<Ocupante> ocupantes = new ArrayList<>();
        Ocupante comoOcupante = new Ocupante(new DocumentoIdentidad(titular.documento()), titular.nombre(),
                titular.fechaNacimiento());
        ocupantes.add(comoOcupante);
        if (acompanantes != null) {
            acompanantes.stream()
                    .map(this::aOcupante)
                    .filter(ocupante -> !ocupante.equals(comoOcupante))
                    .forEach(ocupantes::add);
        }
        return ocupantes;
    }

    /** Ocupantes de una modificación: conserva al titular actual y reemplaza a los acompañantes. */
    public List<Ocupante> aOcupantesModificados(Reserva reserva, List<OcupanteRequest> acompanantes) {
        if (acompanantes == null) {
            return null;
        }
        List<Ocupante> ocupantes = new ArrayList<>();
        reserva.getOcupantes().stream()
                .filter(o -> o.getDocumento().equals(reserva.getTitular().getDocumento()))
                .findFirst()
                .ifPresent(ocupantes::add);
        acompanantes.stream().map(this::aOcupante).filter(o -> !ocupantes.contains(o)).forEach(ocupantes::add);
        return ocupantes;
    }

    /** Búsqueda y cotización: el grupo se describe por fechas de nacimiento (la edad se calcula). */
    public List<Ocupante> aGrupo(List<LocalDate> fechasNacimiento) {
        List<Ocupante> grupo = new ArrayList<>();
        for (int i = 0; i < fechasNacimiento.size(); i++) {
            grupo.add(new Ocupante(new DocumentoIdentidad("GRUPO" + (i + 1)), "Integrante " + (i + 1),
                    fechasNacimiento.get(i)));
        }
        return grupo;
    }

    public HoraEstimadaLlegada aHoraEstimadaLlegada(LocalTime hora) {
        return hora == null ? null : new HoraEstimadaLlegada(hora);
    }

    // ------------------------------------------------------------ dominio → DTO

    public ReservaDetalleResponse aDetalle(Reserva reserva) {
        TitularDeLaReserva titular = reserva.getTitular();
        return new ReservaDetalleResponse(
                reserva.getCodigo().valor(),
                reserva.getEstado(),
                reserva.getCanalOrigen(),
                reserva.getIdentificadorExterno().map(IdentificadorExterno::canal).orElse(null),
                reserva.getIdentificadorExterno().map(IdentificadorExterno::valor).orElse(null),
                reserva.getDomo().valor(),
                reserva.getEstancia().fechaEntrada(),
                reserva.getEstancia().fechaSalida(),
                reserva.getEstancia().noches(),
                reserva.getHoraEstimadaLlegada() == null ? null : reserva.getHoraEstimadaLlegada().hora(),
                new TitularResponse(titular.getDocumento().valor(), titular.getNombreCompleto(),
                        titular.getCorreo(), titular.getTelefono()),
                reserva.getOcupantes().stream().map(o -> aOcupanteResponse(o, reserva)).toList(),
                reserva.totalOcupantes(),
                reserva.getValorCongelado().enPesos(),
                MONEDA,
                reserva.getDesgloseCongelado().stream().map(this::aDetalleNoche).toList(),
                reserva.getPoliticaCongelada().numero(),
                reserva.getMomentoCreacion(),
                reserva.getMotivoCancelacion());
    }

    public ReservaResumenResponse aResumen(Reserva reserva) {
        return new ReservaResumenResponse(
                reserva.getCodigo().valor(),
                reserva.getEstado(),
                reserva.getCanalOrigen(),
                reserva.getDomo().valor(),
                reserva.getEstancia().fechaEntrada(),
                reserva.getEstancia().fechaSalida(),
                reserva.getTitular().getNombreCompleto(),
                reserva.totalOcupantes(),
                reserva.getValorCongelado().enPesos());
    }

    public List<ReservaResumenResponse> aResumenes(List<Reserva> reservas) {
        return reservas.stream().map(this::aResumen).toList();
    }

    public CotizacionDomoResponse aCotizacion(Domo domo, Cotizacion cotizacion) {
        return new CotizacionDomoResponse(
                domo.getIdentificacion().valor(),
                domo.getNombre(),
                domo.getCapacidad(),
                domo.tieneJacuzzi(),
                domo.getCaracteristicas(),
                cotizacion.noches().size(),
                cotizacion.totalRecargos().enPesos(),
                cotizacion.total().enPesos(),
                MONEDA,
                cotizacion.noches().stream().map(this::aDetalleNoche).toList());
    }

    private OcupanteResponse aOcupanteResponse(Ocupante ocupante, Reserva reserva) {
        return new OcupanteResponse(ocupante.getDocumento().valor(), ocupante.getNombreCompleto(),
                ocupante.getFechaNacimiento(), ocupante.esFacturableEn(reserva.getEstancia(), umbral));
    }

    private DetalleNocheResponse aDetalleNoche(DetalleNoche noche) {
        return new DetalleNocheResponse(noche.fecha(), noche.nombreTemporada(), noche.tarifaAplicada().enPesos(),
                noche.ocupantesFacturables(), noche.recargoPersonaAdicional().enPesos(), noche.subtotal().enPesos());
    }
}
