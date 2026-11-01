package upeu.edu.pe.Saludabemente.reportes;

import java.time.LocalDate;
import java.time.LocalDateTime;

public interface ReportePersonalService {
    byte[] generarAsistenciasPdf(Long idPersona, String periodo);
    byte[] generarDatosPersonalesPdf(Long idPersona);
    byte[] generarActividadesPdf(Long idPersona, LocalDate desde, LocalDate hasta);
    byte[] generarEvaluacionesPdf(Long idPersona);
    byte[] generarAlertasPdf(Long idPersona, LocalDateTime desde, LocalDateTime hasta);
}
