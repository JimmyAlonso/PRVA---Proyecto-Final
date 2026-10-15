package co.edu.uniquindio.sga.domain.service;

import co.edu.uniquindio.sga.domain.entity.Domo;
import co.edu.uniquindio.sga.domain.entity.Ocupante;
import co.edu.uniquindio.sga.domain.entity.Temporada;
import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.valueobject.Cotizacion;
import co.edu.uniquindio.sga.domain.valueobject.DetalleNoche;
import co.edu.uniquindio.sga.domain.valueobject.Dinero;
import co.edu.uniquindio.sga.domain.valueobject.Estancia;
import co.edu.uniquindio.sga.domain.valueobject.RecargoPersonaAdicional;
import co.edu.uniquindio.sga.domain.valueobject.Tarifa;
import co.edu.uniquindio.sga.domain.valueobject.UmbralEdadFacturable;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * RN-05 (cada noche se cobra con la tarifa de la temporada vigente en esa noche, multiplicada
 * por los ocupantes facturables), RN-06 (facturables a la fecha de entrada) y L-20 (recargo
 * por persona adicional sobre la ocupación base).
 *
 * <p>El domo y las temporadas se reciben, no se buscan: el servicio sigue siendo dominio puro
 * y quien consulta los repositorios es el caso de uso.
 *
 * <p>Corrección: antes era {@code CotizadorEstanciaService<RecargoPersonaAdicional, DetalleNoche,
 * Cotizacion>}; esos nombres eran parámetros genéricos, no las clases, y no compilaba.
 */
public class CotizadorEstanciaService {

    // L-09 y L-20: configuración del alojamiento, llega por constructor
    private final UmbralEdadFacturable umbral;
    private final RecargoPersonaAdicional recargo;

    public CotizadorEstanciaService(UmbralEdadFacturable umbral, RecargoPersonaAdicional recargo) {
        if (umbral == null || recargo == null) {
            throw new ReglaDominioException("El umbral de edad y el recargo por persona adicional son obligatorios.");
        }
        this.umbral = umbral;
        this.recargo = recargo;
    }

    public Cotizacion cotizar(Domo domo, Estancia estancia, List<Ocupante> ocupantes, List<Temporada> temporadas) {
        if (domo == null || estancia == null) {
            throw new ReglaDominioException("El domo y la estancia son obligatorios para cotizar.");
        }
        if (ocupantes == null || ocupantes.isEmpty()) {
            throw new ReglaDominioException("Se requiere al menos un ocupante para cotizar.");
        }
        // RN-02
        if (!domo.admite(ocupantes.size())) {
            throw new ReglaDominioException("El número de ocupantes excede la capacidad del domo.");
        }
        // RN-06
        int facturables = (int) ocupantes.stream().filter(o -> o.esFacturableEn(estancia, umbral)).count();
        if (facturables == 0) {
            throw new ReglaDominioException("La estancia debe tener al menos un ocupante facturable.");
        }

        List<DetalleNoche> desglose = new ArrayList<>();
        for (LocalDate noche : estancia.nochesOcupadas()) {
            Temporada temporada = temporadaDe(noche, temporadas);
            Tarifa tarifa = domo.tarifaEn(temporada.getId()).orElseThrow(() -> new ReglaDominioException(
                    "El domo " + domo.getIdentificacion().valor() + " no tiene tarifa para la temporada "
                            + temporada.getNombre() + "."));

            Dinero base = tarifa.valorPorOcupanteNoche().multiplicarPor(facturables);                 // RN-05
            Dinero extra = recargo.porNoche(tarifa.valorPorOcupanteNoche(), ocupantes.size());       // L-20
            desglose.add(new DetalleNoche(noche, temporada.getId(), temporada.getNombre(),
                    tarifa.valorPorOcupanteNoche(), facturables, extra, base.sumar(extra)));
        }
        // 3.4: el total se redondea al peso una sola vez, en Cotizacion.total()
        return new Cotizacion(desglose);
    }

    /** F-05: la temporada especial que cubre la noche; si no hay ninguna, rige la base. */
    public static Temporada temporadaDe(LocalDate noche, List<Temporada> temporadas) {
        List<Temporada> especiales = temporadas.stream().filter(t -> !t.isEsBase() && t.cubre(noche)).toList();
        if (especiales.size() > 1) {
            throw new ReglaDominioException("Hay temporadas solapadas para la noche " + noche + ".");
        }
        if (especiales.size() == 1) {
            return especiales.get(0);
        }
        return temporadas.stream().filter(Temporada::isEsBase).findFirst()
                .orElseThrow(() -> new ReglaDominioException("No existe una temporada base configurada."));
    }
}
