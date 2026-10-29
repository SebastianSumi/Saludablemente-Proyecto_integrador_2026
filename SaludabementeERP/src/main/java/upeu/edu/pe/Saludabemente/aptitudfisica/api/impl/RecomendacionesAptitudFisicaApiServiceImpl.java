package upeu.edu.pe.Saludabemente.aptitudfisica.api.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import upeu.edu.pe.Saludabemente.aptitudfisica.api.AptitudFisicaDto;
import upeu.edu.pe.Saludabemente.aptitudfisica.api.RecomendacionesAptitudFisicaApiService;
import upeu.edu.pe.Saludabemente.aptitudfisica.evaluacion.entity.EvaluacionAptitud;
import upeu.edu.pe.Saludabemente.aptitudfisica.evaluacion.enums.EstadoEvaluacion;
import upeu.edu.pe.Saludabemente.aptitudfisica.evaluacion.repository.EvaluacionAptitudRepository;

import java.time.LocalDate;
import java.util.Optional;

/**
 * Implementación de {@link RecomendacionesAptitudFisicaApiService}.
 * Vive en {@code api/impl/} (sub-paquete NO expuesto) para que Modulith
 * no la publique: los consumidores solo ven la interfaz.
 */
@Service
@RequiredArgsConstructor
public class RecomendacionesAptitudFisicaApiServiceImpl
        implements RecomendacionesAptitudFisicaApiService {

    private final EvaluacionAptitudRepository repository;

    @Override
    @Transactional(readOnly = true)
    public Optional<AptitudFisicaDto> obtenerPorPersonaYFecha(Long idPersona, LocalDate fecha) {
        return repository
                .findFirstByIdPersonaAndFechaEvaluacionOrderByIdDesc(idPersona, fecha)
                .map(this::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existePruebaFisica(Long idPersona, Long idPeriodo) {
        return repository.existsByIdPersonaAndIdPeriodoAndEstadoEvaluacion(
                idPersona, idPeriodo, EstadoEvaluacion.COMPLETA);
    }

    // -----------------------------------------------------------------
    // Mapeo interno
    // -----------------------------------------------------------------

    private AptitudFisicaDto toDto(EvaluacionAptitud e) {
        Integer abdominales = null;
        Integer planchas = null;
        java.math.BigDecimal salto = null;
        Integer soga = null;
        Integer carrera = null;

        if (e.getDetalleFuerza() != null) {
            var f = e.getDetalleFuerza();
            abdominales = f.getRepeticionesAbdominales();
            planchas = f.getRepeticionesFlexionBrazo();
            salto = f.getSaltoLongitudCm();
        }
        if (e.getDetalleCardio() != null) {
            var c = e.getDetalleCardio();
            soga = c.getRepeticionesSoga30s();
            carrera = c.getTiempoCarrera400mSeg();
        }

        return new AptitudFisicaDto(
                abdominales,
                planchas,
                soga,
                salto,
                carrera,
                e.getFechaEvaluacion()
        );
    }
}