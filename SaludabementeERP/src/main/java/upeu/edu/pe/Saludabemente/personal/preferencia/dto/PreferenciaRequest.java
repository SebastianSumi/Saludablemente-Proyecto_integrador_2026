package upeu.edu.pe.Saludabemente.personal.preferencia.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalTime;

@Getter
@Setter
public class PreferenciaRequest {

    @NotBlank(message = "El canal preferido es obligatorio")
    @Size(max = 30, message = "El canal preferido no puede superar 30 caracteres")
    private String canalPreferido;

    private LocalTime horarioContactoInicio;

    private LocalTime horarioContactoFin;

    private Boolean aceptaRecordatorios;
}