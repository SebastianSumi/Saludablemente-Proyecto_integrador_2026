package upeu.edu.pe.Saludabemente.alertaclinica.motor.service.evaluador;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import upeu.edu.pe.Saludabemente.alertaclinica.integracion.dto.Hallazgo;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.aptitud_fisica.entity.ReglaSaltaSoga;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.aptitud_fisica.repository.ReglaSaltaSogaRepository;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.enums.SexoBiologico;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class EvaluadorSaltaSoga implements EvaluadorRegla {

    private final ReglaSaltaSogaRepository repository;

    @Override
    public List<String> tiposSoportados() {
        return List.of("SALTA_SOGA");
    }

    @Override
    public List<Hallazgo> evaluar(SexoBiologico sexo, int edad, Map<String, BigDecimal> metricas) {
        BigDecimal valor = metricas.get("SALTA_SOGA");
        if (valor == null) {
            return List.of();
        }

        List<ReglaSaltaSoga> reglas = repository.findReglasActivasPorDemografia(sexo, edad);

        Optional<Hallazgo> hallazgo = reglas.stream()
                .filter(r -> valor.compareTo(BigDecimal.valueOf(r.getSaltosMin())) >= 0
                        && valor.compareTo(BigDecimal.valueOf(r.getSaltosMax())) <= 0)
                .findFirst()
                .filter(r -> r.getPuntosRiesgo() > 0)
                .map(r -> new Hallazgo(
                        "SALTA_SOGA",
                        valor,
                        BigDecimal.valueOf(r.getSaltosMin()),
                        BigDecimal.valueOf(r.getSaltosMax()),
                        r.getClasificacion().name(),
                        r.getPuntosRiesgo(),
                        String.format(Locale.ROOT, "Salta soga %.0f saltos -> %s",
                                valor.doubleValue(), r.getClasificacion().name()),
                        r.getIdReglaSaltaSoga(),
                        "ReglaSaltaSoga"
                ));

        return hallazgo.map(List::of).orElse(List.of());
    }
}