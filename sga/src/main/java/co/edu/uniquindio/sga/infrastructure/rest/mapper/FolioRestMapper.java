package co.edu.uniquindio.sga.infrastructure.rest.mapper;

import co.edu.uniquindio.sga.application.dto.response.FolioResponse;
import co.edu.uniquindio.sga.domain.entity.Folio;
import co.edu.uniquindio.sga.domain.entity.Pago;
import co.edu.uniquindio.sga.domain.valueobject.Cargo;
import co.edu.uniquindio.sga.domain.valueobject.Dinero;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/** 7.7: folio con su detalle completo (MapStruct). El saldo lo calcula el dominio (RN-15). */
@Mapper(componentModel = "spring")
public interface FolioRestMapper {

    @Mapping(target = "valor", source = "valor")
    @Mapping(target = "esInverso", source = "esInverso")
    FolioResponse.Movimiento aMovimiento(Cargo cargo);

    List<FolioResponse.Movimiento> aMovimientos(List<Cargo> cargos);

    @Mapping(target = "idPago", source = "id.valor")
    FolioResponse.PagoResponse aPago(Pago pago);

    List<FolioResponse.PagoResponse> aPagos(List<Pago> pagos);

    default FolioResponse aFolio(Folio folio) {
        return new FolioResponse(
                folio.getCodigo().valor(),
                folio.getReserva().valor(),
                aMovimientos(folio.getCargos()),
                aPagos(folio.getPagos()),
                aPesos(folio.totalCargos()),
                aPesos(folio.totalPagos()),
                folio.saldo().enPesos(),
                folio.tieneDepositoRegistrado(),
                folio.isCerrado(),
                folio.getAutorizacionCierre() == null ? null : folio.getAutorizacionCierre().autor(),
                folio.getAutorizacionCierre() == null ? null : folio.getAutorizacionCierre().motivo());
    }

    default long aPesos(Dinero dinero) {
        return dinero == null ? 0 : dinero.enPesos();
    }
}
