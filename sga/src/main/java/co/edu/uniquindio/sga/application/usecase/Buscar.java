package co.edu.uniquindio.sga.application.usecase;

import co.edu.uniquindio.sga.application.exception.BloqueoNoEncontradoException;
import co.edu.uniquindio.sga.application.exception.DomoNoEncontradoException;
import co.edu.uniquindio.sga.application.exception.FolioNoEncontradoException;
import co.edu.uniquindio.sga.application.exception.PoliticaCancelacionNoEncontradaException;
import co.edu.uniquindio.sga.application.exception.ReservaNoEncontradaException;
import co.edu.uniquindio.sga.application.exception.TemporadaNoEncontradaException;
import co.edu.uniquindio.sga.application.exception.UsuarioNoEncontradoException;
import co.edu.uniquindio.sga.domain.entity.Bloqueo;
import co.edu.uniquindio.sga.domain.entity.Domo;
import co.edu.uniquindio.sga.domain.entity.Folio;
import co.edu.uniquindio.sga.domain.entity.PoliticaCancelacion;
import co.edu.uniquindio.sga.domain.entity.Reserva;
import co.edu.uniquindio.sga.domain.entity.Temporada;
import co.edu.uniquindio.sga.domain.entity.Usuario;
import co.edu.uniquindio.sga.domain.repository.BloqueoRepository;
import co.edu.uniquindio.sga.domain.repository.DomoRepository;
import co.edu.uniquindio.sga.domain.repository.FolioRepository;
import co.edu.uniquindio.sga.domain.repository.PoliticaCancelacionRepository;
import co.edu.uniquindio.sga.domain.repository.ReservaRepository;
import co.edu.uniquindio.sga.domain.repository.TemporadaRepository;
import co.edu.uniquindio.sga.domain.repository.UsuarioRepository;
import co.edu.uniquindio.sga.domain.valueobject.CodigoReserva;
import co.edu.uniquindio.sga.domain.valueobject.IdBloqueo;
import co.edu.uniquindio.sga.domain.valueobject.IdTemporada;
import co.edu.uniquindio.sga.domain.valueobject.IdUsuario;
import co.edu.uniquindio.sga.domain.valueobject.IdentificacionDomo;

/** Búsquedas repetidas por los casos de uso, con su excepción de "no encontrado". */
final class Buscar {

    private Buscar() {
    }

    static Reserva reserva(ReservaRepository repositorio, CodigoReserva codigo) {
        return repositorio.obtenerPorCodigo(codigo).orElseThrow(() -> new ReservaNoEncontradaException(codigo));
    }

    static Domo domo(DomoRepository repositorio, IdentificacionDomo identificacion) {
        return repositorio.obtenerPorIdentificacion(identificacion)
                .orElseThrow(() -> new DomoNoEncontradoException(identificacion));
    }

    static Folio folio(FolioRepository repositorio, CodigoReserva codigo) {
        return repositorio.obtenerPorReserva(codigo).orElseThrow(() -> new FolioNoEncontradoException(codigo));
    }

    /** RN-13: la política congelada en la reserva, no la vigente hoy. */
    static Temporada temporada(TemporadaRepository repositorio, IdTemporada id) {
        return repositorio.obtenerPorId(id).orElseThrow(() -> new TemporadaNoEncontradaException(id));
    }

    static Bloqueo bloqueo(BloqueoRepository repositorio, IdBloqueo id) {
        return repositorio.obtenerPorId(id).orElseThrow(() -> new BloqueoNoEncontradoException(id));
    }

    static Usuario usuario(UsuarioRepository repositorio, IdUsuario id) {
        return repositorio.obtenerPorId(id).filter(Usuario::estaActivo)
                .orElseThrow(() -> new UsuarioNoEncontradoException(id));
    }

    static PoliticaCancelacion politicaCongelada(PoliticaCancelacionRepository repositorio, Reserva reserva) {
        return repositorio.obtenerPorVersion(reserva.getPoliticaCongelada())
                .orElseThrow(() -> new PoliticaCancelacionNoEncontradaException(
                        "versión " + reserva.getPoliticaCongelada().numero()));
    }
}
