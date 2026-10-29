package upeu.edu.pe.Saludabemente.nutricional.antropometrico.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record AntropometricoRequest(
        @NotNull(message = "La estatura es obligatoria")
        @DecimalMin(value = "100.0", message = "La estatura debe ser ≥ 100 cm")
        @DecimalMax(value = "250.0", message = "La estatura debe ser ≤ 250 cm")
        BigDecimal estaturaCm,

        @NotNull(message = "El peso es obligatorio")
        @DecimalMin(value = "20.0", message = "El peso debe ser ≥ 20 kg")
        @DecimalMax(value = "300.0", message = "El peso debe ser ≤ 300 kg")
        BigDecimal pesoKg,

        @DecimalMin(value = "40.0", message = "El perímetro abdominal debe ser ≥ 40 cm")
        @DecimalMax(value = "200.0", message = "El perímetro abdominal debe ser ≤ 200 cm")
        BigDecimal perimetroAbdominalCm,

        @DecimalMin(value = "0.0", message = "El % de grasa no puede ser negativo")
        @DecimalMax(value = "70.0", message = "El % de grasa no puede superar 70")
        BigDecimal porcentajeGrasa,

        @DecimalMin(value = "0.0", message = "El % de masa muscular no puede ser negativo")
        @DecimalMax(value = "100.0", message = "El % de masa muscular no puede superar 100")
        BigDecimal porcentajeMasaMuscular,

        @DecimalMin(value = "0.0", message = "El % de grasa visceral no puede ser negativo")
        @DecimalMax(value = "30.0", message = "El % de grasa visceral no puede superar 30")
        BigDecimal porcentajeGrasaVisceral
) {}