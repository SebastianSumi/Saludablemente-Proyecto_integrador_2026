package upeu.edu.pe.Saludabemente.alertaclinica.motor.service.evaluador;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import upeu.edu.pe.Saludabemente.alertaclinica.integracion.dto.Hallazgo;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.enums.SexoBiologico;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.nutricional.entity.ReglaPorcentajeGrasa;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.nutricional.repository.ReglaPorcentajeGrasaRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class EvaluadorPorcentajeGrasa implements EvaluadorRegla {

    private final ReglaPorcentajeGrasaRepository repository;

    @Override
    public List<String> tiposSoportados() {
        return List.of("GRASA_CORPORAL");
    }

    @Override
    public List<Hallazgo> evaluar(SexoBiologico sexo, int edad, Map<String, BigDecimal> metricas) {
        BigDecimal valor = metricas.get("GRASA_CORPORAL");
        if (valor == null) {
            return List.of();
        }

        List<ReglaPorcentajeGrasa> reglas = repository.findReglasActivasPorDemografia(sexo, edad);

        Optional<Hallazgo> hallazgo = reglas.stream()
                .filter(r -> valor.compareTo(r.getPorcentajeMin()) >= 0
                        && valor.compareTo(r.getPorcentajeMax()) <= 0)
                .findFirst()
                .filter(r -> r.getPuntosRiesgo() > 0)
                .map(r -> new Hallazgo(
                        "GRASA_CORPORAL",
                        valor,
                        r.getPorcentajeMin(),
                        r.getPorcentajeMax(),
                        r.getClasificacion().name(),
                        r.getPuntosRiesgo(),
                        String.format(Locale.ROOT, "Grasa corporal %.2f%% -> %s",
                                valor.doubleValue(), r.getClasificacion().name()),
                        r.getIdReglaGrasa(),
                        "ReglaPorcentajeGrasa"
                ));

        return hallazgo.map(List::of).orElse(List.of());
    }
}