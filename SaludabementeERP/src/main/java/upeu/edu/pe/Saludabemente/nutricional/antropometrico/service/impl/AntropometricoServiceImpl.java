package upeu.edu.pe.Saludabemente.nutricional.antropometrico.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import upeu.edu.pe.Saludabemente.exception.BusinessRuleException;
import upeu.edu.pe.Saludabemente.exception.ResourceNotFoundException;
import upeu.edu.pe.Saludabemente.nutricional.antropometrico.dto.AntropometricoDiagnosticosDto;
import upeu.edu.pe.Saludabemente.nutricional.antropometrico.dto.request.AntropometricoRequest;
import upeu.edu.pe.Saludabemente.nutricional.antropometrico.dto.response.AntropometricoResponse;
import upeu.edu.pe.Saludabemente.nutricional.antropometrico.entity.DetalleAntropometrico;
import upeu.edu.pe.Saludabemente.nutricional.antropometrico.mapper.AntropometricoMapper;
import upeu.edu.pe.Saludabemente.nutricional.antropometrico.repository.AntropometricoRepository;
import upeu.edu.pe.Saludabemente.nutricional.antropometrico.service.AntropometricoService;
import upeu.edu.pe.Saludabemente.nutricional.evaluacion.entity.EvaluacionNutricional;
import upeu.edu.pe.Saludabemente.nutricional.evaluacion.enums.EstadoEvaluacion;
import upeu.edu.pe.Saludabemente.nutricional.evaluacion.repository.EvaluacionRepository;
import upeu.edu.pe.Saludabemente.nutricional.evaluacion.service.EvaluacionService;
import upeu.edu.pe.Saludabemente.nutricional.util.OmronRulesUtil;
import upeu.edu.pe.Saludabemente.personal.persona.dto.PersonaSnapshotClinicoDTO;
import upeu.edu.pe.Saludabemente.personal.persona.service.PersonaService;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class AntropometricoServiceImpl implements AntropometricoService {

    private final AntropometricoRepository antropometricoRepository;
    private final AntropometricoMapper antropometricoMapper;
    private final EvaluacionService evaluacionService;
    private final PersonaService personaService;
    private final EvaluacionRepository evaluacionRepository;

    // ===================================================================
    // Comandos
    // ===================================================================

    @Override
    @Transactional
    public AntropometricoResponse registrar(Long idEvaluacion, AntropometricoRequest request) {
        // 1. Obtener la cabecera (valida existencia con 404 si no está).
        EvaluacionNutricional evaluacion = evaluacionService.obtenerOFallar(idEvaluacion);

        // 2. RN-07: solo se puede registrar antropometría si está PENDIENTE.
        if (evaluacion.getEstadoEvaluacion() != EstadoEvaluacion.PENDIENTE) {
            throw new BusinessRuleException(
                    "Solo se puede registrar antropometría en evaluaciones PENDIENTES. "
                            + "Estado actual: " + evaluacion.getEstadoEvaluacion());
        }

        // 3. Defensa en profundidad: límites físicos plausibles.
        validarLimitesFisicos(request);

        // 4. Unicidad 1:1: no debe existir antropometría previa.
        if (antropometricoRepository.existsByEvaluacion_IdEvaluacion(idEvaluacion)) {
            throw new BusinessRuleException(
                    "La evaluación " + idEvaluacion + " ya tiene antropometría registrada");
        }

        // 5. Calcular diagnósticos OMRON con el snapshot congelado.
        AntropometricoDiagnosticosDto diagnosticos =
                calcularDiagnosticos(request, evaluacion);

        // 6. Mapear y vincular.
        DetalleAntropometrico detalle =
                antropometricoMapper.toEntity(request, diagnosticos);
        evaluacion.asignarAntropometrico(detalle);

        DetalleAntropometrico guardado = antropometricoRepository.save(detalle);

        // 7. Evaluar si la evaluación ya está completa (antropometría + bioquímico).
        evaluacionService.evaluarCompletitudYDisparar(idEvaluacion);

        return antropometricoMapper.toResponse(guardado);
    }

    @Override
    @Transactional
    public AntropometricoResponse recalcular(Long idEvaluacion) {
        DetalleAntropometrico detalle = antropometricoRepository
                .findByEvaluacion_IdEvaluacion(idEvaluacion)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No existe antropometría para la evaluación " + idEvaluacion));

        EvaluacionNutricional evaluacion = detalle.getEvaluacion();
        if (evaluacion.getEstadoEvaluacion() == EstadoEvaluacion.ANULADA) {
            throw new BusinessRuleException(
                    "No se puede recalcular una evaluación anulada");
        }

        // 1. RE-LEER el snapshot desde Personal.
        //    Esto trae los valores ACTUALES, no los congelados.
        PersonaSnapshotClinicoDTO snapshotActual =
                personaService.obtenerSnapshotClinico(evaluacion.getIdPersona());

        // 2. Refrescar los campos congelados en la cabecera.
        //    Es la excepción consciente al principio de snapshot: cuando RRHH
        //    corrige el origen, la evaluación se actualiza.
        evaluacion.setEdadAlMomento(snapshotActual.edadAlMomento());
        evaluacion.setSexoAlMomento(snapshotActual.sexo());
        evaluacion.setNombresCompletos(snapshotActual.nombresCompletos());
        evaluacion.setAreaTrabajoAlMomento(snapshotActual.areaTrabajo());
        evaluacion.setDocumentoIdentidad(snapshotActual.documentoIdentidad());
        // Nota: idPeriodo NO se toca — ese sí es histórico puro.

        // 3. Reconstruir el request desde las medidas crudas.
        AntropometricoRequest request = new AntropometricoRequest(
                detalle.getEstaturaCm(),
                detalle.getPesoKg(),
                detalle.getPerimetroAbdominalCm(),
                detalle.getPorcentajeGrasa(),
                detalle.getPorcentajeMasaMuscular(),
                detalle.getPorcentajeGrasaVisceral()
        );

        // 4. Recalcular con los datos actualizados.
        AntropometricoDiagnosticosDto diagnosticos = calcularDiagnosticos(request, evaluacion);
        antropometricoMapper.actualizarDiagnosticos(diagnosticos, detalle);

        // 5. Guardar cabecera (con snapshot refrescado) y detalle.
        evaluacionRepository.save(evaluacion);
        DetalleAntropometrico guardado = antropometricoRepository.save(detalle);

        return antropometricoMapper.toResponse(guardado);
    }

    // ===================================================================
    // Helpers privados
    // ===================================================================

    /**
     * Motor OMRON. Usa SIEMPRE el snapshot congelado en la cabecera,
     * no los datos actuales de la persona.
     */
    private AntropometricoDiagnosticosDto calcularDiagnosticos(AntropometricoRequest request,
                                                               EvaluacionNutricional evaluacion) {
        String sexo = evaluacion.getSexoAlMomento();
        Integer edad = evaluacion.getEdadAlMomento();

        BigDecimal imc = OmronRulesUtil.calcularImc(
                request.pesoKg(), request.estaturaCm());

        return new AntropometricoDiagnosticosDto(
                imc,
                OmronRulesUtil.diagnosticarImc(imc),
                OmronRulesUtil.diagnosticarPerimetroAbdominal(
                        request.perimetroAbdominalCm(), sexo),
                OmronRulesUtil.diagnosticarGrasa(
                        request.porcentajeGrasa(), sexo, edad),
                OmronRulesUtil.diagnosticarMasaMuscular(
                        request.porcentajeMasaMuscular(), sexo, edad),
                OmronRulesUtil.diagnosticarGrasaVisceral(
                        request.porcentajeGrasaVisceral())
        );
    }

    /**
     * RN-03 y RN-04: rangos clínicamente plausibles.
     * Defensa en profundidad: el Controller ya valida con Bean Validation,
     * pero el Service también puede recibir llamadas internas (Seeder,
     * importación masiva) que no pasan por el Controller.
     */
    private void validarLimitesFisicos(AntropometricoRequest request) {
        if (request.pesoKg().doubleValue() < 20.0
                || request.pesoKg().doubleValue() > 300.0) {
            throw new BusinessRuleException(
                    "Peso fuera de rango plausible: " + request.pesoKg());
        }
        if (request.estaturaCm().doubleValue() < 100.0
                || request.estaturaCm().doubleValue() > 250.0) {
            throw new BusinessRuleException(
                    "Estatura fuera de rango plausible: " + request.estaturaCm());
        }
        if (request.perimetroAbdominalCm() != null) {
            double p = request.perimetroAbdominalCm().doubleValue();
            if (p < 40.0 || p > 200.0) {
                throw new BusinessRuleException(
                        "Perímetro abdominal fuera de rango plausible: " + p);
            }
        }
    }

    @Override
    @Transactional
    public AntropometricoResponse actualizar(Long idEvaluacion, AntropometricoRequest request) {
        EvaluacionNutricional evaluacion = evaluacionService.obtenerOFallar(idEvaluacion);

        if (evaluacion.getEstadoEvaluacion() == EstadoEvaluacion.ANULADA) {
            throw new BusinessRuleException(
                    "No se puede modificar la antropometría de una evaluación anulada");
        }

        DetalleAntropometrico detalle = antropometricoRepository
                .findByEvaluacion_IdEvaluacion(idEvaluacion)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No existe antropometría para la evaluación " + idEvaluacion));

        validarLimitesFisicos(request);

        // Actualizar medidas crudas
        detalle.setEstaturaCm(request.estaturaCm());
        detalle.setPesoKg(request.pesoKg());
        detalle.setPerimetroAbdominalCm(request.perimetroAbdominalCm());
        detalle.setPorcentajeGrasa(request.porcentajeGrasa());
        detalle.setPorcentajeMasaMuscular(request.porcentajeMasaMuscular());
        detalle.setPorcentajeGrasaVisceral(request.porcentajeGrasaVisceral());

        // Recalcular diagnósticos con el snapshot vigente
        AntropometricoDiagnosticosDto diagnosticos = calcularDiagnosticos(request, evaluacion);
        antropometricoMapper.actualizarDiagnosticos(diagnosticos, detalle);

        DetalleAntropometrico guardado = antropometricoRepository.save(detalle);
        return antropometricoMapper.toResponse(guardado);
    }
}