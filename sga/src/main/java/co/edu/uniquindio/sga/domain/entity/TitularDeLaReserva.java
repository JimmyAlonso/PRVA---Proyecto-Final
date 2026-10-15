package co.edu.uniquindio.sga.domain.entity;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.valueobject.DocumentoIdentidad;

import java.util.Objects;

public class TitularDeLaReserva {
        private final DocumentoIdentidad documento;
        private String nombreCompleto, correo, telefono;

        public TitularDeLaReserva(DocumentoIdentidad documento, String nombreCompleto, String correo, String telefono) {
            if (documento == null) throw new ReglaDominioException("El titular debe tener documento de identidad.");
            if (nombreCompleto == null || nombreCompleto.isBlank()) throw new ReglaDominioException("El titular debe tener nombre completo.");
            if (correo == null || !correo.contains("@")) throw new ReglaDominioException("El titular debe tener un correo válido.");
            if (telefono == null || telefono.isBlank()) throw new ReglaDominioException("El titular debe tener teléfono.");
            this.documento = documento;
            this.nombreCompleto = nombreCompleto;
            this.correo = correo.trim();
            this.telefono = telefono.trim();
        }

        public void actualizarContacto(String correo, String telefono) {
            if (correo == null || !correo.contains("@")) throw new ReglaDominioException("El correo no es válido.");
            if (telefono == null || telefono.isBlank()) throw new ReglaDominioException("El teléfono es obligatorio.");
            this.correo = correo.trim();
            this.telefono = telefono.trim();
        }

        public DocumentoIdentidad getDocumento() { return documento; }
        public String getNombreCompleto() { return nombreCompleto; }
        public String getCorreo() { return correo; }
        public String getTelefono() { return telefono; }

        @Override public boolean equals(Object o) {
            return this == o || (o instanceof TitularDeLaReserva t && Objects.equals(documento, t.documento));
        }
        @Override public int hashCode() { return Objects.hashCode(documento); }
    }
