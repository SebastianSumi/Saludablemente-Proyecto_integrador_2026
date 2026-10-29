package upeu.edu.pe.Saludabemente.alertaclinica.motor.service.evaluador;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import upeu.edu.pe.Saludabemente.alertaclinica.integracion.dto.Hallazgo;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.aptitud_fisica.entity.ReglaCarrera400m;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.aptitud_fisica.repository.ReglaCarrera400mRepository;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.enums.SexoBiologico;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class EvaluadorCarrera400m implements EvaluadorRegla {

    private final ReglaCarrera400mRepository repository;

    @Override
    public List<String> tiposSoportados() {
        return List.of("CARRERA_400M");
    }

    @Override
    public List<Hallazgo> evaluar(SexoBiologico sexo, int edad, Map<String, BigDecimal> metricas) {
        BigDecimal valor = metricas.get("CARRERA_400M");
        if (valor == null) {
            return List.of();
        }

        List<ReglaCarrera400m> reglas = repository.findReglasActivasPorDemografia(sexo, edad);

        Optional<Hallazgo> hallazgo = reglas.stream()
                .filter(r -> valor.compareTo(BigDecimal.valueOf(r.getTiempoSegundosMin())) >= 0
                        && valor.compareTo(BigDecimal.valueOf(r.getTiempoSegundosMax())) <= 0)
                .findFirst()
                .filter(r -> r.getPuntosRiesgo() > 0)
                .map(r -> new Hallazgo(
                        "CARRERA_400M",
                        valor,
                        BigDecimal.valueOf(r.getTiempoSegundosMin()),
                        BigDecimal.valueOf(r.getTiempoSegundosMax()),
                        r.getClasificacion().name(),
                        r.getPuntosRiesgo(),
                        String.format(Locale.ROOT, "Carrera 400m %.0f seg -> %s",
                                valor.doubleValue(), r.getClasificacion().name()),
                        r.getIdReglaCarrera(),
                        "ReglaCarrera400m"
                ));

        return hallazgo.map(List::of).orElse(List.of());
    }
}