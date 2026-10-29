package co.edu.uniquindio.sga;

import co.edu.uniquindio.sga.domain.port.CifradorContrasenas;

/** Implementación alternativa del puerto de cifrado para pruebas (I-08): no usa BCrypt. */
public class CifradorFalso implements CifradorContrasenas {

    @Override
    public String cifrar(String contrasenaPlana) {
        return "{cifrada}" + contrasenaPlana;
    }

    @Override
    public boolean coincide(String contrasenaPlana, String contrasenaCifrada) {
        return contrasenaCifrada != null && contrasenaCifrada.equals(cifrar(contrasenaPlana));
    }
}
