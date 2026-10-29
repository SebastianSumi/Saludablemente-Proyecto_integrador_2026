package upeu.edu.pe.Saludabemente.alertaclinica.etiqueta.dto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
@Schema(description = "Conjunto final completo. Incluir asociaciones MOTOR existentes. Una lista vacia retira las editables. revision se obtiene del GET de esta misma tarjeta.")
public record GuardarEtiquetasRequest(
 @NotBlank @Schema(description = "Revision recibida del GET; un cambio concurrente produce 409") String revision,
 @Schema(example = "[{\"idEtiqueta\":1},{\"idEtiqueta\":2}]")
 @NotNull @Size(max = 500) List<@NotNull @Valid EtiquetaDetalleRequest> detalles
) {}
