package upeu.edu.pe.Saludabemente.nutricional.evaluacion;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import upeu.edu.pe.Saludabemente.nutricional.evaluacion.entity.EvaluacionNutricional;
import upeu.edu.pe.Saludabemente.nutricional.evaluacion.enums.EstadoEvaluacion;
import upeu.edu.pe.Saludabemente.nutricional.evaluacion.repository.EvaluacionRepository;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class EvaluacionRepositoryTest {

    @Autowired
    private EvaluacionRepository evaluacionRepository;

    @Test
    void guardaCabeceraYAsignaAuditoriaYEstadoPorDefecto() {
        EvaluacionNutricional evaluacion = new EvaluacionNutricional();
        evaluacion.setIdPersona(1L);
        evaluacion.setIdPeriodo("2026-2");
        evaluacion.setDocumentoIdentidad("12345678");
        evaluacion.setNombresCompletos("Lazaro Matallana Ana");
        evaluacion.setEdadAlMomento(46);
        evaluacion.setSexoAlMomento("F");
        evaluacion.setAreaTrabajoAlMomento("CRAI");

        EvaluacionNutricional guardada = evaluacionRepository.save(evaluacion);

        assertThat(guardada.getIdEvaluacion()).isNotNull();
        assertThat(guardada.getEstadoEvaluacion()).isEqualTo(EstadoEvaluacion.PENDIENTE);
        assertThat(guardada.getFechaEvaluacion()).isNotNull();
        assertThat(guardada.getFechaCreacion()).isNotNull();
        assertThat(guardada.getUsuarioCreacion()).isEqualTo("SYSTEM");
    }

    @Test
    void detectaEvaluacionActivaParaPersonaYPeriodo() {
        EvaluacionNutricional e = new EvaluacionNutricional();
        e.setIdPersona(2L);
        e.setIdPeriodo("2026-2");
        e.setDocumentoIdentidad("87654321");
        e.setNombresCompletos("Perez Juan");
        e.setEdadAlMomento(30);
        e.setSexoAlMomento("M");
        e.setAreaTrabajoAlMomento("TI");
        evaluacionRepository.save(e);

        assertThat(evaluacionRepository.existsActivaPorPersonaYPeriodo(2L, "2026-2", java.util.List.of(EstadoEvaluacion.PENDIENTE, EstadoEvaluacion.COMPLETA))).isTrue();
        assertThat(evaluacionRepository.existsActivaPorPersonaYPeriodo(99L, "2026-2", java.util.List.of(EstadoEvaluacion.PENDIENTE, EstadoEvaluacion.COMPLETA))).isFalse();
    }

    @Test
    void softDeleteMutaEstadoSinBorrarFila() {
        EvaluacionNutricional e = new EvaluacionNutricional();
        e.setIdPersona(3L);
        e.setIdPeriodo("2026-2");
        e.setDocumentoIdentidad("11223344");
        e.setNombresCompletos("Gomez Maria");
        e.setEdadAlMomento(25);
        e.setSexoAlMomento("F");
        e.setAreaTrabajoAlMomento("CRAI");
        EvaluacionNutricional guardada = evaluacionRepository.save(e);

        int filas = evaluacionRepository.softDelete(guardada.getIdEvaluacion(), EstadoEvaluacion.ANULADA);

        assertThat(filas).isEqualTo(1);
        assertThat(evaluacionRepository.findById(guardada.getIdEvaluacion()))
                .isPresent()
                .get()
                .extracting(EvaluacionNutricional::getEstadoEvaluacion)
                .isEqualTo(EstadoEvaluacion.ANULADA);
    }
}