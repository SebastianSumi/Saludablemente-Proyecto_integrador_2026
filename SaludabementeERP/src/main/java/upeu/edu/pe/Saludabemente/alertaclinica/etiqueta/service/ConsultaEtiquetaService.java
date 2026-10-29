package upeu.edu.pe.Saludabemente.alertaclinica.etiqueta.service;

import org.springframework.data.domain.Page;
import upeu.edu.pe.Saludabemente.alertaclinica.etiqueta.dto.*;
import upeu.edu.pe.Saludabemente.alertaclinica.alerta.entity.*;
import java.time.LocalDateTime;

public interface ConsultaEtiquetaService {
    Page<EtiquetaResumenDto> buscar(String estado, String texto, Long idParametro, int pagina, int tamanio);
    ReporteEtiquetasResponse reporte(String estadoEtiqueta, Long idEtiqueta, Long idParametro,
            EstadoAlerta estadoAlerta, SeveridadAlerta severidad, LocalDateTime desde, LocalDateTime hasta);
}
