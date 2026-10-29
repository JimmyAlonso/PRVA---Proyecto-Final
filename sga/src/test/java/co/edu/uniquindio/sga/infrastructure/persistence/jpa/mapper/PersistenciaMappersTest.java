package co.edu.uniquindio.sga.infrastructure.persistence.jpa.mapper;

import co.edu.uniquindio.sga.CifradorFalso;
import co.edu.uniquindio.sga.domain.entity.Alojamiento;
import co.edu.uniquindio.sga.domain.entity.Bloqueo;
import co.edu.uniquindio.sga.domain.entity.Domo;
import co.edu.uniquindio.sga.domain.entity.Folio;
import co.edu.uniquindio.sga.domain.entity.Novedad;
import co.edu.uniquindio.sga.domain.entity.Pago;
import co.edu.uniquindio.sga.domain.entity.PoliticaCancelacion;
import co.edu.uniquindio.sga.domain.entity.Reserva;
import co.edu.uniquindio.sga.domain.entity.Temporada;
import co.edu.uniquindio.sga.domain.entity.Usuario;
import co.edu.uniquindio.sga.domain.valueobject.AutorizacionCierre;
import co.edu.uniquindio.sga.domain.valueobject.CanalOrigen;
import co.edu.uniquindio.sga.domain.valueobject.CodigoRecuperacion;
import co.edu.uniquindio.sga.domain.valueobject.CodigoReserva;
import co.edu.uniquindio.sga.domain.valueobject.Correo;
import co.edu.uniquindio.sga.domain.valueobject.Deposito;
import co.edu.uniquindio.sga.domain.valueobject.DocumentoIdentidad;
import co.edu.uniquindio.sga.domain.valueobject.EstadoReserva;
import co.edu.uniquindio.sga.domain.valueobject.Fogata;
import co.edu.uniquindio.sga.domain.valueobject.GravedadNovedad;
import co.edu.uniquindio.sga.domain.valueobject.HoraEstimadaLlegada;
import co.edu.uniquindio.sga.domain.valueobject.HoraLimiteNoShow;
import co.edu.uniquindio.sga.domain.valueobject.IdBloqueo;
import co.edu.uniquindio.sga.domain.valueobject.IdNovedad;
import co.edu.uniquindio.sga.domain.valueobject.IdPago;
import co.edu.uniquindio.sga.domain.valueobject.IdUsuario;
import co.edu.uniquindio.sga.domain.valueobject.IdentificadorExterno;
import co.edu.uniquindio.sga.domain.valueobject.Malla;
import co.edu.uniquindio.sga.domain.valueobject.MedioPago;
import co.edu.uniquindio.sga.domain.valueobject.MotivoBloqueo;
import co.edu.uniquindio.sga.domain.valueobject.ParametrosOperacion;
import co.edu.uniquindio.sga.domain.valueobject.Periodo;
import co.edu.uniquindio.sga.domain.valueobject.PlazoConfirmacion;
import co.edu.uniquindio.sga.domain.valueobject.Porcentaje;
import co.edu.uniquindio.sga.domain.valueobject.Rol;
import co.edu.uniquindio.sga.domain.valueobject.ServicioAdicional;
import co.edu.uniquindio.sga.domain.valueobject.Tarifa;
import co.edu.uniquindio.sga.domain.valueobject.TiempoPreparacion;
import co.edu.uniquindio.sga.domain.valueobject.TipoNovedad;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static co.edu.uniquindio.sga.Fixtures.ANA;
import static co.edu.uniquindio.sga.Fixtures.BASE;
import static co.edu.uniquindio.sga.Fixtures.DOM_01;
import static co.edu.uniquindio.sga.Fixtures.HORARIO;
import static co.edu.uniquindio.sga.Fixtures.HOY;
import static co.edu.uniquindio.sga.Fixtures.LUIS;
import static co.edu.uniquindio.sga.Fixtures.POLITICA_V1;
import static co.edu.uniquindio.sga.Fixtures.RECARGO;
import static co.edu.uniquindio.sga.Fixtures.TITULAR;
import static co.edu.uniquindio.sga.Fixtures.UMBRAL;
import static co.edu.uniquindio.sga.Fixtures.cotizacionPlana;
import static co.edu.uniquindio.sga.Fixtures.domoBasico;
import static co.edu.uniquindio.sga.Fixtures.estancia;
import static co.edu.uniquindio.sga.Fixtures.pesos;
import static co.edu.uniquindio.sga.Fixtures.politicaFicha;
import static co.edu.uniquindio.sga.Fixtures.reservaBasica;
import static co.edu.uniquindio.sga.Fixtures.temporadas;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Persistir no cambia el dominio: cada agregado pasa a su entidad JPA y vuelve igual, con sus
 * objetos de valor, su estado y sus colecciones en orden. Se prueba sin base de datos.
 */
class PersistenciaMappersTest {

    @Test
    void laReservaVuelveConSuEstadoValorCongeladoYCanalExterno() {
        ReservaPersistenceMapper mapper = new ReservaPersistenceMapper();
        Reserva externa = Reserva.crear(new CodigoReserva("RES-2026-00007"), DOM_01, 2, estancia("2026-10-10", "2026-10-12"),
                TITULAR, List.of(ANA, LUIS), CanalOrigen.EXTERNO, cotizacionPlana(estancia("2026-10-10", "2026-10-12"), 360_000),
                POLITICA_V1, UMBRAL, HOY.atTime(9, 0));
        externa.marcarComoExterna(new IdentificadorExterno("BOOKING_COM", "EXT-1"));
        externa.registrarHoraEstimadaLlegada(new HoraEstimadaLlegada(LocalTime.of(19, 0)));
        externa.confirmar();
        externa.cancelar("Cancelada por el canal");

        Reserva copia = mapper.aDominio(mapper.aEntidad(externa));

        assertEquals(externa.getCodigo(), copia.getCodigo());
        assertEquals(EstadoReserva.CANCELADA, copia.getEstado());
        assertEquals(externa.getIdentificadorExterno(), copia.getIdentificadorExterno());
        assertEquals(externa.getValorCongelado(), copia.getValorCongelado());
        assertEquals(externa.getDesgloseCongelado(), copia.getDesgloseCongelado());
        assertEquals(externa.getOcupantes(), copia.getOcupantes());
        assertEquals(ANA.getFechaNacimiento(), copia.getOcupantes().get(0).getFechaNacimiento());
        assertEquals(externa.getTitular().getCorreo(), copia.getTitular().getCorreo());
        assertEquals(externa.getHoraEstimadaLlegada(), copia.getHoraEstimadaLlegada());
        assertEquals(externa.getPoliticaCongelada(), copia.getPoliticaCongelada());
        assertEquals(externa.getMomentoCreacion(), copia.getMomentoCreacion());
        assertEquals("Cancelada por el canal", copia.getMotivoCancelacion());
    }

    @Test
    void unaReservaSinCanalExternoNiHoraVuelveSinEllos() {
        ReservaPersistenceMapper mapper = new ReservaPersistenceMapper();
        Reserva copia = mapper.aDominio(mapper.aEntidad(reservaBasica("RES-2026-00001", estancia("2026-10-10", "2026-10-12"))));
        assertTrue(copia.getIdentificadorExterno().isEmpty());
        assertEquals(null, copia.getHoraEstimadaLlegada());
        assertEquals(EstadoReserva.PENDIENTE, copia.getEstado());
    }

    @Test
    void elDomoVuelveConDotacionTarifasHistorialYEliminacionLogica() {
        DomoPersistenceMapper mapper = new DomoPersistenceMapper();
        Domo domo = domoBasico();
        domo.describir("Vista panorámica", List.of("Jacuzzi", "Malla"));
        domo.equiparMalla(new Malla(250));
        domo.equiparFogata(new Fogata(2));
        domo.establecerTarifa(new Tarifa(BASE, pesos(200_000)), HOY);
        domo.declararFueraDeServicio();
        domo.retirarDeVenta();

        Domo copia = mapper.aDominio(mapper.aEntidad(domo));

        assertEquals(domo.getIdentificacion(), copia.getIdentificacion());
        assertEquals(List.of("Jacuzzi", "Malla"), copia.getCaracteristicas());
        assertEquals(domo.getJacuzzi(), copia.getJacuzzi());
        assertEquals(domo.getMalla(), copia.getMalla());
        assertEquals(domo.getFogata(), copia.getFogata());
        assertEquals(domo.getEstadoOperativo(), copia.getEstadoOperativo());
        assertEquals(domo.getTarifas(), copia.getTarifas());
        assertEquals(domo.getHistorialTarifas(), copia.getHistorialTarifas());
        assertTrue(copia.isEliminado());
        assertFalse(copia.estaActivo());
    }

    @Test
    void unDomoSinDotacionOpcionalVuelveSinElla() {
        DomoPersistenceMapper mapper = new DomoPersistenceMapper();
        Domo domo = new Domo(DOM_01, "Domo Básico", 1, 2);
        Domo copia = mapper.aDominio(mapper.aEntidad(domo));
        assertTrue(copia.getJacuzzi().isEmpty());
        assertTrue(copia.getMalla().isEmpty());
        assertTrue(copia.getFogata().isEmpty());
    }

    @Test
    void elFolioConservaCargosInversosPagosYAutorizacion() {
        FolioPersistenceMapper mapper = new FolioPersistenceMapper();
        Folio folio = Folio.abrir(new CodigoReserva("RES-2026-00001"), pesos(720_000), HOY);
        folio.registrarDeposito(new Deposito(pesos(100_000)), HOY);
        folio.registrarPago(new Pago(new IdPago("P-1"), pesos(360_000), MedioPago.TRANSFERENCIA, HOY));
        folio.devolverDeposito(HOY);
        folio.cerrar(new AutorizacionCierre("admin@jiyju.co", "Saldo pendiente acordado"));

        Folio copia = mapper.aDominio(mapper.aEntidad(folio));

        assertEquals(folio.getCargos(), copia.getCargos());
        assertEquals(folio.getPagos(), copia.getPagos());
        assertEquals(folio.getPagos().get(0).getMedio(), copia.getPagos().get(0).getMedio());
        assertEquals(folio.saldo(), copia.saldo());
        assertTrue(copia.isCerrado());
        assertEquals(folio.getAutorizacionCierre(), copia.getAutorizacionCierre());
    }

    @Test
    void temporadasBloqueosNovedadesYPoliticaVuelvenIguales() {
        CatalogoPersistenceMapper mapper = new CatalogoPersistenceMapper();
        Temporada alta = temporadas().get(1);
        Temporada copiaAlta = mapper.aDominio(mapper.aEntidad(alta));
        assertEquals(alta.getPeriodos(), copiaAlta.getPeriodos());
        assertFalse(copiaAlta.isEsBase());

        Bloqueo bloqueo = new Bloqueo(new IdBloqueo("B-1"), DOM_01, new Periodo(HOY, HOY.plusDays(3)),
                MotivoBloqueo.MANTENIMIENTO, "Pintura");
        bloqueo.levantar();
        Bloqueo copiaBloqueo = mapper.aDominio(mapper.aEntidad(bloqueo));
        assertEquals(bloqueo.getPeriodo(), copiaBloqueo.getPeriodo());
        assertFalse(copiaBloqueo.isVigente());

        Novedad novedad = new Novedad(new IdNovedad("N-1"), DOM_01, TipoNovedad.DANO, "Malla rota", GravedadNovedad.GRAVE,
                "servicio@jiyju.co", HOY.atTime(10, 0));
        novedad.marcarAtendida();
        Novedad copiaNovedad = mapper.aDominio(mapper.aEntidad(novedad));
        assertEquals(novedad.getGravedad(), copiaNovedad.getGravedad());
        assertTrue(copiaNovedad.isAtendida());

        PoliticaCancelacion politica = politicaFicha();
        PoliticaCancelacion copiaPolitica = mapper.aDominio(mapper.aEntidad(politica));
        assertEquals(politica.getVersion(), copiaPolitica.getVersion());
        assertEquals(politica.getTramos(), copiaPolitica.getTramos());
        assertEquals(politica.getRetencionNoShow(), copiaPolitica.getRetencionNoShow());
    }

    @Test
    void elUsuarioVuelveConSuRolContrasenaCifradaYCodigoDeRecuperacion() {
        UsuarioPersistenceMapper mapper = new UsuarioPersistenceMapper();
        CifradorFalso cifrador = new CifradorFalso();
        Usuario usuario = Usuario.crearConRol(new IdUsuario("U-1"), "Recepción", new Correo("recepcion@jiyju.co"),
                cifrador.cifrar("Recepcion2026"), "3000000000", new DocumentoIdentidad("1000000002"),
                LocalDate.of(1990, 1, 1), Rol.RECEPCIONISTA);
        usuario.emitirCodigoRecuperacion(CodigoRecuperacion.emitir("123456", LocalDateTime.of(2026, 10, 1, 9, 0), 15));

        Usuario copia = mapper.aDominio(mapper.aEntidad(usuario));

        assertEquals(usuario.getId(), copia.getId());
        assertEquals(Rol.RECEPCIONISTA, copia.getRol());
        assertTrue(copia.autenticaCon("Recepcion2026", cifrador));
        assertEquals(usuario.getCodigoRecuperacion(), copia.getCodigoRecuperacion());
    }

    @Test
    void elAlojamientoVuelveConSusServiciosYLosParametrosDeLaConfiguracion() {
        AlojamientoPersistenceMapper mapper = new AlojamientoPersistenceMapper();
        ParametrosOperacion parametros = new ParametrosOperacion(UMBRAL, new TiempoPreparacion(4), new PlazoConfirmacion(4),
                new HoraLimiteNoShow(LocalTime.of(20, 0)), Porcentaje.de(50), new Deposito(pesos(100_000)), RECARGO);
        Alojamiento alojamiento = new Alojamiento("JiyJu Glamping", "Eco-glamping", "Salento", "Vereda Palestina",
                new BigDecimal("4.637800"), new BigDecimal("-75.570300"), HORARIO, "Silencio 10 pm", parametros, false);
        alojamiento.agregarServicio(new ServicioAdicional("KIT_FOGATA", "Kit de fogata", pesos(30_000), false));

        Alojamiento copia = mapper.aDominio(mapper.aEntidad(alojamiento), HORARIO, parametros);

        assertEquals(alojamiento.getNombre(), copia.getNombre());
        assertEquals(alojamiento.getLatitud(), copia.getLatitud());
        assertEquals(alojamiento.getServicios(), copia.getServicios());
        assertEquals(parametros, copia.getParametros());
    }
}
