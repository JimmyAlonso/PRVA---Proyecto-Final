package co.edu.uniquindio.sga.infrastructure.persistence.jpa.embeddable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

/** Glosario propio: malla catamarán con su peso máximo. */
@Embeddable
public class MallaEmbeddable {

    @Column(name = "malla_peso_maximo_kg")
    private Integer pesoMaximoKg;

    protected MallaEmbeddable() {
        // requerido por JPA
    }

    public MallaEmbeddable(Integer pesoMaximoKg) {
        this.pesoMaximoKg = pesoMaximoKg;
    }

    public Integer getPesoMaximoKg() {
        return pesoMaximoKg;
    }
}
