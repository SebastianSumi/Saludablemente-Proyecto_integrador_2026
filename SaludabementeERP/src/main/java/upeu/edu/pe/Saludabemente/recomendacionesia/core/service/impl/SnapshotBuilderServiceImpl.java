package upeu.edu.pe.Saludabemente.recomendacionesia.core.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import upeu.edu.pe.Saludabemente.aptitudfisica.api.AptitudFisicaDto;
import upeu.edu.pe.Saludabemente.aptitudfisica.api.RecomendacionesAptitudFisicaApiService;
import upeu.edu.pe.Saludabemente.nutricional.api.AntropometricoDto;
import upeu.edu.pe.Saludabemente.nutricional.api.BioquimicoDto;
import upeu.edu.pe.Saludabemente.nutricional.api.EvaluacionHistoricaDto;
import upeu.edu.pe.Saludabemente.nutricional.api.EvaluacionNutricionalDto;
import upeu.edu.pe.Saludabemente.nutricional.api.RecomendacionesNutricionalApiService;
import upeu.edu.pe.Saludabemente.personal.api.PersonaRecomendacionesDTO;
import upeu.edu.pe.Saludabemente.personal.api.PersonalRecomendacionesApiService;
import upeu.edu.pe.Saludabemente.recomendacionesia.core.dto.SnapshotDto;
import upeu.edu.pe.Saludabemente.recomendacionesia.core.service.SnapshotBuilderService;
import upeu.edu.pe.Saludabemente.recomendacionesia.exception.DatosInsuficientesException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SnapshotBuilderServiceImpl implements SnapshotBuilderService {

    private final RecomendacionesNutricionalApiService nutricionalApi;
    private final RecomendacionesAptitudFisicaApiService aptitudFisicaApi;
    private final PersonalRecomendacionesApiService personalApi;

    @Override
    public SnapshotDto construir(Long idEvaluacion, Long idPersona) {

        log.info("Armando snapshot para evaluacion={} persona={}", idEvaluacion, idPersona);

        // 1. Datos demográficos
        PersonaRecomendacionesDTO persona = personalApi.obtenerDatosDemograficos(idPersona)
                .orElseThrow(() -> new DatosInsuficientesException(
                        "No existe persona con id " + idPersona));

        if (Boolean.FALSE.equals(persona.activo())) {
            throw new DatosInsuficientesException(
                    "La persona " + idPersona + " está inactiva");
        }

        // 2. Cabecera de evaluación
        EvaluacionNutricionalDto cabecera = nutricionalApi.obtenerCabecera(idEvaluacion);
        LocalDate fechaEval = cabecera.fechaEvaluacion();

        int edad = Period.between(persona.fechaNacimiento(), fechaEval).getYears();

        // 3. Antropometría
        AntropometricoDto antropometria = nutricionalApi.obtenerAntropometria(idEvaluacion);

        // 4. Bioquímica (opcional)
        Optional<BioquimicoDto> bioquimicaOpt = nutricionalApi.obtenerBioquimica(idEvaluacion);

        // 5. Aptitud física (opcional)
        Optional<AptitudFisicaDto> aptitudOpt =
                aptitudFisicaApi.obtenerPorPersonaYFecha(idPersona, fechaEval);

        // 6. Histórico (opcional)
        Optional<EvaluacionHistoricaDto> historicoOpt =
                nutricionalApi.obtenerEvaluacionAnterior(idPersona, fechaEval);

        // 7. Armar SnapshotDto
        SnapshotDto.SnapshotDtoBuilder builder = SnapshotDto.builder()
                .idPersona(idPersona)
                .nombres(armarNombreCompleto(persona))
                .sexo(persona.sexo())
                .fechaNacimiento(persona.fechaNacimiento())
                .edad(edad)
                .idEvaluacion(idEvaluacion)
                .fechaEvaluacion(fechaEval)
                .periodoSemestral(cabecera.periodoSemestral())
                // Antropometría
                .pesoKg(aStr(antropometria.pesoKg()))
                .estaturaCm(aStr(antropometria.estaturaCm()))
                .imc(aStr(antropometria.imc()))
                .dxImc(antropometria.dxImc())
                .perimetroAbdominalCm(aStr(antropometria.perimetroAbdominalCm()))
                .dxPerimetroAbdominal(antropometria.dxPerimetroAbdominal())
                .porcentajeGrasa(aStr(antropometria.porcentajeGrasa()))
                .dxGrasa(antropometria.dxGrasa())
                .porcentajeMasaMuscular(aStr(antropometria.porcentajeMasaMuscular()))
                .dxMasaMuscular(antropometria.dxMasaMuscular())
                .porcentajeGrasaVisceral(aStr(antropometria.porcentajeGrasaVisceral()))
                .dxGrasaVisceral(antropometria.dxGrasaVisceral());

        // Bioquímica
        bioquimicaOpt.ifPresent(bq -> builder
                .glucosa(aStr(bq.glucosa()))
                .colesterol(aStr(bq.colesterol()))
                .trigliceridos(aStr(bq.trigliceridos()))
                .presionSistolica(bq.presionSistolica())
                .presionDiastolica(bq.presionDiastolica())
                .dxBioquimico(bq.dxBioquimico()));

        // Aptitud física
        if (aptitudOpt.isPresent()) {
            AptitudFisicaDto af = aptitudOpt.get();
            builder
                    .abdominales30s(af.abdominales30s())
                    .planchas30s(af.planchas30s())
                    .saltaSoga30s(af.saltaSoga30s())
                    .saltoSinImpulsoCm(aStr(af.saltoSinImpulsoCm()))
                    .carrera400mSeg(af.carrera400mSeg())
                    .aptitudFisicaCompleta(true);
        } else {
            builder.aptitudFisicaCompleta(false);
        }

        // Histórico
        if (historicoOpt.isPresent()) {
            EvaluacionHistoricaDto hist = historicoOpt.get();
            builder.tieneHistorico(true)
                    .periodoSemestralAnterior(hist.cabecera().periodoSemestral())
                    .fechaEvaluacionAnterior(hist.cabecera().fechaEvaluacion());

            if (hist.antropometria() != null) {
                AntropometricoDto aAnt = hist.antropometria();
                builder
                        .pesoKgAnterior(aStr(aAnt.pesoKg()))
                        .imcAnterior(aStr(aAnt.imc()))
                        .dxImcAnterior(aAnt.dxImc())
                        .porcentajeGrasaAnterior(aStr(aAnt.porcentajeGrasa()))
                        .dxGrasaAnterior(aAnt.dxGrasa())
                        .porcentajeMasaMuscularAnterior(aStr(aAnt.porcentajeMasaMuscular()))
                        .dxMasaMuscularAnterior(aAnt.dxMasaMuscular())
                        .porcentajeGrasaVisceralAnterior(aStr(aAnt.porcentajeGrasaVisceral()))
                        .dxGrasaVisceralAnterior(aAnt.dxGrasaVisceral());
            }

            if (hist.bioquimica() != null) {
                BioquimicoDto bAnt = hist.bioquimica();
                builder
                        .glucosaAnterior(aStr(bAnt.glucosa()))
                        .colesterolAnterior(aStr(bAnt.colesterol()))
                        .trigliceridosAnterior(aStr(bAnt.trigliceridos()))
                        .presionSistolicaAnterior(bAnt.presionSistolica())
                        .presionDiastolicaAnterior(bAnt.presionDiastolica())
                        .dxBioquimicoAnterior(bAnt.dxBioquimico());
            }
        } else {
            builder.tieneHistorico(false);
        }

        SnapshotDto snapshot = builder.build();
        log.info("Snapshot armado: historico={} aptitudFisica={}",
                snapshot.isTieneHistorico(), snapshot.isAptitudFisicaCompleta());
        return snapshot;
    }

    private String aStr(BigDecimal bd) {
        return bd == null ? null : bd.toPlainString();
    }

    private String armarNombreCompleto(PersonaRecomendacionesDTO p) {
        StringBuilder sb = new StringBuilder();
        if (p.nombres() != null) sb.append(p.nombres());
        if (p.apellidoPaterno() != null) sb.append(" ").append(p.apellidoPaterno());
        if (p.apellidoMaterno() != null) sb.append(" ").append(p.apellidoMaterno());
        return sb.toString().trim();
    }
}