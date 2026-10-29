package co.edu.uniquindio.sga.infrastructure.rest.mapper;

import co.edu.uniquindio.sga.application.dto.request.JacuzziRequest;
import co.edu.uniquindio.sga.application.dto.request.RegistrarDomoRequest;
import co.edu.uniquindio.sga.application.dto.request.TarifaRequest;
import co.edu.uniquindio.sga.application.dto.response.DomoResponse;
import co.edu.uniquindio.sga.application.dto.response.NocheCalendarioResponse;
import co.edu.uniquindio.sga.application.dto.response.OperacionResponses.BloqueoResponse;
import co.edu.uniquindio.sga.application.dto.response.OperacionResponses.NovedadResponse;
import co.edu.uniquindio.sga.application.dto.response.RegistroTarifaResponse;
import co.edu.uniquindio.sga.domain.entity.Bloqueo;
import co.edu.uniquindio.sga.domain.entity.Domo;
import co.edu.uniquindio.sga.domain.entity.Novedad;
import co.edu.uniquindio.sga.domain.valueobject.Dinero;
import co.edu.uniquindio.sga.domain.valueobject.Fogata;
import co.edu.uniquindio.sga.domain.valueobject.IdTemporada;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionDomo;
import co.edu.uniquindio.sga.domain.valueobject.Jacuzzi;
import co.edu.uniquindio.sga.domain.valueobject.Malla;
import co.edu.uniquindio.sga.domain.valueobject.NocheCalendario;
import co.edu.uniquindio.sga.domain.valueobject.RegistroTarifa;
import co.edu.uniquindio.sga.domain.valueobject.Tarifa;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** DTO ↔ dominio de domos, tarifas, bloqueos, novedades y calendario (MapStruct, sin lógica de negocio). */
@Mapper(componentModel = "spring")
public interface DomoRestMapper {

    // ------------------------------------------------------------ dominio → DTO

    @Mapping(target = "id", source = "id.valor")
    @Mapping(target = "idDomo", source = "domo.valor")
    NovedadResponse aNovedad(Novedad novedad);

    @Mapping(target = "id", source = "id.valor")
    @Mapping(target = "idDomo", source = "domo.valor")
    @Mapping(target = "desde", source = "periodo.desde")
    @Mapping(target = "hasta", source = "periodo.hasta")
    BloqueoResponse aBloqueo(Bloqueo bloqueo);

    @Mapping(target = "temporada", source = "temporada.valor")
    RegistroTarifaResponse aRegistroTarifa(RegistroTarifa registro);

    List<RegistroTarifaResponse> aRegistrosTarifa(List<RegistroTarifa> registros);

    NocheCalendarioResponse aNoche(NocheCalendario noche);

    List<NocheCalendarioResponse> aCalendario(List<NocheCalendario> noches);

    default DomoResponse aDomo(Domo domo) {
        Map<String, Long> tarifas = new LinkedHashMap<>();
        domo.getTarifas().forEach(t -> tarifas.put(t.temporada().valor(), aPesos(t.valorPorOcupanteNoche())));
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

    // ------------------------------------------------------------ DTO → dominio

    default Domo aDomoNuevo(RegistrarDomoRequest request) {
        Domo domo = Domo.registrar(new IdentificacionDomo(request.identificacion()), request.nombre(),
                request.dormitorios(), request.capacidad());
        domo.describir(request.descripcion(), request.caracteristicas());
        domo.equiparJacuzzi(aJacuzzi(request.jacuzzi()));
        domo.equiparMalla(aMalla(request.mallaPesoMaximoKg()));
        domo.equiparFogata(aFogata(request.fogataKitsLena()));
        return domo;
    }

    default List<Tarifa> aTarifas(List<TarifaRequest> tarifas) {
        return tarifas == null ? List.of() : tarifas.stream()
                .map(t -> new Tarifa(new IdTemporada(t.temporada()), Dinero.de(t.valorPorOcupanteNoche())))
                .toList();
    }

    default Jacuzzi aJacuzzi(JacuzziRequest request) {
        return request == null ? null : new Jacuzzi(request.descripcion(), request.horasPrecalentamiento());
    }

    default Malla aMalla(Integer pesoMaximoKg) {
        return pesoMaximoKg == null ? null : new Malla(pesoMaximoKg);
    }

    default Fogata aFogata(Integer kitsLena) {
        return kitsLena == null ? null : new Fogata(kitsLena);
    }

    default long aPesos(Dinero dinero) {
        return dinero == null ? 0 : dinero.enPesos();
    }
}
