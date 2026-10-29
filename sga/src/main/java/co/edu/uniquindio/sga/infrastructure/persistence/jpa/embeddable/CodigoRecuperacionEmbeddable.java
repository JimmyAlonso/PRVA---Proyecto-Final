package co.edu.uniquindio.sga.infrastructure.persistence.jpa.embeddable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.time.LocalDateTime;

/** 7.2: código de recuperación de contraseña, con vencimiento y de un solo uso. */
@Embeddable
public class CodigoRecuperacionEmbeddable {

    @Column(name = "recuperacion_codigo", length = 20)
    private String valor;

    @Column(name = "recuperacion_expira_en")
    private LocalDateTime expiraEn;

    @Column(name = "recuperacion_usado")
    private Boolean usado;

    protected CodigoRecuperacionEmbeddable() {
        // requerido por JPA
    }

    public CodigoRecuperacionEmbeddable(String valor, LocalDateTime expiraEn, Boolean usado) {
        this.valor = valor;
        this.expiraEn = expiraEn;
        this.usado = usado;
    }

    public String getValor() {
        return valor;
    }

    public LocalDateTime getExpiraEn() {
        return expiraEn;
    }

    public Boolean getUsado() {
        return usado;
    }
}
