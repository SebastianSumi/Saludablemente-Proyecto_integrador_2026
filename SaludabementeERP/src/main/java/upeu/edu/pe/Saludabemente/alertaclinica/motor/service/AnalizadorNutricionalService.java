package upeu.edu.pe.Saludabemente.alertaclinica.motor.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import upeu.edu.pe.Saludabemente.alertaclinica.alerta.entity.TipoEvaluacionOrigen;
import upeu.edu.pe.Saludabemente.alertaclinica.integracion.client.PersonalClient;
import upeu.edu.pe.Saludabemente.alertaclinica.integracion.dto.Hallazgo;
import upeu.edu.pe.Saludabemente.alertaclinica.integracion.dto.PacienteContextoDto;
import upeu.edu.pe.Saludabemente.alertaclinica.motor.service.evaluador.EvaluadorRegla;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.enums.SexoBiologico;
import upeu.edu.pe.Saludabemente.nutricional.api.EvaluacionNutricionalIngresoDto;
import upeu.edu.pe.Saludabemente.nutricional.api.MetricaNutricionalDto;

import java.math.BigDecimal;
import java.time.Period;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AnalizadorNutricionalService {

    private static final Logger log = LoggerFactory.getLogger(AnalizadorNutricionalService.class);

    private final PersonalClient personalClient;
    private final List<EvaluadorRegla> evaluadores;
    private final GeneradorAlertaService generadorAlertaService;

    @Transactional
    public void procesar(EvaluacionNutricionalIngresoDto dto) {
        if (dto.metricas() == null || dto.metricas().isEmpty()) {
            log.debug("Evaluacion nutricional {} sin metricas. Se omite.",
                    dto.idEvaluacionNutricional());
            return;
        }

        // 1. Obtener contexto del paciente
        PacienteContextoDto paciente = personalClient.obtenerContexto(dto.idPersona());

        // 2. Calcular edad a la fecha de evaluacion
        int edad = Period.between(paciente.fechaNacimiento(), dto.fechaEvaluacion()).getYears();

        // 3. String sexo -> enum
        SexoBiologico sexo = SexoBiologico.valueOf(paciente.sexo());

        // 4. List<Metrica> -> Map<String, BigDecimal>
        Map<String, BigDecimal> mapaMetricas = new LinkedHashMap<>();
        for (MetricaNutricionalDto m : dto.metricas()) {
            mapaMetricas.put(m.tipo().name(), m.valorMedido());
        }

        // 5. Invocar cada evaluador y acumular hallazgos
        List<Hallazgo> hallazgos = new ArrayList<>();
        for (EvaluadorRegla evaluador : evaluadores) {
            try {
                List<Hallazgo> parcial = evaluador.evaluar(sexo, edad, mapaMetricas);
                hallazgos.addAll(parcial);
            } catch (Exception e) {
                log.error("Evaluador {} fallo al procesar evaluacion {}: {}",
                        evaluador.getClass().getSimpleName(),
                        dto.idEvaluacionNutricional(),
                        e.getMessage());
            }
        }

        // 6. Delegar al generador
        generadorAlertaService.generar(
                dto.idPersona(),
                dto.idEvaluacionNutricional(),
                TipoEvaluacionOrigen.NUTRICIONAL,
                hallazgos
        );
    }
}