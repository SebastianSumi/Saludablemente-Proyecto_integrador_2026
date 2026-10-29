package upeu.edu.pe.Saludabemente.alertaclinica.motor.service.evaluador;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import upeu.edu.pe.Saludabemente.alertaclinica.integracion.dto.Hallazgo;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.enums.SexoBiologico;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.nutricional.entity.ReglaTrigliceridos;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.nutricional.repository.ReglaTrigliceridosRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class EvaluadorTrigliceridos implements EvaluadorRegla {

    private final ReglaTrigliceridosRepository repository;

    @Override
    public List<String> tiposSoportados() {
        return List.of("TRIGLICERIDOS");
    }

    @Override
    public List<Hallazgo> evaluar(SexoBiologico sexo, int edad, Map<String, BigDecimal> metricas) {
        BigDecimal valor = metricas.get("TRIGLICERIDOS");
        if (valor == null) {
            return List.of();
        }

        List<ReglaTrigliceridos> reglas = repository.findReglasActivasPorDemografia(sexo, edad);

        Optional<Hallazgo> hallazgo = reglas.stream()
                .filter(r -> valor.compareTo(r.getTrigliceridosMgDlMin()) >= 0
                        && valor.compareTo(r.getTrigliceridosMgDlMax()) <= 0)
                .findFirst()
                .filter(r -> r.getPuntosRiesgo() > 0)
                .map(r -> new Hallazgo(
                        "TRIGLICERIDOS",
                        valor,
                        r.getTrigliceridosMgDlMin(),
                        r.getTrigliceridosMgDlMax(),
                        r.getClasificacion().name(),
                        r.getPuntosRiesgo(),
                        String.format(Locale.ROOT, "Trigliceridos %.2f mg/dL -> %s",
                                valor.doubleValue(), r.getClasificacion().name()),
                        r.getIdReglaTrigliceridos(),
                        "ReglaTrigliceridos"
                ));

        return hallazgo.map(List::of).orElse(List.of());
    }
}