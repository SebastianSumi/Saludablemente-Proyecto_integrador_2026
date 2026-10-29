package upeu.edu.pe.Saludabemente.alertaclinica.motor.service.evaluador;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import upeu.edu.pe.Saludabemente.alertaclinica.integracion.dto.Hallazgo;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.enums.SexoBiologico;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.nutricional.entity.ReglaGlucosa;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.nutricional.repository.ReglaGlucosaRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class EvaluadorGlucosa implements EvaluadorRegla {

    private final ReglaGlucosaRepository repository;

    @Override
    public List<String> tiposSoportados() {
        return List.of("GLUCOSA");
    }

    @Override
    public List<Hallazgo> evaluar(SexoBiologico sexo, int edad, Map<String, BigDecimal> metricas) {
        BigDecimal valor = metricas.get("GLUCOSA");
        if (valor == null) {
            return List.of();
        }

        List<ReglaGlucosa> reglas = repository.findReglasActivasPorDemografia(sexo, edad);

        Optional<Hallazgo> hallazgo = reglas.stream()
                .filter(r -> valor.compareTo(r.getGlucosaMgDlMin()) >= 0
                        && valor.compareTo(r.getGlucosaMgDlMax()) <= 0)
                .findFirst()
                .filter(r -> r.getPuntosRiesgo() > 0)
                .map(r -> new Hallazgo(
                        "GLUCOSA",
                        valor,
                        r.getGlucosaMgDlMin(),
                        r.getGlucosaMgDlMax(),
                        r.getClasificacion().name(),
                        r.getPuntosRiesgo(),
                        String.format(Locale.ROOT, "Glucosa %.2f mg/dL -> %s",
                                valor.doubleValue(), r.getClasificacion().name()),
                        r.getIdReglaGlucosa(),
                        "ReglaGlucosa"
                ));

        return hallazgo.map(List::of).orElse(List.of());
    }
}