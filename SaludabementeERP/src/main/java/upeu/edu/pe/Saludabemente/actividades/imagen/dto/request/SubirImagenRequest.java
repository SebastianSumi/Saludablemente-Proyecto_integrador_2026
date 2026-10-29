package upeu.edu.pe.Saludabemente.actividades.imagen.dto.request;

import jakarta.validation.constraints.Size;

public record SubirImagenRequest(
        @Size(max = 200) String descripcion
) {}