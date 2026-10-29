package upeu.edu.pe.Saludabemente.nutricional.bioquimico.service;

import org.springframework.web.multipart.MultipartFile;
import upeu.edu.pe.Saludabemente.nutricional.bioquimico.dto.ImportacionReporteDto;
import upeu.edu.pe.Saludabemente.nutricional.bioquimico.dto.request.BioquimicoManualRequest;
import upeu.edu.pe.Saludabemente.nutricional.bioquimico.dto.response.BioquimicoResponse;

/**
 * Gestión del detalle bioquímico: registro manual e importación masiva.
 * Cubre las funciones 34-37 de PLANIFICACION.docx.
 */
public interface BioquimicoService {

    /**
     * Registro manual del laboratorio para un colaborador puntual.
     */
    BioquimicoResponse registrarManual(Long idEvaluacion, BioquimicoManualRequest request);

    /**
     * Importación masiva desde el Excel del laboratorio.
     * El cruce persona↔fila se hace por celular.
     */
    ImportacionReporteDto importarLoteExcel(MultipartFile archivo, String periodo);

    /**
     * Rollback masivo: borra todos los bioquímicos asociados a un archivo.
     * Uso: se importó el Excel equivocado.
     */
    int eliminarPorArchivo(String nombreArchivo);

    /**
     * Actualiza el bioquímico existente de una evaluación.
     * Permitido en PENDIENTE y COMPLETA (el laboratorio puede emitir correcciones).
     * Recalcula el diagnóstico de riesgo global.
     * NO se permite en ANULADA.
     */
    BioquimicoResponse actualizar(Long idEvaluacion, BioquimicoManualRequest request);
}