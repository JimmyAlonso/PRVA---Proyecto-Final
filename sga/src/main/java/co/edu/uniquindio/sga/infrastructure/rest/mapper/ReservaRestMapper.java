package co.edu.uniquindio.sga.infrastructure.rest.mapper;

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
import co.edu.uniquindio.sga.domain.valueobject.Dinero;
import co.edu.uniquindio.sga.domain.valueobject.DocumentoIdentidad;
import co.edu.uniquindio.sga.domain.valueobject.HoraEstimadaLlegada;
import co.edu.uniquindio.sga.domain.valueobject.IdentificadorExterno;
import co.edu.uniquindio.sga.domain.valueobject.UmbralEdadFacturable;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * DTO ↔ dominio de reservas y cotizaciones (Guía 08, MapStruct). El mapper no tiene lógica de
 * negocio: el único dato derivado que expone (si un ocupante es facturable) lo calcula el
 * dominio con el umbral configurado.
 */
@Mapper(componentModel = "spring")
public abstract class ReservaRestMapper {

    private static final String MONEDA = "COP";

    @Autowired
    protected UmbralEdadFacturable umbral;

    // ------------------------------------------------------------ DTO → dominio

    @Mapping(target = "nombreCompleto", source = "nombre")
    public abstract Ocupante aOcupante(OcupanteRequest request);

    public TitularDeLaReserva aTitular(TitularRequest request) {
        return new TitularDeLaReserva(aDocumento(request.documento()), request.nombre(), request.correo(),
                request.telefono());
    }

    /** 3.2: el titular es ocupante. Si no viene en la lista de acompañantes, se agrega al inicio. */
    public List<Ocupante> aOcupantes(TitularRequest titular, List<OcupanteRequest> acompanantes) {
        List<Ocupante> ocupantes = new ArrayList<>();
        Ocupante comoOcupante = new Ocupante(aDocumento(titular.documento()), titular.nombre(), titular.fechaNacimiento());
        ocupantes.add(comoOcupante);
        if (acompanantes != null) {
            acompanantes.stream()
                    .map(this::aOcupante)
                    .filter(ocupante -> !ocupante.equals(comoOcupante))
                    .forEach(ocupantes::add);
        }
        return ocupantes;
    }

    /** Búsqueda y cotización: el grupo se describe por fechas de nacimiento (la edad se calcula, 3.2). */
    public List<Ocupante> aGrupo(List<LocalDate> fechasNacimiento) {
        List<Ocupante> grupo = new ArrayList<>();
        for (int i = 0; i < fechasNacimiento.size(); i++) {
            grupo.add(new Ocupante(aDocumento("GRUPO" + (i + 1)), "Integrante " + (i + 1), fechasNacimiento.get(i)));
        }
        return grupo;
    }

    /**
     * Búsqueda: el grupo se describe por fechas de nacimiento o, si no llegan, por el número de
     * ocupantes (todos adultos). Que falten ambos es un error de forma de la solicitud (400).
     */
    public List<Ocupante> aGrupo(List<LocalDate> fechasNacimiento, Integer numeroOcupantes, LocalDate hoy) {
        if (fechasNacimiento != null && !fechasNacimiento.isEmpty()) {
            return aGrupo(fechasNacimiento);
        }
        if (numeroOcupantes == null || numeroOcupantes < 1) {
            throw new IllegalArgumentException("Indique las fechas de nacimiento del grupo o el número de ocupantes.");
        }
        return aGrupo(Collections.nCopies(numeroOcupantes, hoy.minusYears(30)));
    }

    public HoraEstimadaLlegada aHoraEstimadaLlegada(LocalTime hora) {
        return hora == null ? null : new HoraEstimadaLlegada(hora);
    }

    public DocumentoIdentidad aDocumento(String documento) {
        return documento == null ? null : new DocumentoIdentidad(documento);
    }

    // ------------------------------------------------------------ dominio → DTO

    @Mapping(target = "codigo", source = "codigo.valor")
    @Mapping(target = "idDomo", source = "domo.valor")
    @Mapping(target = "fechaEntrada", source = "estancia.fechaEntrada")
    @Mapping(target = "fechaSalida", source = "estancia.fechaSalida")
    @Mapping(target = "nombreTitular", source = "titular.nombreCompleto")
    @Mapping(target = "totalOcupantes", expression = "java(reserva.totalOcupantes())")
    @Mapping(target = "valorTotal", source = "valorCongelado")
    public abstract ReservaResumenResponse aResumen(Reserva reserva);

    public abstract List<ReservaResumenResponse> aResumenes(List<Reserva> reservas);

    @Mapping(target = "temporada", source = "nombreTemporada")
    @Mapping(target = "tarifaPorOcupanteFacturable", source = "tarifaAplicada")
    public abstract DetalleNocheResponse aDetalleNoche(DetalleNoche noche);

    public abstract List<DetalleNocheResponse> aDetallesNoche(List<DetalleNoche> noches);

    @Mapping(target = "documento", source = "documento.valor")
    @Mapping(target = "nombre", source = "nombreCompleto")
    public abstract TitularResponse aTitularResponse(TitularDeLaReserva titular);

    public ReservaDetalleResponse aDetalle(Reserva reserva) {
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
                aTitularResponse(reserva.getTitular()),
                reserva.getOcupantes().stream().map(o -> aOcupanteResponse(o, reserva)).toList(),
                reserva.totalOcupantes(),
                aPesos(reserva.getValorCongelado()),
                MONEDA,
                aDetallesNoche(reserva.getDesgloseCongelado()),
                reserva.getPoliticaCongelada().numero(),
                reserva.getMomentoCreacion(),
                reserva.getMotivoCancelacion());
    }

    public CotizacionDomoResponse aCotizacion(Domo domo, Cotizacion cotizacion) {
        return new CotizacionDomoResponse(
                domo.getIdentificacion().valor(),
                domo.getNombre(),
                domo.getCapacidad(),
                domo.tieneJacuzzi(),
                domo.getCaracteristicas(),
                cotizacion.noches().size(),
                aPesos(cotizacion.totalRecargos()),
                aPesos(cotizacion.total()),
                MONEDA,
                aDetallesNoche(cotizacion.noches()));
    }

    /** RN-06: el dominio decide si el ocupante es facturable a la fecha de entrada. */
    protected OcupanteResponse aOcupanteResponse(Ocupante ocupante, Reserva reserva) {
        return new OcupanteResponse(ocupante.getDocumento().valor(), ocupante.getNombreCompleto(),
                ocupante.getFechaNacimiento(), ocupante.esFacturableEn(reserva.getEstancia(), umbral));
    }

    /** 3.4: pesos colombianos sin decimales. */
    public long aPesos(Dinero dinero) {
        return dinero == null ? 0 : dinero.enPesos();
    }
}
