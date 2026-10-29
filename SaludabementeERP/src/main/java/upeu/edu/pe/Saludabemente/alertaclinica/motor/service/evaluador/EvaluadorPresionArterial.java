package upeu.edu.pe.Saludabemente.alertaclinica.motor.service.evaluador;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import upeu.edu.pe.Saludabemente.alertaclinica.integracion.dto.Hallazgo;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.enums.SexoBiologico;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.nutricional.entity.ReglaPresionArterial;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.nutricional.repository.ReglaPresionArterialRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * Evaluador especial: ReglaPresionArterial tiene dos pares de rangos
 * (sistolica y diastolica) en una sola regla.
 *
 * Para elegir la regla correcta hay que cruzar AMBOS valores.
 * Si solo uno de los dos valores esta fuera de rango, se genera
 * UN unico hallazgo por la regla completa.
 */
@Component
@RequiredArgsConstructor
public class EvaluadorPresionArterial implements EvaluadorRegla {

    private final ReglaPresionArterialRepository repository;

    @Override
    public List<String> tiposSoportados() {
        return List.of("PRESION_SISTOLICA", "PRESION_DIASTOLICA");
    }

    @Override
    public List<Hallazgo> evaluar(SexoBiologico sexo, int edad, Map<String, BigDecimal> metricas) {
        BigDecimal sistolica = metricas.get("PRESION_SISTOLICA");
        BigDecimal diastolica = metricas.get("PRESION_DIASTOLICA");

        // Si falta cualquiera de los dos, no se puede evaluar correctamente.
        if (sistolica == null || diastolica == null) {
            return List.of();
        }

        List<ReglaPresionArterial> reglas = repository.findReglasActivasPorDemografia(sexo, edad);

        Optional<Hallazgo> hallazgo = reglas.stream()
                .filter(r -> sistolica.compareTo(BigDecimal.valueOf(r.getSistolicaMmHgMin())) >= 0
                        && sistolica.compareTo(BigDecimal.valueOf(r.getSistolicaMmHgMax())) <= 0
                        && diastolica.compareTo(BigDecimal.valueOf(r.getDiastolicaMmHgMin())) >= 0
                        && diastolica.compareTo(BigDecimal.valueOf(r.getDiastolicaMmHgMax())) <= 0)
                .findFirst()
                .filter(r -> r.getPuntosRiesgo() > 0)
                .map(r -> new Hallazgo(
                        "PRESION_ARTERIAL",
                        sistolica,
                        BigDecimal.valueOf(r.getSistolicaMmHgMin()),
                        BigDecimal.valueOf(r.getSistolicaMmHgMax()),
                        r.getClasificacion().name(),
                        r.getPuntosRiesgo(),
                        String.format(Locale.ROOT,
                                "Presion arterial %d/%d mmHg -> %s",
                                sistolica.intValue(), diastolica.intValue(),
                                r.getClasificacion().name()),
                        r.getIdReglaPresion(),
                        "ReglaPresionArterial"
                ));

        return hallazgo.map(List::of).orElse(List.of());
    }
}