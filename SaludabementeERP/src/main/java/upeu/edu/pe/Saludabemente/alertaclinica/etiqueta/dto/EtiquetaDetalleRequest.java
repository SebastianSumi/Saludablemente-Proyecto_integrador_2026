package upeu.edu.pe.Saludabemente.alertaclinica.etiqueta.dto;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import io.swagger.v3.oas.annotations.media.Schema;
public record EtiquetaDetalleRequest(@NotNull @Positive @Schema(example = "1") Long idEtiqueta) {}
