package upeu.edu.pe.Saludabemente.asistencias.registro.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Resultado de la marcación masiva")
public record MarcacionLoteResponse(
        int procesados,
        int creados,
        int actualizados,
        List<FalloMarcacion> fallos
) {
    public record FalloMarcacion(Long idPersona, String motivo) {}
}
