package co.edu.uniquindio.sga.infraestructure.rest.mapper;

import co.edu.uniquindio.sga.application.dto.response.DomoResponse;
import co.edu.uniquindio.sga.application.dto.response.FolioResponse;
import co.edu.uniquindio.sga.application.dto.response.OperacionResponses.AlojamientoResponse;
import co.edu.uniquindio.sga.application.dto.response.OperacionResponses.BloqueoResponse;
import co.edu.uniquindio.sga.application.dto.response.OperacionResponses.NovedadResponse;
import co.edu.uniquindio.sga.application.dto.response.OperacionResponses.ServicioAdicionalResponse;
import co.edu.uniquindio.sga.domain.entity.Alojamiento;
import co.edu.uniquindio.sga.domain.entity.Bloqueo;
import co.edu.uniquindio.sga.domain.entity.Domo;
import co.edu.uniquindio.sga.domain.entity.Folio;
import co.edu.uniquindio.sga.domain.entity.Novedad;
import co.edu.uniquindio.sga.domain.valueobject.Fogata;
import co.edu.uniquindio.sga.domain.valueobject.Jacuzzi;
import co.edu.uniquindio.sga.domain.valueobject.Malla;
import co.edu.uniquindio.sga.domain.valueobject.ParametrosOperacion;

import java.util.LinkedHashMap;
import java.util.Map;

/** Traducción dominio → DTO de domos, folios, novedades, bloqueos y alojamiento. */
public class OperacionMapper {

    public DomoResponse aDomo(Domo domo) {
        Map<String, Long> tarifas = new LinkedHashMap<>();
        domo.getTarifas().forEach(t -> tarifas.put(t.temporada().valor(), t.valorPorOcupanteNoche().enPesos()));
        return new DomoResponse(
                domo.getIdentificacion().valor(),
                domo.getNombre(),
                domo.getDescripcion(),
                domo.getDormitorios(),
                domo.getCapacidad(),
                domo.getCaracteristicas(),
                domo.tieneJacuzzi(),
                domo.getJacuzzi().map(Jacuzzi::horasPrecalentamiento).orElse(null),
                domo.getMalla().map(Malla::pesoMaximoKg).orElse(null),
                domo.getFogata().map(Fogata::kitsLena).orElse(null),
                domo.getEstadoOperativo(),
                domo.estaActivo(),
                tarifas);
    }

    public FolioResponse aFolio(Folio folio) {
        return new FolioResponse(
                folio.getCodigo().valor(),
                folio.getReserva().valor(),
                folio.getCargos().stream().map(c -> new FolioResponse.Movimiento(c.concepto().name(),
                        c.valor().enPesos(), c.esInverso(), c.fecha(), c.descripcion())).toList(),
                folio.getPagos().stream().map(p -> new FolioResponse.PagoResponse(p.getId().valor(),
                        p.getMonto().enPesos(), p.getMedio().name(), p.getFecha())).toList(),
                folio.totalCargos().enPesos(),
                folio.totalPagos().enPesos(),
                folio.saldo().enPesos(),
                folio.tieneDepositoRegistrado(),
                folio.isCerrado(),
                folio.getAutorizacionCierre() == null ? null : folio.getAutorizacionCierre().autor(),
                folio.getAutorizacionCierre() == null ? null : folio.getAutorizacionCierre().motivo());
    }

    public NovedadResponse aNovedad(Novedad novedad) {
        return new NovedadResponse(novedad.getId().valor(), novedad.getDomo().valor(), novedad.getTipo(),
                novedad.getDescripcion(), novedad.getGravedad(), novedad.getAutor(), novedad.getMomentoReporte(),
                novedad.isAtendida());
    }

    public BloqueoResponse aBloqueo(Bloqueo bloqueo) {
        return new BloqueoResponse(bloqueo.getId().valor(), bloqueo.getDomo().valor(), bloqueo.getPeriodo().desde(),
                bloqueo.getPeriodo().hasta(), bloqueo.getMotivo(), bloqueo.getObservacion(), bloqueo.isVigente());
    }

    public AlojamientoResponse aAlojamiento(Alojamiento alojamiento) {
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
                p.deposito().monto().enPesos(),
                alojamiento.isAceptaMascotas(),
                alojamiento.getServicios().stream().map(s -> new ServicioAdicionalResponse(s.codigo(), s.nombre(),
                        s.valor().enPesos(), s.porPersona())).toList());
    }
}
