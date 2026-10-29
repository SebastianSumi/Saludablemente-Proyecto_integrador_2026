package upeu.edu.pe.Saludabemente.noticias.categoria.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoriaResponseDTO {
    private Long id;
    private String nombre;
    private String descripcion;
    private String colorHex;
    private String icono;
    private Integer orden;
    private Boolean esSistema;
    private Boolean activo;
    private long cantidadNoticias;
}