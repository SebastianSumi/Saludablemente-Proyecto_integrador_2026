package upeu.edu.pe.Saludabemente.alertaclinica.etiqueta.dto;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import io.swagger.v3.oas.annotations.media.Schema;
public record ParametroDetalleRequest(@NotNull @Positive @Schema(example = "1") Long idParametro) {}
