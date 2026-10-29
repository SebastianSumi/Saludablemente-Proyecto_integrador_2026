package upeu.edu.pe.Saludabemente.personal.persona.dto.response;

public record PersonaResumen(
        Long id,
        String nombres,
        String apellidoPaterno,
        String apellidoMaterno
) {
    /**
     * Nombre completo armado para combos y etiquetas del frontend.
     */
    public String getNombreCompleto() {
        StringBuilder sb = new StringBuilder(nombres)
                .append(' ')
                .append(apellidoPaterno);
        if (apellidoMaterno != null && !apellidoMaterno.isBlank()) {
            sb.append(' ').append(apellidoMaterno);
        }
        return sb.toString();
    }
}
