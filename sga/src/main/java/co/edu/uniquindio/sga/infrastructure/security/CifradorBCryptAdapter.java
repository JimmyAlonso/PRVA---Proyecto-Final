package co.edu.uniquindio.sga.infrastructure.security;

import co.edu.uniquindio.sga.domain.port.CifradorContrasenas;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/** 7.2: adaptador del puerto CifradorContrasenas con BCrypt (el PasswordEncoder de SeguridadConfig). */
@Component
public class CifradorBCryptAdapter implements CifradorContrasenas {

    private final PasswordEncoder passwordEncoder;

    public CifradorBCryptAdapter(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public String cifrar(String contrasenaPlana) {
        return passwordEncoder.encode(contrasenaPlana);
    }

    @Override
    public boolean coincide(String contrasenaPlana, String contrasenaCifrada) {
        return contrasenaPlana != null && contrasenaCifrada != null
                && passwordEncoder.matches(contrasenaPlana, contrasenaCifrada);
    }
}
