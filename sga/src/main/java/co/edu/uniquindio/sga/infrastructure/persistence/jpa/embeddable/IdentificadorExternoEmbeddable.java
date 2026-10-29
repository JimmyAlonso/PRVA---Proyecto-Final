package co.edu.uniquindio.sga.infrastructure.persistence.jpa.embeddable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

/** RN-19: canal + identificador externo, únicos en la tabla de reservas. */
@Embeddable
public class IdentificadorExternoEmbeddable {

    @Column(name = "canal_externo", length = 40)
    private String canal;

    @Column(name = "id_externo", length = 80)
    private String valor;

    protected IdentificadorExternoEmbeddable() {
        // requerido por JPA
    }

    public IdentificadorExternoEmbeddable(String canal, String valor) {
        this.canal = canal;
        this.valor = valor;
    }

    public String getCanal() {
        return canal;
    }

    public String getValor() {
        return valor;
    }
}
