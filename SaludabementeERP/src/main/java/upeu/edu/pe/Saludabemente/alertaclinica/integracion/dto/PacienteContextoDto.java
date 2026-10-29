package upeu.edu.pe.Saludabemente.alertaclinica.integracion.dto;

import java.time.LocalDate;

public record PacienteContextoDto(

        Long idPersona,
        String nombreCompleto,
        String areaTrabajo,
        String sexo,
        LocalDate fechaNacimiento,
        String celularContacto,
        String correoContacto
) {}