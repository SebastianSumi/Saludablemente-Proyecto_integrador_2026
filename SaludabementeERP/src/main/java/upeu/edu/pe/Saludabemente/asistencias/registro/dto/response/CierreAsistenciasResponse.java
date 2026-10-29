package upeu.edu.pe.Saludabemente.asistencias.registro.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Resultado del cierre de una actividad")
public record CierreAsistenciasResponse(
        Long idActividad,
        Integer totalEsperados,
        Integer totalAsistentes,
        Integer totalAusentes,
        Integer totalExonerados,
        Integer totalIncompletas,
        Boolean cierreRealizado
) {}