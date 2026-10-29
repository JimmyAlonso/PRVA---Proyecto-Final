package co.edu.uniquindio.sga.application.usecase;

import co.edu.uniquindio.sga.application.dto.Pagina;
import co.edu.uniquindio.sga.application.exception.BloqueoNoEncontradoException;
import co.edu.uniquindio.sga.domain.entity.Bloqueo;
import co.edu.uniquindio.sga.domain.entity.Domo;
import co.edu.uniquindio.sga.domain.entity.Reserva;
import co.edu.uniquindio.sga.domain.entity.Temporada;
import co.edu.uniquindio.sga.domain.repository.BloqueoRepository;
import co.edu.uniquindio.sga.domain.repository.DomoRepository;
import co.edu.uniquindio.sga.domain.repository.TemporadaRepository;
import co.edu.uniquindio.sga.domain.service.InventarioDomoService;
import co.edu.uniquindio.sga.domain.valueobject.Fogata;
import co.edu.uniquindio.sga.domain.valueobject.IdBloqueo;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionDomo;
import co.edu.uniquindio.sga.domain.valueobject.Jacuzzi;
import co.edu.uniquindio.sga.domain.valueobject.Malla;
import co.edu.uniquindio.sga.domain.valueobject.MotivoBloqueo;
import co.edu.uniquindio.sga.domain.valueobject.Periodo;
import co.edu.uniquindio.sga.domain.valueobject.RegistroTarifa;
import co.edu.uniquindio.sga.domain.valueobject.Tarifa;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * 7.1 Administrador: gestionar domos (CRUD con eliminación lógica), sus tarifas (7.4), su
 * estado FUERA_DE_SERVICIO y los bloqueos (7.3). Las reglas viven en el agregado Domo y en
 * InventarioDomoService; aquí solo se traen los agregados, se invoca al dominio y se guarda.
 */
@Service
@Transactional
public class GestionarDomosUseCase {

    /** Resultado de una actualización: 7.3 exige advertir las reservas que quedan por encima de la nueva capacidad. */
    public record DomoActualizado(Domo domo, List<Reserva> reservasPorEncimaDeLaCapacidad) {
    }

    private final DomoRepository domoRepository;
    private final TemporadaRepository temporadaRepository;
    private final BloqueoRepository bloqueoRepository;
    private final InventarioDomoService inventarioService;

    public GestionarDomosUseCase(DomoRepository domoRepository, TemporadaRepository temporadaRepository,
                                 BloqueoRepository bloqueoRepository, InventarioDomoService inventarioService) {
        this.domoRepository = domoRepository;
        this.temporadaRepository = temporadaRepository;
        this.bloqueoRepository = bloqueoRepository;
        this.inventarioService = inventarioService;
    }

    /** 7.3: inventario completo para el administrador (activos e inactivos, sin los eliminados). */
    @Transactional(readOnly = true)
    public Pagina<Domo> inventario(int pagina) {
        return Pagina.de(domoRepository.listarNoEliminados(), pagina);
    }

    /**
     * 7.3 y 7.4: registra un domo nuevo con sus tarifas. Queda activo solo si tiene tarifa en
     * todas las temporadas; si no, queda inactivo hasta completarlas.
     */
    public Domo registrar(Domo nuevo, List<Tarifa> tarifas, LocalDate fecha) {
        inventarioService.verificarIdentificacionDisponible(nuevo.getIdentificacion());   // L-04
        aplicarTarifas(nuevo, tarifas, fecha);
        List<Temporada> temporadas = temporadaRepository.buscarTodas();
        if (nuevo.tieneTarifasCompletas(temporadas)) {
            nuevo.activar(temporadas);
        }
        domoRepository.guardar(nuevo);
        return nuevo;
    }

    /** 7.3: actualiza datos y dotación. Las reservas existentes no cambian aunque superen la nueva capacidad. */
    public DomoActualizado actualizar(IdentificacionDomo id, String nombre, String descripcion, int dormitorios,
                                      int capacidad, List<String> caracteristicas, Jacuzzi jacuzzi, Malla malla,
                                      Fogata fogata) {
        Domo domo = Buscar.domo(domoRepository, id);
        domo.actualizarDatos(nombre, descripcion, dormitorios, capacidad, caracteristicas);
        domo.equiparJacuzzi(jacuzzi);
        domo.equiparMalla(malla);
        domo.equiparFogata(fogata);
        domoRepository.guardar(domo);
        return new DomoActualizado(domo, inventarioService.reservasQueSuperanCapacidad(domo));
    }

    /** 7.4: define o cambia tarifas; el valor anterior pasa al histórico y las reservas creadas no cambian (RN-22). */
    public Domo establecerTarifas(IdentificacionDomo id, List<Tarifa> tarifas, LocalDate fecha) {
        Domo domo = Buscar.domo(domoRepository, id);
        aplicarTarifas(domo, tarifas, fecha);
        domoRepository.guardar(domo);
        return domo;
    }

    @Transactional(readOnly = true)
    public List<RegistroTarifa> historialTarifas(IdentificacionDomo id) {
        return Buscar.domo(domoRepository, id).getHistorialTarifas();
    }

    /** 7.4: el sistema impide activar un domo sin tarifas completas. */
    public Domo activar(IdentificacionDomo id) {
        Domo domo = Buscar.domo(domoRepository, id);
        domo.activar(temporadaRepository.buscarTodas());
        domoRepository.guardar(domo);
        return domo;
    }

    /** 7.3: retiro de la venta (eliminación lógica), solo sin reservas activas ni futuras. */
    public Domo retirarDeVenta(IdentificacionDomo id) {
        Domo domo = Buscar.domo(domoRepository, id);
        inventarioService.retirarDeVenta(domo);
        domoRepository.guardar(domo);
        return domo;
    }

    /** 7.6: FUERA_DE_SERVICIO desde cualquier estado no ocupado, solo por administrador. */
    public Domo declararFueraDeServicio(IdentificacionDomo id) {
        Domo domo = Buscar.domo(domoRepository, id);
        domo.declararFueraDeServicio();
        domoRepository.guardar(domo);
        return domo;
    }

    /** FUERA_DE_SERVICIO → PREPARADO, también decisión administrativa. */
    public Domo restablecerServicio(IdentificacionDomo id) {
        Domo domo = Buscar.domo(domoRepository, id);
        domo.restablecerServicio();
        domoRepository.guardar(domo);
        return domo;
    }

    // ------------------------------------------------------------------ bloqueos (7.3)

    /** RN-07: un bloqueo impide vender esas noches; no se registra sobre noches con reservas activas. */
    public Bloqueo registrarBloqueo(IdentificacionDomo id, Periodo periodo, MotivoBloqueo motivo, String observacion) {
        Domo domo = Buscar.domo(domoRepository, id);
        Bloqueo bloqueo = inventarioService.registrarBloqueo(new IdBloqueo(UUID.randomUUID().toString()), domo, periodo,
                motivo, observacion);
        bloqueoRepository.guardar(bloqueo);
        return bloqueo;
    }

    /** Levantar un bloqueo es su eliminación lógica: queda en el historial como no vigente. */
    public Bloqueo levantarBloqueo(IdentificacionDomo id, IdBloqueo idBloqueo) {
        Buscar.domo(domoRepository, id);
        Bloqueo bloqueo = Buscar.bloqueo(bloqueoRepository, idBloqueo);
        if (!bloqueo.getDomo().equals(id)) {
            throw new BloqueoNoEncontradoException(idBloqueo);
        }
        bloqueo.levantar();
        bloqueoRepository.guardar(bloqueo);
        return bloqueo;
    }

    private void aplicarTarifas(Domo domo, List<Tarifa> tarifas, LocalDate fecha) {
        if (tarifas == null) {
            return;
        }
        for (Tarifa tarifa : tarifas) {
            Buscar.temporada(temporadaRepository, tarifa.temporada());     // 404 si la temporada no existe
            domo.establecerTarifa(tarifa, fecha);
        }
    }
}
