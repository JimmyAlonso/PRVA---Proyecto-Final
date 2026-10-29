package co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity;

import co.edu.uniquindio.sga.infrastructure.persistence.jpa.embeddable.AutorizacionCierreEmbeddable;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.embeddable.CargoEmbeddable;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.embeddable.PagoEmbeddable;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;

/** Tabla de folios (F-08): un folio por reserva, con cargos y pagos que nunca se editan (RN-16). */
@Entity
@Table(name = "folio")
public class FolioEntity {

    @Id
    @Column(name = "codigo", length = 30)
    private String codigo;

    @Column(name = "codigo_reserva", nullable = false, unique = true, length = 20)
    private String codigoReserva;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "folio_cargo", joinColumns = @JoinColumn(name = "codigo_folio"))
    @OrderColumn(name = "posicion")
    private List<CargoEmbeddable> cargos = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "folio_pago", joinColumns = @JoinColumn(name = "codigo_folio"))
    @OrderColumn(name = "posicion")
    private List<PagoEmbeddable> pagos = new ArrayList<>();

    @Column(name = "cerrado", nullable = false)
    private boolean cerrado;

    @Embedded
    private AutorizacionCierreEmbeddable autorizacionCierre;

    public FolioEntity() {
        // requerido por JPA
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getCodigoReserva() {
        return codigoReserva;
    }

    public void setCodigoReserva(String codigoReserva) {
        this.codigoReserva = codigoReserva;
    }

    public List<CargoEmbeddable> getCargos() {
        return cargos;
    }

    public void setCargos(List<CargoEmbeddable> cargos) {
        this.cargos = cargos;
    }

    public List<PagoEmbeddable> getPagos() {
        return pagos;
    }

    public void setPagos(List<PagoEmbeddable> pagos) {
        this.pagos = pagos;
    }

    public boolean isCerrado() {
        return cerrado;
    }

    public void setCerrado(boolean cerrado) {
        this.cerrado = cerrado;
    }

    public AutorizacionCierreEmbeddable getAutorizacionCierre() {
        return autorizacionCierre;
    }

    public void setAutorizacionCierre(AutorizacionCierreEmbeddable autorizacionCierre) {
        this.autorizacionCierre = autorizacionCierre;
    }
}
