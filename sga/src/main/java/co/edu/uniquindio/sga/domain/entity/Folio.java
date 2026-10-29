package co.edu.uniquindio.sga.domain.entity;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.valueobject.AutorizacionCierre;
import co.edu.uniquindio.sga.domain.valueobject.Cargo;
import co.edu.uniquindio.sga.domain.valueobject.CodigoFolio;
import co.edu.uniquindio.sga.domain.valueobject.CodigoReserva;
import co.edu.uniquindio.sga.domain.valueobject.ConceptoCargo;
import co.edu.uniquindio.sga.domain.valueobject.Deposito;
import co.edu.uniquindio.sga.domain.valueobject.Dinero;
import co.edu.uniquindio.sga.domain.valueobject.Saldo;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Raíz del agregado Folio (F-08, 7.7): cuenta de la reserva que acumula cargos y pagos.
 *
 * <p>RN-16: los cargos y pagos no se modifican ni se eliminan; toda corrección es un
 * movimiento inverso. No existen métodos para eliminar un cargo ni para modificar un pago.
 */
public class Folio {

    private final CodigoFolio codigo;
    private final CodigoReserva reserva;
    // Colecciones internas siempre de solo lectura; toda modificación reemplaza la referencia
    private List<Cargo> cargos = List.of();
    private List<Pago> pagos = List.of();
    private boolean cerrado;
    private AutorizacionCierre autorizacionCierre;

    public Folio(CodigoFolio codigo, CodigoReserva reserva) {
        if (codigo == null) {
            throw new ReglaDominioException("El folio debe tener un código.");
        }
        if (reserva == null) {
            throw new ReglaDominioException("El folio debe indicar la reserva a la que pertenece.");
        }
        this.codigo = codigo;
        this.reserva = reserva;
        this.cerrado = false;
    }

    /** Rehidrata un folio ya persistido con sus movimientos en el orden en que se registraron. */
    public static Folio reconstruir(CodigoFolio codigo, CodigoReserva reserva, List<Cargo> cargos, List<Pago> pagos,
                                    boolean cerrado, AutorizacionCierre autorizacionCierre) {
        Folio folio = new Folio(codigo, reserva);
        folio.cargos = cargos == null ? List.of() : List.copyOf(cargos);
        folio.pagos = pagos == null ? List.of() : List.copyOf(pagos);
        folio.cerrado = cerrado;
        folio.autorizacionCierre = autorizacionCierre;
        return folio;
    }

    /** 7.7: el folio se abre al crear la reserva, con el cargo de alojamiento ya calculado. */
    public static Folio abrir(CodigoReserva reserva, Dinero valorAlojamiento, LocalDate fecha) {
        if (reserva == null) {
            throw new ReglaDominioException("El folio debe indicar la reserva a la que pertenece.");
        }
        Folio folio = new Folio(new CodigoFolio("FOL-" + reserva.valor()), reserva);
        folio.registrarCargo(new Cargo(ConceptoCargo.ALOJAMIENTO, valorAlojamiento, fecha,
                "Alojamiento de la reserva " + reserva.valor(), false));
        return folio;
    }

    /** RN-16: un cargo solo se agrega, nunca se modifica ni se elimina. */
    public void registrarCargo(Cargo cargo) {
        if (cargo == null) {
            throw new ReglaDominioException("El cargo a registrar es obligatorio.");
        }
        verificarAbierto();
        if (cargo.esInverso()) {
            throw new ReglaDominioException("Un movimiento inverso se registra con registrarMovimientoInverso.");
        }
        if (cargo.valor().esCero()) {
            throw new ReglaDominioException("No se registran cargos por valor cero.");
        }
        cargos = agregar(cargos, cargo);
    }

    /** RN-16: la corrección es un cargo nuevo marcado como inverso, jamás una edición. */
    public void registrarMovimientoInverso(Cargo inverso) {
        if (inverso == null) {
            throw new ReglaDominioException("El movimiento inverso a registrar es obligatorio.");
        }
        verificarAbierto();
        if (!inverso.esInverso()) {
            throw new ReglaDominioException("El movimiento debe estar marcado como inverso.");
        }
        if (inverso.valor().esMayorQue(netoDe(inverso.concepto()))) {
            throw new ReglaDominioException("El movimiento inverso no puede superar lo cargado por "
                    + inverso.concepto() + ".");
        }
        cargos = agregar(cargos, inverso);
    }

    /** RN-15: todo pago se registra contra el folio con su medio y su fecha. */
    public void registrarPago(Pago pago) {
        if (pago == null) {
            throw new ReglaDominioException("El pago a registrar es obligatorio.");
        }
        verificarAbierto();
        pagos = agregar(pagos, pago);
    }

    /**
     * RN-14 y 7.5: la diferencia entre el valor anterior y el recalculado se registra como
     * ajuste, positivo (cargo) o negativo (movimiento inverso).
     */
    public void registrarAjuste(Dinero valorAnterior, Dinero valorNuevo, LocalDate fecha, String motivo) {
        if (valorNuevo.esMayorQue(valorAnterior)) {
            registrarCargo(new Cargo(ConceptoCargo.AJUSTE, valorNuevo.restar(valorAnterior), fecha,
                    "Ajuste por modificación: " + motivo, false));
        } else if (valorAnterior.esMayorQue(valorNuevo)) {
            verificarAbierto();
            // Ajuste negativo: movimiento inverso de AJUSTE (lo que baja es el alojamiento ya cargado)
            Dinero diferencia = valorAnterior.restar(valorNuevo);
            cargos = agregar(cargos, new Cargo(ConceptoCargo.AJUSTE, diferencia, fecha,
                    "Ajuste por modificación (a favor): " + motivo, true));
        }
    }

    /**
     * RN-13 y RN-16: cuando la reserva termina sin estancia (cancelación, no-show o vencimiento),
     * se revierte el valor del alojamiento con un movimiento inverso y la retención queda como
     * penalidad. Si el huésped había pagado más que la retención, el saldo queda a favor
     * (negativo): esa es la devolución que se registra (3.4: el desembolso ocurre fuera).
     */
    public void liquidarTerminacion(Dinero valorAlojamiento, Dinero retencion, LocalDate fecha, String motivo) {
        if (retencion == null) {
            throw new ReglaDominioException("La retención a registrar es obligatoria.");
        }
        verificarAbierto();
        BigDecimal netoAlojamiento = netoDe(ConceptoCargo.ALOJAMIENTO).valor()
                .add(netoConSigno(ConceptoCargo.AJUSTE));
        Dinero aRevertir = netoAlojamiento.signum() > 0 ? new Dinero(netoAlojamiento) : Dinero.CERO;
        if (!aRevertir.esCero()) {
            cargos = agregar(cargos, new Cargo(ConceptoCargo.ALOJAMIENTO, aRevertir, fecha,
                    "Reverso de alojamiento: " + motivo, true));
        }
        if (!retencion.esCero()) {
            registrarCargo(new Cargo(ConceptoCargo.PENALIDAD, retencion, fecha, "Retención: " + motivo, false));
        }
    }

    /** L-22: registra el depósito exigido por el alojamiento antes del check-in. */
    public void registrarDeposito(Deposito deposito, LocalDate fecha) {
        if (deposito == null) {
            throw new ReglaDominioException("El depósito es obligatorio.");
        }
        if (tieneDepositoRegistrado()) {
            throw new ReglaDominioException("El folio ya tiene el depósito registrado.");
        }
        registrarCargo(new Cargo(ConceptoCargo.DEPOSITO, deposito.monto(), fecha,
                "Depósito de garantía (L-22)", false));
    }

    /** L-22: devolución total del depósito al salir, como movimiento inverso (RN-16). */
    public void devolverDeposito(LocalDate fecha) {
        Dinero neto = netoDe(ConceptoCargo.DEPOSITO);
        if (neto.esCero()) {
            throw new ReglaDominioException("El folio no tiene depósito vigente para devolver.");
        }
        registrarMovimientoInverso(new Cargo(ConceptoCargo.DEPOSITO, neto, fecha, "Devolución del depósito", true));
    }

    /** L-22: el depósito consta en el folio y no fue devuelto. */
    public boolean tieneDepositoRegistrado() {
        return !netoDe(ConceptoCargo.DEPOSITO).esCero();
    }

    private Dinero netoDe(ConceptoCargo concepto) {
        BigDecimal neto = netoConSigno(concepto);
        return neto.signum() > 0 ? new Dinero(neto) : Dinero.CERO;
    }

    private BigDecimal netoConSigno(ConceptoCargo concepto) {
        return cargos.stream()
                .filter(cargo -> cargo.concepto() == concepto)
                .map(Cargo::valorConSigno)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public Dinero totalCargos() {
        BigDecimal total = cargos.stream().map(Cargo::valorConSigno).reduce(BigDecimal.ZERO, BigDecimal::add);
        return total.signum() > 0 ? new Dinero(total) : Dinero.CERO;
    }

    public Dinero totalPagos() {
        return pagos.stream().map(Pago::getMonto).reduce(Dinero.CERO, Dinero::sumar);
    }

    /**
     * RN-15: el saldo es la diferencia entre cargos y pagos; valor derivado, nunca almacenado.
     * Corrección: antes devolvía {@link Dinero}, que no admite negativos, y el saldo a favor
     * (7.7) lanzaba excepción.
     */
    public Saldo saldo() {
        return Saldo.de(totalCargos(), totalPagos());
    }

    /** Anticipo: cuánto se ha pagado frente a un monto exigido. */
    public boolean pagosCubren(Dinero monto) {
        return !totalPagos().esMenorQue(monto);
    }

    /** RN-17: el folio no puede cerrarse con saldo distinto de cero sin autorización explícita. */
    public boolean puedeCerrarseSinAutorizacion() {
        return saldo().esCero();
    }

    public void cerrar(AutorizacionCierre autorizacion) {
        verificarAbierto();
        // RN-17: con saldo distinto de cero se exige autorización explícita registrada (autor y motivo)
        if (!saldo().esCero()) {
            if (autorizacion == null) {
                throw new ReglaDominioException("El folio tiene saldo de " + saldo().enPesos()
                        + " COP: no puede cerrarse sin autorización explícita del administrador.");
            }
            this.autorizacionCierre = autorizacion;
        }
        this.cerrado = true;
    }

    private void verificarAbierto() {
        if (cerrado) {
            throw new ReglaDominioException("El folio está cerrado y no admite movimientos.");
        }
    }

    private static <T> List<T> agregar(List<T> lista, T elemento) {
        List<T> actualizada = new ArrayList<>(lista);
        actualizada.add(elemento);
        return List.copyOf(actualizada);
    }

    public CodigoFolio getCodigo() {
        return codigo;
    }

    public CodigoReserva getReserva() {
        return reserva;
    }

    public List<Cargo> getCargos() {
        return cargos;
    }

    public List<Pago> getPagos() {
        return pagos;
    }

    public boolean isCerrado() {
        return cerrado;
    }

    public AutorizacionCierre getAutorizacionCierre() {
        return autorizacionCierre;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Folio folio)) return false;
        return Objects.equals(codigo, folio.codigo);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(codigo);
    }
}
