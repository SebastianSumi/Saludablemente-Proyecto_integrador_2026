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
import upeu.edu.pe.Saludabemente.aptitudfisica.api.EvaluacionFisicaIngresoDto;
import upeu.edu.pe.Saludabemente.aptitudfisica.api.MetricaFisicaDto;

import java.math.BigDecimal;
import java.time.Period;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AnalizadorFisicoService {

    private static final Logger log = LoggerFactory.getLogger(AnalizadorFisicoService.class);

    private final PersonalClient personalClient;
    private final List<EvaluadorRegla> evaluadores;
    private final GeneradorAlertaService generadorAlertaService;

    @Transactional
    public void procesar(EvaluacionFisicaIngresoDto dto) {
        if (dto.metricas() == null || dto.metricas().isEmpty()) {
            log.debug("Evaluacion fisica {} sin metricas. Se omite.",
                    dto.idEvaluacionFisica());
            return;
        }

        // 1. Contexto del paciente
        PacienteContextoDto paciente = personalClient.obtenerContexto(dto.idPersona());

        // 2. Edad a la fecha de evaluacion
        int edad = Period.between(paciente.fechaNacimiento(), dto.fechaEvaluacion()).getYears();

        // 3. String sexo -> enum
        SexoBiologico sexo = SexoBiologico.valueOf(paciente.sexo());

        // 4. List<Metrica> -> Map
        Map<String, BigDecimal> mapaMetricas = new LinkedHashMap<>();
        for (MetricaFisicaDto m : dto.metricas()) {
            mapaMetricas.put(m.tipo().name(), m.valorMedido());
        }

        // 5. Evaluadores
        List<Hallazgo> hallazgos = new ArrayList<>();
        for (EvaluadorRegla evaluador : evaluadores) {
            try {
                List<Hallazgo> parcial = evaluador.evaluar(sexo, edad, mapaMetricas);
                hallazgos.addAll(parcial);
            } catch (Exception e) {
                log.error("Evaluador {} fallo al procesar evaluacion fisica {}: {}",
                        evaluador.getClass().getSimpleName(),
                        dto.idEvaluacionFisica(),
                        e.getMessage());
            }
        }

        // 6. Delegar al generador
        generadorAlertaService.generar(
                dto.idPersona(),
                dto.idEvaluacionFisica(),
                TipoEvaluacionOrigen.APTITUD_FISICA,
                hallazgos
        );
    }
}