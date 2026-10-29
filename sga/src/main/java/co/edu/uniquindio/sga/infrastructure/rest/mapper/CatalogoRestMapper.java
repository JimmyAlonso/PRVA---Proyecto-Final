package co.edu.uniquindio.sga.infrastructure.rest.mapper;

import co.edu.uniquindio.sga.application.dto.request.PeriodoRequest;
import co.edu.uniquindio.sga.application.dto.request.RegistrarTemporadaRequest;
import co.edu.uniquindio.sga.application.dto.request.ServicioAdicionalRequest;
import co.edu.uniquindio.sga.application.dto.request.TarifaDomoRequest;
import co.edu.uniquindio.sga.application.dto.request.TramoRetencionRequest;
import co.edu.uniquindio.sga.application.dto.response.OperacionResponses.AlojamientoResponse;
import co.edu.uniquindio.sga.application.dto.response.OperacionResponses.ServicioAdicionalResponse;
import co.edu.uniquindio.sga.application.dto.response.PeriodoResponse;
import co.edu.uniquindio.sga.application.dto.response.PoliticaCancelacionResponse;
import co.edu.uniquindio.sga.application.dto.response.TemporadaResponse;
import co.edu.uniquindio.sga.application.dto.response.TramoRetencionResponse;
import co.edu.uniquindio.sga.domain.entity.Alojamiento;
import co.edu.uniquindio.sga.domain.entity.PoliticaCancelacion;
import co.edu.uniquindio.sga.domain.entity.Temporada;
import co.edu.uniquindio.sga.domain.valueobject.Dinero;
import co.edu.uniquindio.sga.domain.valueobject.IdTemporada;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionDomo;
import co.edu.uniquindio.sga.domain.valueobject.ParametrosOperacion;
import co.edu.uniquindio.sga.domain.valueobject.Periodo;
import co.edu.uniquindio.sga.domain.valueobject.Porcentaje;
import co.edu.uniquindio.sga.domain.valueobject.ServicioAdicional;
import co.edu.uniquindio.sga.domain.valueobject.TramoRetencion;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** DTO ↔ dominio de temporadas, política de cancelación y alojamiento (MapStruct). */
@Mapper(componentModel = "spring")
public interface CatalogoRestMapper {

    // ------------------------------------------------------------ temporadas

    PeriodoResponse aPeriodoResponse(Periodo periodo);

    @Mapping(target = "id", source = "id.valor")
    @Mapping(target = "base", source = "esBase")
    TemporadaResponse aTemporada(Temporada temporada);

    List<TemporadaResponse> aTemporadas(List<Temporada> temporadas);

    default Temporada aTemporadaNueva(RegistrarTemporadaRequest request) {
        return new Temporada(new IdTemporada(request.id()), request.nombre(), aPeriodos(request.periodos()), false);
    }

    default Map<IdentificacionDomo, Dinero> aTarifasPorDomo(List<TarifaDomoRequest> tarifas) {
        Map<IdentificacionDomo, Dinero> resultado = new LinkedHashMap<>();
        if (tarifas != null) {
            tarifas.forEach(t -> resultado.put(new IdentificacionDomo(t.idDomo()), Dinero.de(t.valorPorOcupanteNoche())));
        }
        return resultado;
    }

    default Periodo aPeriodo(PeriodoRequest request) {
        return new Periodo(request.desde(), request.hasta());
    }

    default List<Periodo> aPeriodos(List<PeriodoRequest> periodos) {
        return periodos == null ? List.of() : periodos.stream().map(this::aPeriodo).toList();
    }

    // ------------------------------------------------------------ política de cancelación

    @Mapping(target = "porcentajeRetencion", source = "retencion.valor")
    TramoRetencionResponse aTramoResponse(TramoRetencion tramo);

    @Mapping(target = "version", source = "version.numero")
    @Mapping(target = "vigenteDesde", source = "version.vigenteDesde")
    @Mapping(target = "retencionNoShow", source = "retencionNoShow.valor")
    PoliticaCancelacionResponse aPolitica(PoliticaCancelacion politica);

    List<PoliticaCancelacionResponse> aPoliticas(List<PoliticaCancelacion> politicas);

    default List<TramoRetencion> aTramos(List<TramoRetencionRequest> tramos) {
        return tramos == null ? List.of() : tramos.stream()
                .map(t -> new TramoRetencion(t.horasAntelacionMinima(), new Porcentaje(t.porcentajeRetencion())))
                .toList();
    }

    // ------------------------------------------------------------ alojamiento

    @Mapping(target = "valor", source = "valor")
    ServicioAdicionalResponse aServicio(ServicioAdicional servicio);

    List<ServicioAdicionalResponse> aServicios(List<ServicioAdicional> servicios);

    default ServicioAdicional aServicioAdicional(String codigo, ServicioAdicionalRequest request) {
        return new ServicioAdicional(codigo, request.nombre(), Dinero.de(request.valor()), request.porPersona());
    }

    default Porcentaje aPorcentaje(BigDecimal valor) {
        return valor == null ? null : new Porcentaje(valor);
    }

    default AlojamientoResponse aAlojamiento(Alojamiento alojamiento) {
        ParametrosOperacion p = alojamiento.getParametros();
        return new AlojamientoResponse(
                alojamiento.getNombre(),
                alojamiento.getDescripcion(),
                alojamiento.getCiudad(),
                alojamiento.getDireccion(),
                alojamiento.getLatitud().toPlainString(),
                alojamiento.getLongitud().toPlainString(),
                alojamiento.getHorario().horaEntrada(),
                alojamiento.getHorario().horaSalida(),
                alojamiento.getNormasConvivencia(),
                p.umbralEdad().anios(),
                p.tiempoPreparacion().horas(),
                p.plazoConfirmacion().horas(),
                p.horaLimiteNoShow().hora(),
                p.anticipo().valor().longValue(),
                aPesos(p.deposito().monto()),
                alojamiento.isAceptaMascotas(),
                aServicios(alojamiento.getServicios()));
    }

    default long aPesos(Dinero dinero) {
        return dinero == null ? 0 : dinero.enPesos();
    }
}
