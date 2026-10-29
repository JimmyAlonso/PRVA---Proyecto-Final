package co.edu.uniquindio.sga.infrastructure.persistence.jpa.embeddable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

/** Glosario propio: kits de leña de la fogata del domo. */
@Embeddable
public class FogataEmbeddable {

    @Column(name = "fogata_kits_lena")
    private Integer kitsLena;

    protected FogataEmbeddable() {
        // requerido por JPA
    }

    public FogataEmbeddable(Integer kitsLena) {
        this.kitsLena = kitsLena;
    }

    public Integer getKitsLena() {
        return kitsLena;
    }
}
