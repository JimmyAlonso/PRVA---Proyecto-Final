package co.edu.uniquindio.sga.domain.service;

import co.edu.uniquindio.sga.CifradorFalso;
import co.edu.uniquindio.sga.domain.entity.Bloqueo;
import co.edu.uniquindio.sga.domain.entity.Domo;
import co.edu.uniquindio.sga.domain.entity.Reserva;
import co.edu.uniquindio.sga.domain.entity.Temporada;
import co.edu.uniquindio.sga.domain.entity.Usuario;
import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.valueobject.Correo;
import co.edu.uniquindio.sga.domain.valueobject.DocumentoIdentidad;
import co.edu.uniquindio.sga.domain.valueobject.EstadoNoche;
import co.edu.uniquindio.sga.domain.valueobject.IdBloqueo;
import co.edu.uniquindio.sga.domain.valueobject.IdTemporada;
import co.edu.uniquindio.sga.domain.valueobject.IdUsuario;
import co.edu.uniquindio.sga.domain.valueobject.MotivoBloqueo;
import co.edu.uniquindio.sga.domain.valueobject.NocheCalendario;
import co.edu.uniquindio.sga.domain.valueobject.Periodo;
import co.edu.uniquindio.sga.infrastructure.persistence.inmemory.BloqueoRepositoryInMemory;
import co.edu.uniquindio.sga.infrastructure.persistence.inmemory.DomoRepositoryInMemory;
import co.edu.uniquindio.sga.infrastructure.persistence.inmemory.ReservaRepositoryInMemory;
import co.edu.uniquindio.sga.infrastructure.persistence.inmemory.UsuarioRepositoryInMemory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static co.edu.uniquindio.sga.Fixtures.DOM_01;
import static co.edu.uniquindio.sga.Fixtures.domoBasico;
import static co.edu.uniquindio.sga.Fixtures.domoFamiliar;
import static co.edu.uniquindio.sga.Fixtures.estancia;
import static co.edu.uniquindio.sga.Fixtures.reservaBasica;
import static co.edu.uniquindio.sga.Fixtures.temporadas;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Servicios de dominio del corte 2: inventario (7.3), usuarios (7.2), calendario (7.5) y temporadas (7.4). */
class ServiciosDelCorte2Test {

    private ReservaRepositoryInMemory reservas;
    private DomoRepositoryInMemory domos;
    private BloqueoRepositoryInMemory bloqueos;
    private InventarioDomoService inventario;

    @BeforeEach
    void configurar() {
        reservas = new ReservaRepositoryInMemory();
        domos = new DomoRepositoryInMemory();
        bloqueos = new BloqueoRepositoryInMemory();
        inventario = new InventarioDomoService(reservas, domos);
    }

    @Test
    void noSeRetiraUnDomoConReservasActivas() {                                            // 7.3
        Domo domo = domoBasico();
        Reserva activa = reservaBasica("RES-2026-00001", estancia("2026-10-10", "2026-10-12"));
        reservas.guardar(activa);
        assertThrows(ReglaDominioException.class, () -> inventario.retirarDeVenta(domo));
        activa.cancelar("El huésped cambió de planes");
        inventario.retirarDeVenta(domo);
        assertTrue(domo.isEliminado());
    }

    @Test
    void noSeBloqueanNochesConReservasActivas() {                                          // 7.3 Bloqueos
        Domo domo = domoBasico();
        reservas.guardar(reservaBasica("RES-2026-00001", estancia("2026-10-10", "2026-10-12")));
        assertThrows(ReglaDominioException.class, () -> inventario.registrarBloqueo(new IdBloqueo("B-1"), domo,
                new Periodo(LocalDate.of(2026, 10, 11), LocalDate.of(2026, 10, 15)), MotivoBloqueo.MANTENIMIENTO, null));
        // La noche de salida (12) no está ocupada: [entrada, salida)
        Bloqueo bloqueo = inventario.registrarBloqueo(new IdBloqueo("B-2"), domo,
                new Periodo(LocalDate.of(2026, 10, 12), LocalDate.of(2026, 10, 15)), MotivoBloqueo.MANTENIMIENTO, null);
        assertEquals(DOM_01, bloqueo.getDomo());
    }

    @Test
    void laIdentificacionDelDomoEsUnica() {                                                // L-04
        domos.guardar(domoBasico());
        assertThrows(ReglaDominioException.class, () -> inventario.verificarIdentificacionDisponible(DOM_01));
    }

    @Test
    void reducirLaCapacidadAdvierteLasReservasQueQuedanPorEncima() {                       // 7.3
        Domo domo = domoBasico();
        reservas.guardar(reservaBasica("RES-2026-00001", estancia("2026-10-10", "2026-10-12")));
        domo.actualizarDatos("Domo Básico", null, 1, 1, List.of());
        assertEquals(1, inventario.reservasQueSuperanCapacidad(domo).size());
    }

    @Test
    void elCalendarioMarcaNochesReservadasBloqueadasYLibres() {                            // 7.5
        Domo domo = domoBasico();
        reservas.guardar(reservaBasica("RES-2026-00001", estancia("2026-10-10", "2026-10-12")));
        bloqueos.guardar(new Bloqueo(new IdBloqueo("B-1"), DOM_01,
                new Periodo(LocalDate.of(2026, 10, 13), LocalDate.of(2026, 10, 13)), MotivoBloqueo.USO_INTERNO, null));
        List<NocheCalendario> noches = new CalendarioOcupacionService(reservas, bloqueos)
                .calendario(domo, new Periodo(LocalDate.of(2026, 10, 10), LocalDate.of(2026, 10, 14)));
        assertEquals(5, noches.size());
        assertEquals(EstadoNoche.RESERVADA, noches.get(0).estado());
        assertEquals(EstadoNoche.RESERVADA, noches.get(1).estado());
        assertEquals(EstadoNoche.LIBRE, noches.get(2).estado());                         // noche de salida
        assertEquals(EstadoNoche.BLOQUEADA, noches.get(3).estado());
        assertEquals(EstadoNoche.LIBRE, noches.get(4).estado());
    }

    @Test
    void unaTemporadaNuevaExigeTarifaEnTodosLosDomosActivos() {                            // 7.4
        CalendarioTemporadasService calendario = new CalendarioTemporadasService();
        List<Temporada> conNueva = new ArrayList<>(temporadas());
        Temporada puentes = new Temporada(new IdTemporada("PUENTES"), "Puentes",
                List.of(new Periodo(LocalDate.of(2026, 11, 13), LocalDate.of(2026, 11, 15))), false);
        conNueva.add(puentes);
        assertThrows(ReglaDominioException.class,
                () -> calendario.verificarTarifasCompletas(List.of(domoBasico(), domoFamiliar()), conNueva));
        assertThrows(ReglaDominioException.class, () -> calendario.verificarRegistro(temporadas(), temporadas().get(1)));
    }

    @Test
    void elCorreoEsUnicoYNoSeEliminaUnTitularConReservasActivas() {                        // 7.2
        UsuarioRepositoryInMemory usuarios = new UsuarioRepositoryInMemory();
        GestionUsuariosService gestion = new GestionUsuariosService(usuarios, reservas);
        CifradorFalso cifrador = new CifradorFalso();
        Usuario ana = Usuario.registrarHuesped(new IdUsuario("U-1"), "Ana Ramírez", new Correo("ana@correo.com"),
                cifrador.cifrar("Clave2026"), "3001234567", new DocumentoIdentidad("1094123456"), LocalDate.of(1990, 5, 12));
        usuarios.guardar(ana);
        assertThrows(ReglaDominioException.class,
                () -> gestion.verificarRegistro(new Correo("ANA@correo.com"), new DocumentoIdentidad("999999999")));

        Reserva activa = reservaBasica("RES-2026-00001", estancia("2026-10-10", "2026-10-12"));  // titular: Ana
        reservas.guardar(activa);
        assertThrows(ReglaDominioException.class, () -> gestion.eliminar(ana));
        activa.cancelar("El huésped cambió de planes");
        gestion.eliminar(ana);
        assertTrue(ana.isEliminado());
    }
}
