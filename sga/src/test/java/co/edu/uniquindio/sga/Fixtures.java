package co.edu.uniquindio.sga;

import co.edu.uniquindio.sga.domain.entity.Domo;
import co.edu.uniquindio.sga.domain.entity.Ocupante;
import co.edu.uniquindio.sga.domain.entity.PoliticaCancelacion;
import co.edu.uniquindio.sga.domain.entity.Reserva;
import co.edu.uniquindio.sga.domain.entity.Temporada;
import co.edu.uniquindio.sga.domain.entity.TitularDeLaReserva;
import co.edu.uniquindio.sga.domain.valueobject.CanalOrigen;
import co.edu.uniquindio.sga.domain.valueobject.CodigoReserva;
import co.edu.uniquindio.sga.domain.valueobject.Cotizacion;
import co.edu.uniquindio.sga.domain.valueobject.DetalleNoche;
import co.edu.uniquindio.sga.domain.valueobject.Dinero;
import co.edu.uniquindio.sga.domain.valueobject.DocumentoIdentidad;
import co.edu.uniquindio.sga.domain.valueobject.Estancia;
import co.edu.uniquindio.sga.domain.valueobject.HorarioAlojamiento;
import co.edu.uniquindio.sga.domain.valueobject.IdTemporada;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionDomo;
import co.edu.uniquindio.sga.domain.valueobject.Jacuzzi;
import co.edu.uniquindio.sga.domain.valueobject.Periodo;
import co.edu.uniquindio.sga.domain.valueobject.Porcentaje;
import co.edu.uniquindio.sga.domain.valueobject.RecargoPersonaAdicional;
import co.edu.uniquindio.sga.domain.valueobject.Tarifa;
import co.edu.uniquindio.sga.domain.valueobject.TramoRetencion;
import co.edu.uniquindio.sga.domain.valueobject.UmbralEdadFacturable;
import co.edu.uniquindio.sga.domain.valueobject.VersionPolitica;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/** Datos de prueba compartidos, tomados de la Ficha A de JiyJu Glamping. */
public final class Fixtures {

    private Fixtures() {
    }

    public static final LocalDate HOY = LocalDate.of(2026, 10, 1);
    public static final IdentificacionDomo DOM_01 = new IdentificacionDomo("DOM-01");
    public static final IdentificacionDomo DOM_03 = new IdentificacionDomo("DOM-03");
    public static final IdentificacionDomo DOM_04 = new IdentificacionDomo("DOM-04");
    public static final IdTemporada BASE = new IdTemporada("BASE");
    public static final IdTemporada ALTA = new IdTemporada("ALTA");
    public static final UmbralEdadFacturable UMBRAL = new UmbralEdadFacturable(12);
    public static final HorarioAlojamiento HORARIO = new HorarioAlojamiento(LocalTime.of(15, 0), LocalTime.of(11, 0));
    public static final RecargoPersonaAdicional RECARGO = new RecargoPersonaAdicional(2, Porcentaje.de(25));
    public static final VersionPolitica POLITICA_V1 = new VersionPolitica(1, LocalDate.of(2026, 1, 1));

    public static final TitularDeLaReserva TITULAR = new TitularDeLaReserva(new DocumentoIdentidad("1094123456"),
            "Ana Ramírez", "ana@correo.com", "3001234567");
    public static final Ocupante ANA = new Ocupante(new DocumentoIdentidad("1094123456"), "Ana Ramírez",
            LocalDate.of(1990, 5, 12));
    public static final Ocupante LUIS = new Ocupante(new DocumentoIdentidad("1094999888"), "Luis Ramírez",
            LocalDate.of(1988, 2, 1));
    public static final Ocupante SOFIA_NINA = new Ocupante(new DocumentoIdentidad("TI1030998877"), "Sofía Ramírez",
            LocalDate.of(2019, 3, 2));
    public static final Ocupante PEDRO = new Ocupante(new DocumentoIdentidad("1094555666"), "Pedro Ramírez",
            LocalDate.of(1960, 7, 7));

    public static Dinero pesos(long valor) {
        return Dinero.de(valor);
    }

    public static Estancia estancia(String entrada, String salida) {
        return new Estancia(LocalDate.parse(entrada), LocalDate.parse(salida));
    }

    public static List<Temporada> temporadas() {
        return List.of(Temporada.base(BASE, "Base"),
                new Temporada(ALTA, "Alta", List.of(new Periodo(LocalDate.of(2026, 12, 15), LocalDate.of(2027, 1, 15))), false));
    }

    /** DOM-01: Básico, capacidad 2, con Jacuzzi (3 h). Base 180.000 / Alta 280.000. */
    public static Domo domoBasico() {
        Domo domo = new Domo(DOM_01, "Domo Básico", 1, 2);
        domo.equiparJacuzzi(new Jacuzzi("Jacuzzi a leña", 3));
        domo.definirTarifa(new Tarifa(BASE, pesos(180_000)));
        domo.definirTarifa(new Tarifa(ALTA, pesos(280_000)));
        return domo;
    }

    /** DOM-03: Familiar, capacidad 4, con Jacuzzi. Base 250.000 / Alta 350.000. */
    public static Domo domoFamiliar() {
        Domo domo = new Domo(DOM_03, "Domo Familiar", 2, 4);
        domo.equiparJacuzzi(new Jacuzzi("Jacuzzi", 3));
        domo.definirTarifa(new Tarifa(BASE, pesos(250_000)));
        domo.definirTarifa(new Tarifa(ALTA, pesos(350_000)));
        return domo;
    }

    /** DOM-04: Exclusivo, sin Jacuzzi. */
    public static Domo domoExclusivo() {
        Domo domo = new Domo(DOM_04, "Domo Exclusivo", 1, 2);
        domo.definirTarifa(new Tarifa(BASE, pesos(220_000)));
        domo.definirTarifa(new Tarifa(ALTA, pesos(320_000)));
        return domo;
    }

    /** Política de la ficha: ≥ 15 días 10 %, 5-14 días 50 %, < 5 días 100 %, no-show 100 %. */
    public static PoliticaCancelacion politicaFicha() {
        return new PoliticaCancelacion(POLITICA_V1, List.of(
                new TramoRetencion(360, Porcentaje.de(10)),
                new TramoRetencion(120, Porcentaje.de(50)),
                new TramoRetencion(0, Porcentaje.de(100))), Porcentaje.de(100));
    }

    /** Cotización simple: un subtotal fijo por noche. */
    public static Cotizacion cotizacionPlana(Estancia estancia, long porNoche) {
        List<DetalleNoche> noches = new ArrayList<>();
        for (LocalDate noche : estancia.nochesOcupadas()) {
            noches.add(new DetalleNoche(noche, BASE, "Base", pesos(porNoche), 1, Dinero.CERO, pesos(porNoche)));
        }
        return new Cotizacion(noches);
    }

    public static Reserva reserva(String codigo, IdentificacionDomo domo, int capacidad, Estancia estancia,
                                  List<Ocupante> ocupantes) {
        return Reserva.crear(new CodigoReserva(codigo), domo, capacidad, estancia, TITULAR, ocupantes,
                CanalOrigen.PORTAL, cotizacionPlana(estancia, 360_000), POLITICA_V1, UMBRAL, HOY.atTime(9, 0));
    }

    public static Reserva reservaBasica(String codigo, Estancia estancia) {
        return reserva(codigo, DOM_01, 2, estancia, List.of(ANA, LUIS));
    }
}
