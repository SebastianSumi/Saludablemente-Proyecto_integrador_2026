package upeu.edu.pe.Saludabemente.actividades.imagen.dto.response;

public record ImagenResponse(
        Long idImagen,
        String url,
        String descripcion,
        Integer orden
) {}