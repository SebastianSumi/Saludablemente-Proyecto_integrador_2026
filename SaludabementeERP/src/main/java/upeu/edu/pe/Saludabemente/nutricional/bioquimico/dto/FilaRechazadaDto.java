package upeu.edu.pe.Saludabemente.nutricional.bioquimico.dto;

/**
 * Fila del Excel que no pudo procesarse, con motivo del rechazo.
 */
public record FilaRechazadaDto(
        int numeroFila,
        String celular,
        String motivo
) {}