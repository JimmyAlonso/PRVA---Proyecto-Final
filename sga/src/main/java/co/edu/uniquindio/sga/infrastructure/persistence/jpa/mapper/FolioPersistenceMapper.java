package co.edu.uniquindio.sga.infrastructure.persistence.jpa.mapper;

import co.edu.uniquindio.sga.domain.entity.Folio;
import co.edu.uniquindio.sga.domain.entity.Pago;
import co.edu.uniquindio.sga.domain.valueobject.AutorizacionCierre;
import co.edu.uniquindio.sga.domain.valueobject.Cargo;
import co.edu.uniquindio.sga.domain.valueobject.CodigoFolio;
import co.edu.uniquindio.sga.domain.valueobject.CodigoReserva;
import co.edu.uniquindio.sga.domain.valueobject.ConceptoCargo;
import co.edu.uniquindio.sga.domain.valueobject.Dinero;
import co.edu.uniquindio.sga.domain.valueobject.IdPago;
import co.edu.uniquindio.sga.domain.valueobject.MedioPago;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.embeddable.AutorizacionCierreEmbeddable;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.embeddable.CargoEmbeddable;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.embeddable.PagoEmbeddable;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity.FolioEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

/** Traduce el agregado Folio a su tabla. Cargos y pagos conservan el orden en que se registraron (RN-16). */
@Component
public class FolioPersistenceMapper {

    public FolioEntity aEntidad(Folio folio) {
        FolioEntity entidad = new FolioEntity();
        entidad.setCodigo(folio.getCodigo().valor());
        entidad.setCodigoReserva(folio.getReserva().valor());
        entidad.setCargos(new ArrayList<>(folio.getCargos().stream()
                .map(c -> new CargoEmbeddable(c.concepto().name(), c.valor().valor(), c.fecha(), c.descripcion(),
                        c.esInverso()))
                .toList()));
        entidad.setPagos(new ArrayList<>(folio.getPagos().stream()
                .map(p -> new PagoEmbeddable(p.getId().valor(), p.getMonto().valor(), p.getMedio().name(), p.getFecha()))
                .toList()));
        entidad.setCerrado(folio.isCerrado());
        AutorizacionCierre autorizacion = folio.getAutorizacionCierre();
        entidad.setAutorizacionCierre(autorizacion == null ? null
                : new AutorizacionCierreEmbeddable(autorizacion.autor(), autorizacion.motivo()));
        return entidad;
    }

    public Folio aDominio(FolioEntity entidad) {
        AutorizacionCierreEmbeddable autorizacion = entidad.getAutorizacionCierre();
        return Folio.reconstruir(
                new CodigoFolio(entidad.getCodigo()),
                new CodigoReserva(entidad.getCodigoReserva()),
                entidad.getCargos().stream()
                        .map(c -> new Cargo(ConceptoCargo.valueOf(c.getConcepto()), new Dinero(c.getValor()), c.getFecha(),
                                c.getDescripcion(), Boolean.TRUE.equals(c.getInverso())))
                        .toList(),
                entidad.getPagos().stream()
                        .map(p -> new Pago(new IdPago(p.getIdPago()), new Dinero(p.getMonto()), MedioPago.valueOf(p.getMedio()),
                                p.getFecha()))
                        .toList(),
                entidad.isCerrado(),
                autorizacion == null || autorizacion.getAutor() == null ? null
                        : new AutorizacionCierre(autorizacion.getAutor(), autorizacion.getMotivo()));
    }
}
