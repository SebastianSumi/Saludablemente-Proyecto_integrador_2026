package upeu.edu.pe.Saludabemente.alertaclinica.motor.service.evaluador;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import upeu.edu.pe.Saludabemente.alertaclinica.integracion.dto.Hallazgo;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.enums.SexoBiologico;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.nutricional.entity.ReglaPerimetroAbdominal;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.nutricional.repository.ReglaPerimetroAbdominalRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class EvaluadorPerimetroAbdominal implements EvaluadorRegla {

    private final ReglaPerimetroAbdominalRepository repository;

    @Override
    public List<String> tiposSoportados() {
        return List.of("PERIMETRO_ABDOMINAL");
    }

    @Override
    public List<Hallazgo> evaluar(SexoBiologico sexo, int edad, Map<String, BigDecimal> metricas) {
        BigDecimal valor = metricas.get("PERIMETRO_ABDOMINAL");
        if (valor == null) {
            return List.of();
        }

        List<ReglaPerimetroAbdominal> reglas = repository.findReglasActivasPorDemografia(sexo, edad);

        Optional<Hallazgo> hallazgo = reglas.stream()
                .filter(r -> valor.compareTo(r.getPerimetroCmMin()) >= 0
                        && valor.compareTo(r.getPerimetroCmMax()) <= 0)
                .findFirst()
                .filter(r -> r.getPuntosRiesgo() > 0)
                .map(r -> new Hallazgo(
                        "PERIMETRO_ABDOMINAL",
                        valor,
                        r.getPerimetroCmMin(),
                        r.getPerimetroCmMax(),
                        r.getClasificacion().name(),
                        r.getPuntosRiesgo(),
                        String.format(Locale.ROOT, "Perimetro abdominal %.2f cm -> %s",
                                valor.doubleValue(), r.getClasificacion().name()),
                        r.getIdReglaPerimetro(),
                        "ReglaPerimetroAbdominal"
                ));

        return hallazgo.map(List::of).orElse(List.of());
    }
}