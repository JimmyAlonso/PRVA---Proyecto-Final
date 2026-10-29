package co.edu.uniquindio.sga.domain.service;

import co.edu.uniquindio.sga.domain.entity.Usuario;
import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.repository.ReservaRepository;
import co.edu.uniquindio.sga.domain.repository.UsuarioRepository;
import co.edu.uniquindio.sga.domain.valueobject.Correo;
import co.edu.uniquindio.sga.domain.valueobject.DocumentoIdentidad;

/**
 * Reglas de 7.2 que ningún usuario puede evaluar solo: la unicidad del correo y del documento
 * (necesitan ver a los demás usuarios) y la eliminación, que depende de las reservas del titular.
 */
public class GestionUsuariosService {

    private final UsuarioRepository usuarioRepository;
    private final ReservaRepository reservaRepository;

    public GestionUsuariosService(UsuarioRepository usuarioRepository, ReservaRepository reservaRepository) {
        if (usuarioRepository == null || reservaRepository == null) {
            throw new ReglaDominioException("Los repositorios de usuarios y reservas son obligatorios.");
        }
        this.usuarioRepository = usuarioRepository;
        this.reservaRepository = reservaRepository;
    }

    /** 7.2: el correo es único; un documento no puede tener dos cuentas activas. */
    public void verificarRegistro(Correo correo, DocumentoIdentidad documento) {
        if (usuarioRepository.existeCorreo(correo)) {
            throw new ReglaDominioException("Ya existe un usuario registrado con el correo " + correo.valor() + ".");
        }
        if (usuarioRepository.existeDocumentoActivo(documento)) {
            throw new ReglaDominioException("Ya existe una cuenta activa con el documento " + documento.valor() + ".");
        }
    }

    /**
     * 7.2: no puede eliminarse un usuario que sea titular de reservas PENDIENTE, CONFIRMADA o
     * EN_CURSO. Eliminarlo no elimina al titular ni sus reservas históricas.
     */
    public void eliminar(Usuario usuario) {
        if (usuario == null) {
            throw new ReglaDominioException("El usuario es obligatorio.");
        }
        if (reservaRepository.existenActivasDeTitular(usuario.getDocumento())) {
            throw new ReglaDominioException("No se puede eliminar el usuario: es titular de reservas activas.");
        }
        usuario.eliminar();
    }
}
