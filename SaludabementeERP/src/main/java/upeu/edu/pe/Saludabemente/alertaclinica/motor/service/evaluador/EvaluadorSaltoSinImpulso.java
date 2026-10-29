package upeu.edu.pe.Saludabemente.alertaclinica.motor.service.evaluador;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import upeu.edu.pe.Saludabemente.alertaclinica.integracion.dto.Hallazgo;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.aptitud_fisica.entity.ReglaSaltoSinImpulso;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.aptitud_fisica.repository.ReglaSaltoSinImpulsoRepository;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.enums.SexoBiologico;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class EvaluadorSaltoSinImpulso implements EvaluadorRegla {

    private final ReglaSaltoSinImpulsoRepository repository;

    @Override
    public List<String> tiposSoportados() {
        return List.of("SALTO_SIN_IMPULSO");
    }

    @Override
    public List<Hallazgo> evaluar(SexoBiologico sexo, int edad, Map<String, BigDecimal> metricas) {
        BigDecimal valor = metricas.get("SALTO_SIN_IMPULSO");
        if (valor == null) {
            return List.of();
        }

        List<ReglaSaltoSinImpulso> reglas = repository.findReglasActivasPorDemografia(sexo, edad);

        Optional<Hallazgo> hallazgo = reglas.stream()
                .filter(r -> valor.compareTo(r.getDistanciaCmMin()) >= 0
                        && valor.compareTo(r.getDistanciaCmMax()) <= 0)
                .findFirst()
                .filter(r -> r.getPuntosRiesgo() > 0)
                .map(r -> new Hallazgo(
                        "SALTO_SIN_IMPULSO",
                        valor,
                        r.getDistanciaCmMin(),
                        r.getDistanciaCmMax(),
                        r.getClasificacion().name(),
                        r.getPuntosRiesgo(),
                        String.format(Locale.ROOT, "Salto sin impulso %.2f cm -> %s",
                                valor.doubleValue(), r.getClasificacion().name()),
                        r.getIdReglaSalto(),
                        "ReglaSaltoSinImpulso"
                ));

        return hallazgo.map(List::of).orElse(List.of());
    }
}