package upeu.edu.pe.Saludabemente.personal.persona.dto.request;

import jakarta.validation.constraints.*;
import upeu.edu.pe.Saludabemente.personal.persona.entity.Sexo;

import java.time.LocalDate;

public record PersonaRequest(
        @NotBlank(message = "Los nombres son obligatorios")
        @Size(max = 100, message = "Los nombres no pueden superar 100 caracteres")
        String nombres,

        @NotBlank(message = "El apellido paterno es obligatorio")
        @Size(max = 100, message = "El apellido paterno no puede superar 100 caracteres")
        String apellidoPaterno,

        @Size(max = 100, message = "El apellido materno no puede superar 100 caracteres")
        String apellidoMaterno,

        @NotBlank(message = "El documento de identidad es obligatorio")
        @Pattern(
                regexp = "^\\d{8}$",
                message = "El documento de identidad debe tener exactamente 8 dígitos"
        )
        String documentoIdentidad,

        @NotBlank(message = "El celular es obligatorio")
        @Pattern(
                regexp = "^\\+?[0-9]{9,15}$",
                message = "El celular debe tener entre 9 y 15 dígitos, opcionalmente con prefijo +"
        )
        String celular,

        @NotNull(message = "La fecha de nacimiento es obligatoria")
        @Past(message = "La fecha de nacimiento debe ser anterior a hoy")
        LocalDate fechaNacimiento,

        @NotNull(message = "El sexo es obligatorio")
        Sexo sexo,

        @Positive(message = "El id de área debe ser positivo")
        Long areaId,

        @Size(max = 5, message = "La talla de polo no puede superar 5 caracteres")
        String tallaPolo,

        @Size(max = 20, message = "El estado civil no puede superar 20 caracteres")
        String estadoCivil,

        @Size(max = 30, message = "El nivel educativo no puede superar 30 caracteres")
        String nivelEducativo,

        @Positive(message = "El id de equipo debe ser positivo")
        Long equipoId,

        @Email(message = "El email debe tener un formato válido")
        @Size(max = 120, message = "El email no puede superar 120 caracteres")
        String email
) {
    public PersonaRequest(
            String nombres,
            String apellidoPaterno,
            String apellidoMaterno,
            String documentoIdentidad,
            String celular,
            LocalDate fechaNacimiento,
            Sexo sexo,
            Long areaId,
            String tallaPolo,
            String estadoCivil,
            String nivelEducativo,
            Long equipoId
    ) {
        this(nombres, apellidoPaterno, apellidoMaterno, documentoIdentidad, celular,
             fechaNacimiento, sexo, areaId, tallaPolo, estadoCivil, nivelEducativo, equipoId, null);
    }
}
