package upeu.edu.pe.Saludabemente.personal.persona.dto;

import upeu.edu.pe.Saludabemente.personal.persona.entity.Sexo;

/**
 * DTO mínimo para el módulo Evaluación Nutricional.
 * Solo expone los datos que ese módulo necesita para aplicar la tabla OMRON:
 * edad calculada + sexo. Nada más.
 */
public record PersonaAgeSexDTO(
        Long id,
        Integer edad,
        Sexo sexo
) {
}