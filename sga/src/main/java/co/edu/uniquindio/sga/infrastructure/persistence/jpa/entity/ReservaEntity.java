package co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity;

import co.edu.uniquindio.sga.domain.valueobject.CanalOrigen;
import co.edu.uniquindio.sga.domain.valueobject.EstadoReserva;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.embeddable.DetalleNocheEmbeddable;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.embeddable.EstanciaEmbeddable;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.embeddable.IdentificadorExternoEmbeddable;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.embeddable.OcupanteEmbeddable;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.embeddable.TitularEmbeddable;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.embeddable.VersionPoliticaEmbeddable;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Tabla de reservas. Es una clase de infraestructura, separada del agregado Reserva del
 * dominio: aquí sí hay constructor vacío y setters porque los exige JPA, y el dominio no los
 * necesita. La restricción única canal + identificador externo respalda RN-19 en la base.
 */
@Entity
@Table(name = "reserva",
        uniqueConstraints = @UniqueConstraint(name = "uk_reserva_canal_externo",
                columnNames = {"canal_externo", "id_externo"}),
        indexes = {
                @Index(name = "ix_reserva_domo", columnList = "domo"),
                @Index(name = "ix_reserva_estado", columnList = "estado"),
                @Index(name = "ix_reserva_titular", columnList = "titular_documento")
        })
public class ReservaEntity {

    @Id
    @Column(name = "codigo", length = 20)
    private String codigo;

    @Column(name = "domo", nullable = false, length = 20)
    private String domo;

    @Embedded
    private EstanciaEmbeddable estancia;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 15)
    private EstadoReserva estado;

    @Enumerated(EnumType.STRING)
    @Column(name = "canal_origen", nullable = false, length = 10)
    private CanalOrigen canalOrigen;

    @Embedded
    private IdentificadorExternoEmbeddable identificadorExterno;

    @Embedded
    private TitularEmbeddable titular;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "reserva_ocupante", joinColumns = @JoinColumn(name = "codigo_reserva"))
    @OrderColumn(name = "posicion")
    private List<OcupanteEmbeddable> ocupantes = new ArrayList<>();

    @Column(name = "hora_estimada_llegada")
    private LocalTime horaEstimadaLlegada;

    @Column(name = "valor_congelado", nullable = false, precision = 15, scale = 2)
    private BigDecimal valorCongelado;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "reserva_noche", joinColumns = @JoinColumn(name = "codigo_reserva"))
    @OrderColumn(name = "posicion")
    private List<DetalleNocheEmbeddable> desgloseCongelado = new ArrayList<>();

    @Embedded
    private VersionPoliticaEmbeddable politicaCongelada;

    @Column(name = "momento_creacion", nullable = false)
    private LocalDateTime momentoCreacion;

    @Column(name = "motivo_cancelacion", length = 300)
    private String motivoCancelacion;

    public ReservaEntity() {
        // requerido por JPA
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getDomo() {
        return domo;
    }

    public void setDomo(String domo) {
        this.domo = domo;
    }

    public EstanciaEmbeddable getEstancia() {
        return estancia;
    }

    public void setEstancia(EstanciaEmbeddable estancia) {
        this.estancia = estancia;
    }

    public EstadoReserva getEstado() {
        return estado;
    }

    public void setEstado(EstadoReserva estado) {
        this.estado = estado;
    }

    public CanalOrigen getCanalOrigen() {
        return canalOrigen;
    }

    public void setCanalOrigen(CanalOrigen canalOrigen) {
        this.canalOrigen = canalOrigen;
    }

    public IdentificadorExternoEmbeddable getIdentificadorExterno() {
        return identificadorExterno;
    }

    public void setIdentificadorExterno(IdentificadorExternoEmbeddable identificadorExterno) {
        this.identificadorExterno = identificadorExterno;
    }

    public TitularEmbeddable getTitular() {
        return titular;
    }

    public void setTitular(TitularEmbeddable titular) {
        this.titular = titular;
    }

    public List<OcupanteEmbeddable> getOcupantes() {
        return ocupantes;
    }

    public void setOcupantes(List<OcupanteEmbeddable> ocupantes) {
        this.ocupantes = ocupantes;
    }

    public LocalTime getHoraEstimadaLlegada() {
        return horaEstimadaLlegada;
    }

    public void setHoraEstimadaLlegada(LocalTime horaEstimadaLlegada) {
        this.horaEstimadaLlegada = horaEstimadaLlegada;
    }

    public BigDecimal getValorCongelado() {
        return valorCongelado;
    }

    public void setValorCongelado(BigDecimal valorCongelado) {
        this.valorCongelado = valorCongelado;
    }

    public List<DetalleNocheEmbeddable> getDesgloseCongelado() {
        return desgloseCongelado;
    }

    public void setDesgloseCongelado(List<DetalleNocheEmbeddable> desgloseCongelado) {
        this.desgloseCongelado = desgloseCongelado;
    }

    public VersionPoliticaEmbeddable getPoliticaCongelada() {
        return politicaCongelada;
    }

    public void setPoliticaCongelada(VersionPoliticaEmbeddable politicaCongelada) {
        this.politicaCongelada = politicaCongelada;
    }

    public LocalDateTime getMomentoCreacion() {
        return momentoCreacion;
    }

    public void setMomentoCreacion(LocalDateTime momentoCreacion) {
        this.momentoCreacion = momentoCreacion;
    }

    public String getMotivoCancelacion() {
        return motivoCancelacion;
    }

    public void setMotivoCancelacion(String motivoCancelacion) {
        this.motivoCancelacion = motivoCancelacion;
    }
}
