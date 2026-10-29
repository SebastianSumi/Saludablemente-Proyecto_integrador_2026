package upeu.edu.pe.Saludabemente.alertaclinica.motor.service.evaluador;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import upeu.edu.pe.Saludabemente.alertaclinica.integracion.dto.Hallazgo;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.enums.SexoBiologico;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.nutricional.entity.ReglaGrasaVisceral;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.nutricional.repository.ReglaGrasaVisceralRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class EvaluadorGrasaVisceral implements EvaluadorRegla {

    private final ReglaGrasaVisceralRepository repository;

    @Override
    public List<String> tiposSoportados() {
        return List.of("GRASA_VISCERAL");
    }

    @Override
    public List<Hallazgo> evaluar(SexoBiologico sexo, int edad, Map<String, BigDecimal> metricas) {
        BigDecimal valor = metricas.get("GRASA_VISCERAL");
        if (valor == null) {
            return List.of();
        }

        List<ReglaGrasaVisceral> reglas = repository.findReglasActivasPorDemografia(sexo, edad);

        Optional<Hallazgo> hallazgo = reglas.stream()
                .filter(r -> valor.compareTo(r.getNivelMin()) >= 0
                        && valor.compareTo(r.getNivelMax()) <= 0)
                .findFirst()
                .filter(r -> r.getPuntosRiesgo() > 0)
                .map(r -> new Hallazgo(
                        "GRASA_VISCERAL",
                        valor,
                        r.getNivelMin(),
                        r.getNivelMax(),
                        r.getClasificacion().name(),
                        r.getPuntosRiesgo(),
                        String.format(Locale.ROOT, "Grasa visceral nivel %.2f -> %s",
                                valor.doubleValue(), r.getClasificacion().name()),
                        r.getIdReglaVisceral(),
                        "ReglaGrasaVisceral"
                ));

        return hallazgo.map(List::of).orElse(List.of());
    }
}