package co.edu.uniquindio.sga.application.usecase;

import co.edu.uniquindio.sga.CifradorFalso;
import co.edu.uniquindio.sga.application.exception.CredencialesInvalidasException;
import co.edu.uniquindio.sga.application.exception.TemporadaNoEncontradaException;
import co.edu.uniquindio.sga.domain.entity.Domo;
import co.edu.uniquindio.sga.domain.entity.PoliticaCancelacion;
import co.edu.uniquindio.sga.domain.entity.Temporada;
import co.edu.uniquindio.sga.domain.entity.Usuario;
import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.service.CalendarioTemporadasService;
import co.edu.uniquindio.sga.domain.service.GestionUsuariosService;
import co.edu.uniquindio.sga.domain.service.InventarioDomoService;
import co.edu.uniquindio.sga.domain.valueobject.Correo;
import co.edu.uniquindio.sga.domain.valueobject.Dinero;
import co.edu.uniquindio.sga.domain.valueobject.DocumentoIdentidad;
import co.edu.uniquindio.sga.domain.valueobject.IdTemporada;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionDomo;
import co.edu.uniquindio.sga.domain.valueobject.Periodo;
import co.edu.uniquindio.sga.domain.valueobject.Porcentaje;
import co.edu.uniquindio.sga.domain.valueobject.Rol;
import co.edu.uniquindio.sga.domain.valueobject.Tarifa;
import co.edu.uniquindio.sga.domain.valueobject.TramoRetencion;
import co.edu.uniquindio.sga.domain.valueobject.VigenciaCodigoRecuperacion;
import co.edu.uniquindio.sga.infrastructure.notificacion.NotificacionLogAdapter;
import co.edu.uniquindio.sga.infrastructure.persistence.inmemory.BloqueoRepositoryInMemory;
import co.edu.uniquindio.sga.infrastructure.persistence.inmemory.DomoRepositoryInMemory;
import co.edu.uniquindio.sga.infrastructure.persistence.inmemory.PoliticaCancelacionRepositoryInMemory;
import co.edu.uniquindio.sga.infrastructure.persistence.inmemory.ReservaRepositoryInMemory;
import co.edu.uniquindio.sga.infrastructure.persistence.inmemory.TemporadaRepositoryInMemory;
import co.edu.uniquindio.sga.infrastructure.persistence.inmemory.UsuarioRepositoryInMemory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static co.edu.uniquindio.sga.Fixtures.ALTA;
import static co.edu.uniquindio.sga.Fixtures.BASE;
import static co.edu.uniquindio.sga.Fixtures.DOM_01;
import static co.edu.uniquindio.sga.Fixtures.HOY;
import static co.edu.uniquindio.sga.Fixtures.domoBasico;
import static co.edu.uniquindio.sga.Fixtures.estancia;
import static co.edu.uniquindio.sga.Fixtures.pesos;
import static co.edu.uniquindio.sga.Fixtures.politicaFicha;
import static co.edu.uniquindio.sga.Fixtures.reservaBasica;
import static co.edu.uniquindio.sga.Fixtures.temporadas;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Casos de uso del corte 2 con los adaptadores en memoria (I-02): domos, temporadas, política
 * y usuarios. Los casos de uso orquestan; las reglas las aplica el dominio.
 */
class GestionCorte2UseCaseTest {

    private static final LocalDateTime AHORA = HOY.atTime(9, 0);

    private ReservaRepositoryInMemory reservas;
    private DomoRepositoryInMemory domos;
    private TemporadaRepositoryInMemory temporadasRepo;
    private UsuarioRepositoryInMemory usuarios;
    private NotificacionLogAdapter notificaciones;
    private GestionarDomosUseCase gestionarDomos;
    private GestionarTemporadasUseCase gestionarTemporadas;
    private GestionarPoliticaCancelacionUseCase gestionarPolitica;
    private RegistrarHuespedUseCase registrarHuesped;
    private AutenticarUsuarioUseCase autenticar;
    private RecuperarContrasenaUseCase recuperar;
    private AdministrarUsuariosUseCase administrarUsuarios;

    @BeforeEach
    void configurar() {
        reservas = new ReservaRepositoryInMemory();
        domos = new DomoRepositoryInMemory();
        temporadasRepo = new TemporadaRepositoryInMemory();
        usuarios = new UsuarioRepositoryInMemory();
        notificaciones = new NotificacionLogAdapter();
        temporadas().forEach(temporadasRepo::guardar);
        domos.guardar(domoBasico());
        var politicas = new PoliticaCancelacionRepositoryInMemory();
        politicas.guardar(politicaFicha());
        CifradorFalso cifrador = new CifradorFalso();
        GestionUsuariosService gestionUsuarios = new GestionUsuariosService(usuarios, reservas);

        gestionarDomos = new GestionarDomosUseCase(domos, temporadasRepo, new BloqueoRepositoryInMemory(),
                new InventarioDomoService(reservas, domos));
        gestionarTemporadas = new GestionarTemporadasUseCase(temporadasRepo, domos, new CalendarioTemporadasService());
        gestionarPolitica = new GestionarPoliticaCancelacionUseCase(politicas);
        registrarHuesped = new RegistrarHuespedUseCase(usuarios, gestionUsuarios, cifrador);
        autenticar = new AutenticarUsuarioUseCase(usuarios, cifrador);
        recuperar = new RecuperarContrasenaUseCase(usuarios, notificaciones, cifrador, new VigenciaCodigoRecuperacion(15));
        administrarUsuarios = new AdministrarUsuariosUseCase(usuarios, gestionUsuarios, cifrador);
    }

    // ------------------------------------------------------------------ domos (7.3, 7.4)

    @Test
    void unDomoQuedaActivoSoloSiLlegaConTarifasEnTodasLasTemporadas() {
        Domo incompleto = gestionarDomos.registrar(Domo.registrar(new IdentificacionDomo("DOM-06"), "Mirador", 1, 2),
                List.of(new Tarifa(BASE, pesos(190_000))), HOY);
        assertFalse(incompleto.estaActivo());
        Domo completo = gestionarDomos.registrar(Domo.registrar(new IdentificacionDomo("DOM-07"), "Cascada", 1, 2),
                List.of(new Tarifa(BASE, pesos(190_000)), new Tarifa(ALTA, pesos(290_000))), HOY);
        assertTrue(completo.estaActivo());
        assertThrows(ReglaDominioException.class, () -> gestionarDomos.registrar(
                Domo.registrar(DOM_01, "Repetido", 1, 2), List.of(), HOY));                    // L-04
        assertThrows(TemporadaNoEncontradaException.class, () -> gestionarDomos.registrar(
                Domo.registrar(new IdentificacionDomo("DOM-08"), "Otro", 1, 2),
                List.of(new Tarifa(new IdTemporada("NO_EXISTE"), pesos(1))), HOY));
    }

    @Test
    void retirarUnDomoConReservaActivaSeRechaza() {
        reservas.guardar(reservaBasica("RES-2026-00001", estancia("2026-10-10", "2026-10-12")));
        assertThrows(ReglaDominioException.class, () -> gestionarDomos.retirarDeVenta(DOM_01));
        assertFalse(domos.obtenerPorIdentificacion(DOM_01).orElseThrow().isEliminado());
    }

    // ------------------------------------------------------------------ temporadas y política (7.4)

    @Test
    void unaTemporadaSinTarifaParaLosDomosActivosSeRechaza() {
        Temporada puentes = new Temporada(new IdTemporada("PUENTES"), "Puentes",
                List.of(new Periodo(LocalDate.of(2026, 11, 13), LocalDate.of(2026, 11, 15))), false);
        assertThrows(ReglaDominioException.class, () -> gestionarTemporadas.registrar(puentes, Map.of(), HOY));
        gestionarTemporadas.registrar(puentes, Map.of(DOM_01, Dinero.de(230_000)), HOY);
        assertEquals(3, gestionarTemporadas.listar().size());
        assertEquals(pesos(230_000), domos.obtenerPorIdentificacion(DOM_01).orElseThrow()
                .tarifaEn(new IdTemporada("PUENTES")).orElseThrow().valorPorOcupanteNoche());
    }

    @Test
    void unaTemporadaQueSeSolapaSeRechaza() {
        Temporada solapada = new Temporada(new IdTemporada("NAVIDAD"), "Navidad",
                List.of(new Periodo(LocalDate.of(2026, 12, 20), LocalDate.of(2026, 12, 26))), false);
        assertThrows(ReglaDominioException.class,
                () -> gestionarTemporadas.registrar(solapada, Map.of(DOM_01, Dinero.de(300_000)), HOY));
    }

    @Test
    void publicarUnaVersionDeLaPoliticaConservaLaAnterior() {
        PoliticaCancelacion v2 = gestionarPolitica.publicarVersion(List.of(new TramoRetencion(240, Porcentaje.de(20)),
                new TramoRetencion(0, Porcentaje.de(80))), Porcentaje.de(100), LocalDate.of(2026, 11, 1));
        assertEquals(2, v2.getVersion().numero());
        assertEquals(2, gestionarPolitica.versiones().size());
        assertEquals(1, gestionarPolitica.vigente(HOY).getVersion().numero());
        assertEquals(2, gestionarPolitica.vigente(LocalDate.of(2026, 11, 2)).getVersion().numero());
    }

    // ------------------------------------------------------------------ usuarios y seguridad (7.2)

    @Test
    void registroInicioDeSesionYCorreoUnico() {
        Usuario usuario = registrarHuesped.ejecutar("Ana Ramírez", new Correo("ana@correo.com"), "Clave2026",
                "3001234567", new DocumentoIdentidad("1094123456"), LocalDate.of(1990, 5, 12));
        assertEquals(Rol.HUESPED, usuario.getRol());
        assertEquals(usuario, autenticar.ejecutar(new Correo("ANA@correo.com"), "Clave2026"));
        assertThrows(CredencialesInvalidasException.class, () -> autenticar.ejecutar(new Correo("ana@correo.com"), "Mala2026"));
        assertThrows(CredencialesInvalidasException.class, () -> autenticar.ejecutar(new Correo("nadie@correo.com"), "Clave2026"));
        assertThrows(ReglaDominioException.class, () -> registrarHuesped.ejecutar("Otra", new Correo("ana@correo.com"),
                "Clave2026", "300", new DocumentoIdentidad("555555555"), LocalDate.of(1990, 1, 1)));
    }

    @Test
    void recuperacionDeContrasenaConCodigoDeUnSoloUso() {
        registrarHuesped.ejecutar("Ana Ramírez", new Correo("ana@correo.com"), "Clave2026", "3001234567",
                new DocumentoIdentidad("1094123456"), LocalDate.of(1990, 5, 12));
        recuperar.solicitarCodigo(new Correo("ana@correo.com"), AHORA);
        recuperar.solicitarCodigo(new Correo("nadie@correo.com"), AHORA);        // no revela si existe
        assertEquals(1, notificaciones.getEnviados().size());
        String mensaje = notificaciones.getEnviados().get(0);
        String codigo = mensaje.replaceAll(".*código de recuperación es (\\d{6}).*", "$1");
        recuperar.restablecer(new Correo("ana@correo.com"), codigo, "Nueva2026", AHORA.plusMinutes(10));
        autenticar.ejecutar(new Correo("ana@correo.com"), "Nueva2026");
        assertThrows(ReglaDominioException.class,
                () -> recuperar.restablecer(new Correo("ana@correo.com"), codigo, "Otra2026", AHORA.plusMinutes(11)));
    }

    @Test
    void elAdministradorAsignaRolesYEliminaLogicamente() {
        Usuario recepcion = administrarUsuarios.crear("Recepción", new Correo("recepcion@jiyju.co"), "Recepcion2026",
                "3000000000", new DocumentoIdentidad("1000000002"), LocalDate.of(1990, 1, 1), Rol.RECEPCIONISTA);
        assertEquals(Rol.ADMINISTRADOR, administrarUsuarios.asignarRol(recepcion.getId(), Rol.ADMINISTRADOR).getRol());
        administrarUsuarios.eliminar(recepcion.getId());
        assertEquals(0, administrarUsuarios.listar(0).totalElementos());
        assertThrows(CredencialesInvalidasException.class,
                () -> autenticar.ejecutar(new Correo("recepcion@jiyju.co"), "Recepcion2026"));
    }
}
