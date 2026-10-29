package upeu.edu.pe.Saludabemente.alertaclinica.alerta.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AtenderAlertaRequest(

        @NotNull(message = "El id del usuario que atiende es obligatorio")
        Long idUsuario,

        @Size(max = 300, message = "La observacion no puede superar los 300 caracteres")
        String observacion
) {}