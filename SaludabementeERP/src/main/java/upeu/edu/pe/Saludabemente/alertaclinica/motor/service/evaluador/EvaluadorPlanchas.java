package upeu.edu.pe.Saludabemente.alertaclinica.motor.service.evaluador;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import upeu.edu.pe.Saludabemente.alertaclinica.integracion.dto.Hallazgo;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.aptitud_fisica.entity.ReglaPlanchas;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.aptitud_fisica.repository.ReglaPlanchasRepository;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.enums.SexoBiologico;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class EvaluadorPlanchas implements EvaluadorRegla {

    private final ReglaPlanchasRepository repository;

    @Override
    public List<String> tiposSoportados() {
        return List.of("PLANCHAS");
    }

    @Override
    public List<Hallazgo> evaluar(SexoBiologico sexo, int edad, Map<String, BigDecimal> metricas) {
        BigDecimal valor = metricas.get("PLANCHAS");
        if (valor == null) {
            return List.of();
        }

        List<ReglaPlanchas> reglas = repository.findReglasActivasPorDemografia(sexo, edad);

        Optional<Hallazgo> hallazgo = reglas.stream()
                .filter(r -> valor.compareTo(BigDecimal.valueOf(r.getRepeticionesMin())) >= 0
                        && valor.compareTo(BigDecimal.valueOf(r.getRepeticionesMax())) <= 0)
                .findFirst()
                .filter(r -> r.getPuntosRiesgo() > 0)
                .map(r -> new Hallazgo(
                        "PLANCHAS",
                        valor,
                        BigDecimal.valueOf(r.getRepeticionesMin()),
                        BigDecimal.valueOf(r.getRepeticionesMax()),
                        r.getClasificacion().name(),
                        r.getPuntosRiesgo(),
                        String.format(Locale.ROOT, "Planchas %.0f reps -> %s",
                                valor.doubleValue(), r.getClasificacion().name()),
                        r.getIdReglaPlanchas(),
                        "ReglaPlanchas"
                ));

        return hallazgo.map(List::of).orElse(List.of());
    }
}