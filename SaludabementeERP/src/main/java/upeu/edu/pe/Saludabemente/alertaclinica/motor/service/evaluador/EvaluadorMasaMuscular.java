package upeu.edu.pe.Saludabemente.alertaclinica.motor.service.evaluador;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import upeu.edu.pe.Saludabemente.alertaclinica.integracion.dto.Hallazgo;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.enums.SexoBiologico;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.nutricional.entity.ReglaMasaMuscular;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.nutricional.repository.ReglaMasaMuscularRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class EvaluadorMasaMuscular implements EvaluadorRegla {

    private final ReglaMasaMuscularRepository repository;

    @Override
    public List<String> tiposSoportados() {
        return List.of("MASA_MUSCULAR");
    }

    @Override
    public List<Hallazgo> evaluar(SexoBiologico sexo, int edad, Map<String, BigDecimal> metricas) {
        BigDecimal valor = metricas.get("MASA_MUSCULAR");
        if (valor == null) {
            return List.of();
        }

        List<ReglaMasaMuscular> reglas = repository.findReglasActivasPorDemografia(sexo, edad);

        Optional<Hallazgo> hallazgo = reglas.stream()
                .filter(r -> valor.compareTo(r.getPorcentajeMin()) >= 0
                        && valor.compareTo(r.getPorcentajeMax()) <= 0)
                .findFirst()
                .filter(r -> r.getPuntosRiesgo() > 0)
                .map(r -> new Hallazgo(
                        "MASA_MUSCULAR",
                        valor,
                        r.getPorcentajeMin(),
                        r.getPorcentajeMax(),
                        r.getClasificacion().name(),
                        r.getPuntosRiesgo(),
                        String.format(Locale.ROOT, "Masa muscular %.2f%% -> %s",
                                valor.doubleValue(), r.getClasificacion().name()),
                        r.getIdReglaMusculo(),
                        "ReglaMasaMuscular"
                ));

        return hallazgo.map(List::of).orElse(List.of());
    }
}