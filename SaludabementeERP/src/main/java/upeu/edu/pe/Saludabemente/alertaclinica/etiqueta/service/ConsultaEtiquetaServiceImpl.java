package upeu.edu.pe.Saludabemente.alertaclinica.etiqueta.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import upeu.edu.pe.Saludabemente.alertaclinica.alerta.entity.*;
import upeu.edu.pe.Saludabemente.alertaclinica.etiqueta.dto.*;
import upeu.edu.pe.Saludabemente.alertaclinica.etiqueta.mapper.EtiquetaClinicaMapper;
import upeu.edu.pe.Saludabemente.alertaclinica.etiqueta.repository.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ConsultaEtiquetaServiceImpl implements ConsultaEtiquetaService {
    private final EtiquetaClinicaRepository etiquetas;
    private final AlertaEtiquetaRepository asociaciones;
    private final EtiquetaClinicaMapper mapper;

    @Override
    public Page<EtiquetaResumenDto> buscar(String estado, String texto, Long idParametro, int pagina, int tamanio) {
        validarEstado(estado);
        if (pagina < 0 || tamanio < 1 || tamanio > 100) throw new IllegalArgumentException("Pagina >= 0 y tamanio entre 1 y 100");
        return etiquetas.buscarResumenPaginado(estado, texto == null || texto.isBlank() ? null : texto.trim(),
                idParametro, PageRequest.of(pagina, tamanio));
    }

    @Override
    public ReporteEtiquetasResponse reporte(String estadoEtiqueta, Long idEtiqueta, Long idParametro,
            EstadoAlerta estadoAlerta, SeveridadAlerta severidad, LocalDateTime desde, LocalDateTime hasta) {
        validarEstado(estadoEtiqueta);
        if (desde != null && hasta != null && desde.isAfter(hasta)) throw new IllegalArgumentException("El rango de fechas es invalido");
        // Una consulta para todas las asignaciones; no una consulta por cada etiqueta.
        var porEtiqueta = asociaciones.findAllParaReporte().stream()
                .filter(a -> estadoAlerta == null || a.getAlerta().getEstado() == estadoAlerta)
                .filter(a -> severidad == null || a.getAlerta().getNivelSeveridad() == severidad)
                .filter(a -> desde == null || !a.getFechaAsignacion().isBefore(desde))
                .filter(a -> hasta == null || !a.getFechaAsignacion().isAfter(hasta))
                .collect(Collectors.groupingBy(a -> a.getIdEtiqueta()));
        var filas = new ArrayList<ReporteEtiquetasResponse.Fila>();
        var unicas = new HashSet<Long>();
        var criticas = new HashSet<Long>();
        for (var etiqueta : etiquetas.findAllConParametros()) {
            if (estadoEtiqueta != null && !estadoEtiqueta.equals(etiqueta.getEstado())) continue;
            if (idEtiqueta != null && !idEtiqueta.equals(etiqueta.getId())) continue;
            if (idParametro != null && etiqueta.getParametros().stream().noneMatch(p -> idParametro.equals(p.getIdParametro()))) continue;
            var asignaciones = porEtiqueta.getOrDefault(etiqueta.getId(), List.of());
            var alertas = asignaciones.stream().map(a -> {
                var alerta = a.getAlerta();
                unicas.add(alerta.getIdAlerta());
                if (alerta.getNivelSeveridad() == SeveridadAlerta.CRITICA) criticas.add(alerta.getIdAlerta());
                return new AlertaResumenParaEtiquetaDto(alerta.getIdAlerta(), alerta.getPersonaId(),
                        alerta.getNivelSeveridad(), alerta.getEstado(), alerta.getFechaGeneracion(),
                        a.getAsignadaPor(), a.getFechaAsignacion());
            }).sorted(Comparator.comparing(AlertaResumenParaEtiquetaDto::fechaAsignacion).reversed()
                    .thenComparing(AlertaResumenParaEtiquetaDto::idAlerta)).toList();
            var ultima = alertas.stream().map(AlertaResumenParaEtiquetaDto::fechaAsignacion).max(Comparator.naturalOrder()).orElse(null);
            filas.add(new ReporteEtiquetasResponse.Fila(etiqueta.getId(), etiqueta.getCodigo(), etiqueta.getNombre(),
                    etiqueta.getColorHex(), etiqueta.getEstado(), mapper.toResponseDto(etiqueta, List.of()).parametros(),
                    alertas.size(), ultima, alertas));
        }
        filas.sort(Comparator.comparing(ReporteEtiquetasResponse.Fila::codigo));
        return new ReporteEtiquetasResponse(filas.size(), filas.stream().filter(f -> "ACTIVO".equals(f.estado())).count(),
                filas.stream().mapToLong(ReporteEtiquetasResponse.Fila::cantidadAlertas).sum(), unicas.size(), criticas.size(), filas);
    }

    private void validarEstado(String estado) {
        if (estado != null && !Set.of("ACTIVO", "INACTIVO").contains(estado)) throw new IllegalArgumentException("Estado debe ser ACTIVO o INACTIVO");
    }
}
