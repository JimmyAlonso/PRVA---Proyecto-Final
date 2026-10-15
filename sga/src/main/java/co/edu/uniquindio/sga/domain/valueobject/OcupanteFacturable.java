package co.edu.uniquindio.sga.domain.valueobject;

import co.edu.uniquindio.sga.domain.entity.Ocupante;
import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;

public record OcupanteFacturable(DocumentoIdentidad documento, int edadAlEntrar) {
    public OcupanteFacturable {
        if (documento == null) throw new ReglaDominioException("El ocupante facturable debe tener documento.");
    }
    public static OcupanteFacturable de(Ocupante ocupante, Estancia estancia, UmbralEdadFacturable umbral) {
        if (!ocupante.esFacturableEn(estancia, umbral))
            throw new ReglaDominioException("El ocupante no alcanza el umbral de edad facturable.");
        return new OcupanteFacturable(ocupante.getDocumento(), ocupante.edadA(estancia.fechaEntrada()));
    }
}
