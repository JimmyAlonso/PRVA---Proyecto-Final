package co.edu.uniquindio.sga.infrastructure.rest;

import co.edu.uniquindio.sga.application.dto.request.LoginRequest;
import co.edu.uniquindio.sga.application.dto.request.RegistroHuespedRequest;
import co.edu.uniquindio.sga.application.dto.request.RestablecerContrasenaRequest;
import co.edu.uniquindio.sga.application.dto.request.SolicitarRecuperacionRequest;
import co.edu.uniquindio.sga.application.dto.response.MensajeResponse;
import co.edu.uniquindio.sga.application.dto.response.TokenResponse;
import co.edu.uniquindio.sga.application.dto.response.UsuarioResponse;
import co.edu.uniquindio.sga.application.usecase.AutenticarUsuarioUseCase;
import co.edu.uniquindio.sga.application.usecase.RecuperarContrasenaUseCase;
import co.edu.uniquindio.sga.application.usecase.RegistrarHuespedUseCase;
import co.edu.uniquindio.sga.domain.entity.Usuario;
import co.edu.uniquindio.sga.domain.valueobject.Correo;
import co.edu.uniquindio.sga.domain.valueobject.DocumentoIdentidad;
import co.edu.uniquindio.sga.infrastructure.rest.mapper.UsuarioRestMapper;
import co.edu.uniquindio.sga.infrastructure.security.JwtService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.time.Clock;
import java.time.LocalDateTime;

/** 7.2: registro público de huéspedes, inicio de sesión con JWT y recuperación de contraseña. */
@RestController
@RequestMapping("/api/v1/auth")
public class AutenticacionController {

    private final RegistrarHuespedUseCase registrarHuespedUseCase;
    private final AutenticarUsuarioUseCase autenticarUsuarioUseCase;
    private final RecuperarContrasenaUseCase recuperarContrasenaUseCase;
    private final JwtService jwtService;
    private final UsuarioRestMapper mapper;
    private final Clock reloj;

    public AutenticacionController(RegistrarHuespedUseCase registrarHuespedUseCase,
                                   AutenticarUsuarioUseCase autenticarUsuarioUseCase,
                                   RecuperarContrasenaUseCase recuperarContrasenaUseCase, JwtService jwtService,
                                   UsuarioRestMapper mapper, Clock reloj) {
        this.registrarHuespedUseCase = registrarHuespedUseCase;
        this.autenticarUsuarioUseCase = autenticarUsuarioUseCase;
        this.recuperarContrasenaUseCase = recuperarContrasenaUseCase;
        this.jwtService = jwtService;
        this.mapper = mapper;
        this.reloj = reloj;
    }

    /** 7.2: el registro público crea usuarios con rol HUESPED. */
    @PostMapping("/registro")
    public ResponseEntity<UsuarioResponse> registrar(@Valid @RequestBody RegistroHuespedRequest request) {
        Usuario usuario = registrarHuespedUseCase.ejecutar(request.nombre(), new Correo(request.correo()),
                request.contrasena(), request.telefono(), new DocumentoIdentidad(request.documento()),
                request.fechaNacimiento());
        return ResponseEntity.created(URI.create("/api/v1/usuarios/me")).body(mapper.aUsuario(usuario));
    }

    /** 7.2: emite un JWT con expiración. Credenciales incorrectas responden 401. */
    @PostMapping("/login")
    public ResponseEntity<TokenResponse> iniciarSesion(@Valid @RequestBody LoginRequest request) {
        Usuario usuario = autenticarUsuarioUseCase.ejecutar(new Correo(request.correo()), request.contrasena());
        JwtService.TokenEmitido token = jwtService.emitir(usuario);
        return ResponseEntity.ok(new TokenResponse(token.token(), "Bearer", token.expiraEn(), mapper.aUsuario(usuario)));
    }

    /** 7.2: envía un código de un solo uso al correo registrado. Responde igual aunque el correo no exista. */
    @PostMapping("/recuperacion")
    public ResponseEntity<MensajeResponse> solicitarRecuperacion(@Valid @RequestBody SolicitarRecuperacionRequest request) {
        recuperarContrasenaUseCase.solicitarCodigo(new Correo(request.correo()), LocalDateTime.now(reloj));
        return ResponseEntity.ok(new MensajeResponse(
                "Si el correo está registrado, recibirá un código de recuperación válido por 15 minutos."));
    }

    @PostMapping("/recuperacion/restablecer")
    public ResponseEntity<MensajeResponse> restablecer(@Valid @RequestBody RestablecerContrasenaRequest request) {
        recuperarContrasenaUseCase.restablecer(new Correo(request.correo()), request.codigo(), request.contrasenaNueva(),
                LocalDateTime.now(reloj));
        return ResponseEntity.ok(new MensajeResponse("La contraseña se restableció. Ya puede iniciar sesión."));
    }
}
