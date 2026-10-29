package co.edu.uniquindio.sga.infrastructure.rest;

import co.edu.uniquindio.sga.application.dto.Pagina;
import co.edu.uniquindio.sga.application.dto.request.ActualizarPerfilRequest;
import co.edu.uniquindio.sga.application.dto.request.AsignarRolRequest;
import co.edu.uniquindio.sga.application.dto.request.CambiarContrasenaRequest;
import co.edu.uniquindio.sga.application.dto.request.CrearUsuarioRequest;
import co.edu.uniquindio.sga.application.dto.response.MensajeResponse;
import co.edu.uniquindio.sga.application.dto.response.UsuarioResponse;
import co.edu.uniquindio.sga.application.usecase.AdministrarUsuariosUseCase;
import co.edu.uniquindio.sga.application.usecase.GestionarPerfilUseCase;
import co.edu.uniquindio.sga.domain.entity.Usuario;
import co.edu.uniquindio.sga.domain.valueobject.Correo;
import co.edu.uniquindio.sga.domain.valueobject.DocumentoIdentidad;
import co.edu.uniquindio.sga.domain.valueobject.IdUsuario;
import co.edu.uniquindio.sga.infrastructure.rest.mapper.UsuarioRestMapper;
import co.edu.uniquindio.sga.infrastructure.security.UsuarioAutenticado;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

/**
 * 7.2 Perfil propio (/me, cualquier usuario autenticado) y 7.1 gestión de usuarios y roles
 * (administrador). La eliminación es lógica: el usuario queda marcado como eliminado.
 */
@RestController
@RequestMapping("/api/v1/usuarios")
public class UsuarioController {

    private final GestionarPerfilUseCase perfilUseCase;
    private final AdministrarUsuariosUseCase administrarUseCase;
    private final UsuarioRestMapper mapper;

    public UsuarioController(GestionarPerfilUseCase perfilUseCase, AdministrarUsuariosUseCase administrarUseCase,
                             UsuarioRestMapper mapper) {
        this.perfilUseCase = perfilUseCase;
        this.administrarUseCase = administrarUseCase;
        this.mapper = mapper;
    }

    // ------------------------------------------------------------------ perfil propio

    @GetMapping("/me")
    public ResponseEntity<UsuarioResponse> miPerfil(@AuthenticationPrincipal UsuarioAutenticado usuario) {
        return ResponseEntity.ok(mapper.aUsuario(perfilUseCase.consultar(new IdUsuario(usuario.id()))));
    }

    @PutMapping("/me")
    public ResponseEntity<UsuarioResponse> actualizarPerfil(@AuthenticationPrincipal UsuarioAutenticado usuario,
                                                            @Valid @RequestBody ActualizarPerfilRequest request) {
        return ResponseEntity.ok(mapper.aUsuario(perfilUseCase.actualizar(new IdUsuario(usuario.id()), request.nombre(),
                request.telefono(), request.fotoPerfil())));
    }

    @PutMapping("/me/contrasena")
    public ResponseEntity<MensajeResponse> cambiarContrasena(@AuthenticationPrincipal UsuarioAutenticado usuario,
                                                             @Valid @RequestBody CambiarContrasenaRequest request) {
        perfilUseCase.cambiarContrasena(new IdUsuario(usuario.id()), request.contrasenaActual(), request.contrasenaNueva());
        return ResponseEntity.ok(new MensajeResponse("La contraseña se cambió correctamente."));
    }

    // ------------------------------------------------------------------ administración (7.1)

    @GetMapping
    public ResponseEntity<Pagina<UsuarioResponse>> listar(@RequestParam(defaultValue = "0") int pagina) {
        return ResponseEntity.ok(administrarUseCase.listar(pagina).mapear(mapper::aUsuario));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponse> obtener(@PathVariable String id) {
        return ResponseEntity.ok(mapper.aUsuario(administrarUseCase.consultar(new IdUsuario(id))));
    }

    /** 7.2: los roles internos solo los asigna un administrador. */
    @PostMapping
    public ResponseEntity<UsuarioResponse> crear(@Valid @RequestBody CrearUsuarioRequest request) {
        Usuario usuario = administrarUseCase.crear(request.nombre(), new Correo(request.correo()), request.contrasena(),
                request.telefono(), new DocumentoIdentidad(request.documento()), request.fechaNacimiento(), request.rol());
        return ResponseEntity.created(URI.create("/api/v1/usuarios/" + usuario.getId().valor()))
                .body(mapper.aUsuario(usuario));
    }

    @PutMapping("/{id}/rol")
    public ResponseEntity<UsuarioResponse> asignarRol(@PathVariable String id, @Valid @RequestBody AsignarRolRequest request) {
        return ResponseEntity.ok(mapper.aUsuario(administrarUseCase.asignarRol(new IdUsuario(id), request.rol())));
    }

    /** 7.2: eliminación lógica; no procede si el usuario es titular de reservas activas (409). */
    @DeleteMapping("/{id}")
    public ResponseEntity<MensajeResponse> eliminar(@PathVariable String id) {
        administrarUseCase.eliminar(new IdUsuario(id));
        return ResponseEntity.ok(new MensajeResponse("El usuario quedó eliminado (eliminación lógica)."));
    }
}
