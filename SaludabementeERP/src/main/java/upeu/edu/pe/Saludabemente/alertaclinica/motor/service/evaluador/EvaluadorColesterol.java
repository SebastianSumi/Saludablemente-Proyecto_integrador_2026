package upeu.edu.pe.Saludabemente.alertaclinica.motor.service.evaluador;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import upeu.edu.pe.Saludabemente.alertaclinica.integracion.dto.Hallazgo;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.enums.SexoBiologico;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.nutricional.entity.ReglaColesterol;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.nutricional.repository.ReglaColesterolRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class EvaluadorColesterol implements EvaluadorRegla {

    private final ReglaColesterolRepository repository;

    @Override
    public List<String> tiposSoportados() {
        return List.of("COLESTEROL");
    }

    @Override
    public List<Hallazgo> evaluar(SexoBiologico sexo, int edad, Map<String, BigDecimal> metricas) {
        BigDecimal valor = metricas.get("COLESTEROL");
        if (valor == null) {
            return List.of();
        }

        List<ReglaColesterol> reglas = repository.findReglasActivasPorDemografia(sexo, edad);

        Optional<Hallazgo> hallazgo = reglas.stream()
                .filter(r -> valor.compareTo(r.getColesterolMgDlMin()) >= 0
                        && valor.compareTo(r.getColesterolMgDlMax()) <= 0)
                .findFirst()
                .filter(r -> r.getPuntosRiesgo() > 0)
                .map(r -> new Hallazgo(
                        "COLESTEROL",
                        valor,
                        r.getColesterolMgDlMin(),
                        r.getColesterolMgDlMax(),
                        r.getClasificacion().name(),
                        r.getPuntosRiesgo(),
                        String.format(Locale.ROOT, "Colesterol %.2f mg/dL -> %s",
                                valor.doubleValue(), r.getClasificacion().name()),
                        r.getIdReglaColesterol(),
                        "ReglaColesterol"
                ));

        return hallazgo.map(List::of).orElse(List.of());
    }
}