package co.edu.uniquindio.sga.infrastructure.persistence.jpa.embeddable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.time.LocalDate;

/** RN-13 y RN-22: versión de la política congelada en la reserva. */
@Embeddable
public class VersionPoliticaEmbeddable {

    @Column(name = "politica_numero")
    private Integer numero;

    @Column(name = "politica_vigente_desde")
    private LocalDate vigenteDesde;

    protected VersionPoliticaEmbeddable() {
        // requerido por JPA
    }

    public VersionPoliticaEmbeddable(Integer numero, LocalDate vigenteDesde) {
        this.numero = numero;
        this.vigenteDesde = vigenteDesde;
    }

    public Integer getNumero() {
        return numero;
    }

    public LocalDate getVigenteDesde() {
        return vigenteDesde;
    }
}
