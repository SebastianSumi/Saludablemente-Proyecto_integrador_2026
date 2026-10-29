package upeu.edu.pe.Saludabemente.nutricional.bioquimico.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import upeu.edu.pe.Saludabemente.exception.BusinessRuleException;
import upeu.edu.pe.Saludabemente.exception.ResourceNotFoundException;
import upeu.edu.pe.Saludabemente.nutricional.bioquimico.dto.FilaRechazadaDto;
import upeu.edu.pe.Saludabemente.nutricional.bioquimico.dto.ImportacionReporteDto;
import upeu.edu.pe.Saludabemente.nutricional.bioquimico.dto.request.BioquimicoExcelDto;
import upeu.edu.pe.Saludabemente.nutricional.bioquimico.dto.request.BioquimicoManualRequest;
import upeu.edu.pe.Saludabemente.nutricional.bioquimico.dto.response.BioquimicoResponse;
import upeu.edu.pe.Saludabemente.nutricional.bioquimico.entity.DetalleBioquimico;
import upeu.edu.pe.Saludabemente.nutricional.bioquimico.mapper.BioquimicoMapper;
import upeu.edu.pe.Saludabemente.nutricional.bioquimico.repository.BioquimicoRepository;
import upeu.edu.pe.Saludabemente.nutricional.bioquimico.service.BioquimicoService;
import upeu.edu.pe.Saludabemente.nutricional.evaluacion.entity.EvaluacionNutricional;
import upeu.edu.pe.Saludabemente.nutricional.evaluacion.enums.EstadoEvaluacion;
import upeu.edu.pe.Saludabemente.nutricional.evaluacion.repository.EvaluacionRepository;
import upeu.edu.pe.Saludabemente.nutricional.evaluacion.service.EvaluacionService;
import upeu.edu.pe.Saludabemente.nutricional.util.BioquimicoRulesUtil;
import upeu.edu.pe.Saludabemente.nutricional.util.ExcelParserUtil;
import upeu.edu.pe.Saludabemente.personal.persona.service.PersonaService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BioquimicoServiceImpl implements BioquimicoService {

    private final BioquimicoRepository bioquimicoRepository;
    private final BioquimicoMapper bioquimicoMapper;
    private final EvaluacionRepository evaluacionRepository;
    private final EvaluacionService evaluacionService;
    private final PersonaService personaService;

    // ===================================================================
    // Registro manual
    // ===================================================================

    @Override
    @Transactional
    public BioquimicoResponse registrarManual(Long idEvaluacion,
                                              BioquimicoManualRequest request) {
        // 1. Obtener cabecera (404 si no existe).
        EvaluacionNutricional evaluacion = evaluacionService.obtenerOFallar(idEvaluacion);

        // 2. RN-08: solo se puede registrar bioquímico si está PENDIENTE.
        if (evaluacion.getEstadoEvaluacion() != EstadoEvaluacion.PENDIENTE) {
            throw new BusinessRuleException(
                    "Solo se puede registrar bioquímico en evaluaciones PENDIENTES. "
                            + "Estado actual: " + evaluacion.getEstadoEvaluacion());
        }

        // 3. Unicidad 1:1.
        if (bioquimicoRepository.existsByEvaluacion_IdEvaluacion(idEvaluacion)) {
            throw new BusinessRuleException(
                    "La evaluación " + idEvaluacion + " ya tiene bioquímico registrado");
        }

        // 4. Mapear y calcular riesgo global.
        DetalleBioquimico detalle = bioquimicoMapper.toEntityManual(request);
        detalle.setDxBioquimico(calcularRiesgoGlobal(
                request.getGlucosa(),
                request.getColesterol(),
                request.getTrigliceridos(),
                request.getPresionSistolica(),
                request.getPresionDiastolica()));
        evaluacion.asignarBioquimico(detalle);

        DetalleBioquimico guardado = bioquimicoRepository.save(detalle);

        // 5. Evaluar completitud.
        evaluacionService.evaluarCompletitudYDisparar(idEvaluacion);

        return bioquimicoMapper.toResponse(guardado);
    }

    // ===================================================================
    // Importación masiva
    // ===================================================================

    @Override
    @Transactional
    public ImportacionReporteDto importarLoteExcel(MultipartFile archivo, String periodo) {
        String nombreArchivo = archivo.getOriginalFilename();

        // 1. Parsear el Excel.
        List<BioquimicoExcelDto> filas;
        try {
            filas = ExcelParserUtil.extraerFilas(archivo.getInputStream());
        } catch (IOException e) {
            throw new BusinessRuleException(
                    "No se pudo leer el archivo Excel: " + e.getMessage());
        }

        // 2. Resolución batch de celulares → idPersona (1 sola query).
        Set<String> celulares = filas.stream()
                .map(BioquimicoExcelDto::celular)
                .collect(Collectors.toSet());
        Map<String, Long> celularAIdPersona =
                personaService.obtenerIdsPersonaPorCelulares(celulares);

        // 3. Procesar fila por fila, acumulando rechazos.
        List<FilaRechazadaDto> rechazadas = new ArrayList<>();
        int procesadas = 0;

        for (BioquimicoExcelDto fila : filas) {
            Long idPersona = celularAIdPersona.get(fila.celular());

            if (idPersona == null) {
                rechazadas.add(new FilaRechazadaDto(
                        fila.numeroFila(),
                        fila.celular(),
                        "No existe persona con ese celular"));
                continue;
            }

            Optional<EvaluacionNutricional> evaluacionOpt =
                    evaluacionRepository.findPendientePorPersonaYPeriodo(
                            idPersona, periodo, EstadoEvaluacion.PENDIENTE);

            if (evaluacionOpt.isEmpty()) {
                rechazadas.add(new FilaRechazadaDto(
                        fila.numeroFila(),
                        fila.celular(),
                        "No hay evaluación PENDIENTE para el periodo " + periodo));
                continue;
            }

            EvaluacionNutricional evaluacion = evaluacionOpt.get();

            // Si ya tiene bioquímico, no sobrescribimos: la fila se rechaza.
            if (bioquimicoRepository.existsByEvaluacion_IdEvaluacion(
                    evaluacion.getIdEvaluacion())) {
                rechazadas.add(new FilaRechazadaDto(
                        fila.numeroFila(),
                        fila.celular(),
                        "La evaluación ya tiene bioquímico registrado"));
                continue;
            }

            vincularFilaAEvaluacion(fila, evaluacion, nombreArchivo);
            evaluacionService.evaluarCompletitudYDisparar(evaluacion.getIdEvaluacion());
            procesadas++;
        }

        return new ImportacionReporteDto(
                nombreArchivo,
                periodo,
                filas.size(),
                procesadas,
                rechazadas.size(),
                rechazadas);
    }

    /**
     * Subrutina interna: mapea una fila del Excel a una entidad bioquímica,
     * calcula el riesgo global, la vincula a la evaluación y la persiste.
     */
    private void vincularFilaAEvaluacion(BioquimicoExcelDto fila,
                                         EvaluacionNutricional evaluacion,
                                         String nombreArchivo) {
        DetalleBioquimico detalle = bioquimicoMapper.toEntityExcel(fila, nombreArchivo);
        detalle.setDxBioquimico(calcularRiesgoGlobal(
                fila.glucosa(),
                fila.colesterol(),
                fila.trigliceridos(),
                fila.presionSistolica(),
                fila.presionDiastolica()));
        evaluacion.asignarBioquimico(detalle);
        bioquimicoRepository.save(detalle);
    }

    // ===================================================================
    // Rollback
    // ===================================================================

    @Override
    @Transactional
    public int eliminarPorArchivo(String nombreArchivo) {
        if (nombreArchivo == null || nombreArchivo.isBlank()) {
            throw new BusinessRuleException("El nombre del archivo es obligatorio");
        }
        return bioquimicoRepository.deleteByArchivoOrigen(nombreArchivo);
    }

    // ===================================================================
    // Helpers privados
    // ===================================================================

    /**
     * Motor bioquímico puro. Ver BioquimicoRulesUtil.
     */
    private String calcularRiesgoGlobal(java.math.BigDecimal glucosa,
                                        java.math.BigDecimal colesterol,
                                        java.math.BigDecimal trigliceridos,
                                        Integer presionSistolica,
                                        Integer presionDiastolica) {
        return BioquimicoRulesUtil.calcularRiesgoGlobal(
                glucosa, colesterol, trigliceridos,
                presionSistolica, presionDiastolica);
    }

    @Override
    @Transactional
    public BioquimicoResponse actualizar(Long idEvaluacion,
                                         BioquimicoManualRequest request) {
        EvaluacionNutricional evaluacion = evaluacionService.obtenerOFallar(idEvaluacion);

        // Solo bloqueamos ANULADA.
        if (evaluacion.getEstadoEvaluacion() == EstadoEvaluacion.ANULADA) {
            throw new BusinessRuleException(
                    "No se puede modificar el bioquímico de una evaluación anulada");
        }

        DetalleBioquimico detalle = bioquimicoRepository
                .findByEvaluacion_IdEvaluacion(idEvaluacion)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No existe bioquímico para la evaluación " + idEvaluacion));

        // Actualizar los marcadores
        detalle.setGlucosa(request.getGlucosa());
        detalle.setColesterol(request.getColesterol());
        detalle.setTrigliceridos(request.getTrigliceridos());
        detalle.setPresionSistolica(request.getPresionSistolica());
        detalle.setPresionDiastolica(request.getPresionDiastolica());

        // Recalcular el diagnóstico de riesgo global
        detalle.setDxBioquimico(calcularRiesgoGlobal(
                request.getGlucosa(),
                request.getColesterol(),
                request.getTrigliceridos(),
                request.getPresionSistolica(),
                request.getPresionDiastolica()));

        DetalleBioquimico guardado = bioquimicoRepository.save(detalle);
        return bioquimicoMapper.toResponse(guardado);
    }
}