package co.edu.uniquindio.sga.domain.service;
// CotizadorEstanciaService.java  — RN-05, RN-06, L-20

import co.edu.uniquindio.sga.domain.entity.Domo;
import co.edu.uniquindio.sga.domain.entity.Ocupante;
import co.edu.uniquindio.sga.domain.entity.Temporada;
import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.repository.DomoRepository;
import co.edu.uniquindio.sga.domain.repository.TemporadaRepository;
import co.edu.uniquindio.sga.domain.valueobject.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class CotizadorEstanciaService<RecargoPersonaAdicional, DetalleNoche, Cotizacion> {
        private final TemporadaRepository temporadaRepository;
        private final DomoRepository domoRepository;
        private final RecargoPersonaAdicional recargo;

        public CotizadorEstanciaService(TemporadaRepository temporadaRepository, DomoRepository domoRepository,
                                        RecargoPersonaAdicional recargo) {
            this.temporadaRepository = temporadaRepository;
            this.domoRepository = domoRepository;
            this.recargo = recargo;
        }

        public Cotizacion  cotizar(IdentificacionDomo idDomo, Estancia estancia,
                                               List<Ocupante> ocupantes, UmbralEdadFacturable umbral) {
            if (idDomo == null || estancia == null || umbral == null)
                throw new ReglaDominioException("Domo, estancia y umbral son obligatorios para cotizar.");
            if (ocupantes == null || ocupantes.isEmpty())
                throw new ReglaDominioException("Se requiere al menos un ocupante para cotizar.");

            Domo domo = domoRepository.obtenerPorIdentificacion(idDomo)
                    .orElseThrow(() -> new ReglaDominioException("No existe el domo " + idDomo.valor() + "."));
            if (!domo.estaActivo()) throw new ReglaDominioException("El domo no está activo.");
            if (!domo.admite(ocupantes.size()))                                   // RN-02
                throw new ReglaDominioException("El número de ocupantes excede la capacidad del domo.");

            int facturables = (int) ocupantes.stream().filter(o -> o.esFacturableEn(estancia, umbral)).count(); // RN-06
            if (facturables == 0) throw new ReglaDominioException("La reserva debe tener al menos un ocupante facturable.");

            List<Temporada> temporadas = temporadaRepository.listarTodas();
            List<DetalleNoche> desglose = new ArrayList<>();
            Dinero total = Dinero.CERO;

            for (LocalDate noche : estancia.nochesOcupadas()) {
                Temporada temporada = temporadaDe(noche, temporadas);
                Tarifa tarifa = domo.tarifaEn(temporada.getId()).orElseThrow(() -> new ReglaDominioException(
                        "El domo no tiene tarifa para la temporada " + temporada.getNombre() + "."));

                Dinero base = tarifa.valorPorOcupanteNoche().multiplicarPor(facturables);           // RN-05
                Dinero extra = recargo.porNoche(tarifa.valorPorOcupanteNoche(), ocupantes.size());  // L-20
                Dinero subtotal = base.sumar(extra);

                desglose.add(new DetalleNoche(noche, temporada.getId(), tarifa.valorPorOcupanteNoche(),
                        facturables, extra, subtotal));
                total = total.sumar(subtotal);
            }
            return new Cotizacion(desglose, total);
        }

        // F-05: la temporada especial que cubre la noche; si no hay, la base
        private Temporada temporadaDe(LocalDate noche, List<Temporada> temporadas) {
            List<Temporada> especiales = temporadas.stream().filter(t -> !t.isEsBase() && t.cubre(noche)).toList();
            if (especiales.size() > 1) throw new ReglaDominioException("Hay temporadas solapadas para la noche " + noche + ".");
            if (especiales.size() == 1) return especiales.get(0);
            return temporadas.stream().filter(Temporada::isEsBase).findFirst()
                    .orElseThrow(() -> new ReglaDominioException("No existe una temporada base configurada."));
        }
    }
