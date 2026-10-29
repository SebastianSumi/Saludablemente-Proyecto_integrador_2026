package upeu.edu.pe.Saludabemente.actividades.configuracion.dto.response;

import java.util.List;

public record ConfiguracionResponse(
        Long idActividad,
        Integer puntajeSalud,
        List<String> materiales
) {}
