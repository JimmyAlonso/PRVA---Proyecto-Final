package co.edu.uniquindio.sga.application.usecase;

import co.edu.uniquindio.sga.application.exception.ServicioAdicionalNoEncontradoException;
import co.edu.uniquindio.sga.domain.entity.Folio;
import co.edu.uniquindio.sga.domain.entity.Pago;
import co.edu.uniquindio.sga.domain.repository.AlojamientoRepository;
import co.edu.uniquindio.sga.domain.repository.FolioRepository;
import co.edu.uniquindio.sga.domain.valueobject.Cargo;
import co.edu.uniquindio.sga.domain.valueobject.CodigoReserva;
import co.edu.uniquindio.sga.domain.valueobject.ConceptoCargo;
import co.edu.uniquindio.sga.domain.valueobject.Dinero;
import co.edu.uniquindio.sga.domain.valueobject.IdPago;
import co.edu.uniquindio.sga.domain.valueobject.MedioPago;
import co.edu.uniquindio.sga.domain.valueobject.ServicioAdicional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

/**
 * 7.7 Folio, cargos y pagos: consultar el folio, registrar pagos parciales (con medio y fecha,
 * RN-15), registrar servicios adicionales (L-17) y el depósito exigido para el check-in (L-22).
 * Ningún movimiento se edita ni se borra (RN-16).
 */
@Service
@Transactional
public class GestionarFolioUseCase {

    private final FolioRepository folioRepository;
    private final AlojamientoRepository alojamientoRepository;

    public GestionarFolioUseCase(FolioRepository folioRepository, AlojamientoRepository alojamientoRepository) {
        this.folioRepository = folioRepository;
        this.alojamientoRepository = alojamientoRepository;
    }

    public Folio consultar(CodigoReserva codigo) {
        return Buscar.folio(folioRepository, codigo);
    }

    public Folio registrarPago(CodigoReserva codigo, Dinero monto, MedioPago medio, LocalDate fecha) {
        Folio folio = Buscar.folio(folioRepository, codigo);
        folio.registrarPago(new Pago(new IdPago(UUID.randomUUID().toString()), monto, medio, fecha));
        folioRepository.guardar(folio);
        return folio;
    }

    public Folio registrarServicio(CodigoReserva codigo, String codigoServicio, int cantidad, LocalDate fecha) {
        ServicioAdicional servicio = alojamientoRepository.obtener().buscarServicio(codigoServicio)
                .orElseThrow(() -> new ServicioAdicionalNoEncontradoException(codigoServicio));
        Folio folio = Buscar.folio(folioRepository, codigo);
        folio.registrarCargo(new Cargo(ConceptoCargo.SERVICIO, servicio.cargoPara(cantidad), fecha,
                servicio.nombre() + " x" + cantidad, false));
        folioRepository.guardar(folio);
        return folio;
    }

    /** L-22: el monto del depósito es configuración del alojamiento. */
    public Folio registrarDeposito(CodigoReserva codigo, LocalDate fecha) {
        Folio folio = Buscar.folio(folioRepository, codigo);
        folio.registrarDeposito(alojamientoRepository.obtener().getParametros().deposito(), fecha);
        folioRepository.guardar(folio);
        return folio;
    }

    /** Glosario: al liquidar la estancia sin daños, el depósito se devuelve (movimiento inverso). */
    public Folio devolverDeposito(CodigoReserva codigo, LocalDate fecha) {
        Folio folio = Buscar.folio(folioRepository, codigo);
        folio.devolverDeposito(fecha);
        folioRepository.guardar(folio);
        return folio;
    }

    /** RN-16: corrección de un cargo con un movimiento inverso, nunca una edición. */
    public Folio registrarCorreccion(CodigoReserva codigo, ConceptoCargo concepto, Dinero valor, String motivo,
                                     LocalDate fecha) {
        Folio folio = Buscar.folio(folioRepository, codigo);
        folio.registrarMovimientoInverso(new Cargo(concepto, valor, fecha, "Corrección: " + motivo, true));
        folioRepository.guardar(folio);
        return folio;
    }
}
