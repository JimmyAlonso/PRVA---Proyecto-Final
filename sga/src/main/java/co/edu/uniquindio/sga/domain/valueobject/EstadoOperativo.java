package co.edu.uniquindio.sga.domain.valueobject;

// Sección 3.3 y 7.6: condición física presente del domo
public enum EstadoOperativo {
    PREPARADO,
    OCUPADO,
    PENDIENTE_PREPARACION,
    EN_PREPARACION,
    FUERA_DE_SERVICIO;

    /** RN-11: un domo solo puede recibir un grupo si su estado operativo es PREPARADO. */
    public boolean permiteRegistro() {
        return this == PREPARADO;
    }
}
