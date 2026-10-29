package co.edu.uniquindio.sga.application.usecase;

import co.edu.uniquindio.sga.application.exception.PoliticaCancelacionNoEncontradaException;
import co.edu.uniquindio.sga.domain.entity.PoliticaCancelacion;
import co.edu.uniquindio.sga.domain.repository.PoliticaCancelacionRepository;
import co.edu.uniquindio.sga.domain.valueobject.Porcentaje;
import co.edu.uniquindio.sga.domain.valueobject.TramoRetencion;
import co.edu.uniquindio.sga.domain.valueobject.VersionPolitica;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * F-07 y 7.4: una sola política vigente, versionada. Actualizarla publica una versión nueva y
 * conserva las anteriores, porque cada reserva sigue ligada a la versión que congeló (RN-13).
 */
@Service
@Transactional
public class GestionarPoliticaCancelacionUseCase {

    private final PoliticaCancelacionRepository politicaRepository;

    public GestionarPoliticaCancelacionUseCase(PoliticaCancelacionRepository politicaRepository) {
        this.politicaRepository = politicaRepository;
    }

    @Transactional(readOnly = true)
    public PoliticaCancelacion vigente(LocalDate fecha) {
        return politicaRepository.obtenerVigenteEn(fecha)
                .orElseThrow(() -> new PoliticaCancelacionNoEncontradaException("vigente en " + fecha));
    }

    @Transactional(readOnly = true)
    public List<PoliticaCancelacion> versiones() {
        return politicaRepository.listarVersiones();
    }

    @Transactional(readOnly = true)
    public PoliticaCancelacion version(int numero) {
        return politicaRepository.listarVersiones().stream()
                .filter(politica -> politica.getVersion().numero() == numero)
                .findFirst()
                .orElseThrow(() -> new PoliticaCancelacionNoEncontradaException("versión " + numero));
    }

    public PoliticaCancelacion publicarVersion(List<TramoRetencion> tramos, Porcentaje retencionNoShow,
                                               LocalDate vigenteDesde) {
        PoliticaCancelacion nueva = politicaRepository.obtenerUltima()
                .map(ultima -> ultima.nuevaVersion(tramos, retencionNoShow, vigenteDesde))
                .orElseGet(() -> new PoliticaCancelacion(new VersionPolitica(1, vigenteDesde), tramos, retencionNoShow));
        politicaRepository.guardar(nueva);
        return nueva;
    }
}
