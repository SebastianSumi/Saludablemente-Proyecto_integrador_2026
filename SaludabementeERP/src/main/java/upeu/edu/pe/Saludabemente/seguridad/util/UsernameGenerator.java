package upeu.edu.pe.Saludabemente.seguridad.util;

import java.text.Normalizer;
import java.util.function.Predicate;

/**
 * Generador de usernames cortos para el módulo Seguridad.
 *
 * <p>Formato: {@code {primerNombre}.{apellidoPaterno}} en minúsculas, sin
 * tildes ni caracteres especiales. Ejemplo: "Ana" + "Lazaro" → "ana.lazaro".</p>
 *
 * <p><b>Regla del nombre:</b> si la persona tiene nombre compuesto
 * ("Freyde Job"), se usa solo el primer token → "freyde".</p>
 *
 * <p><b>Unicidad:</b> si el candidato existe, agrega sufijo numérico
 * ("ana.lazaro2", "ana.lazaro3", ...). El chequeo se delega al llamador
 * vía un {@link Predicate}, para no acoplar este util al repositorio.</p>
 */
public final class UsernameGenerator {

    private UsernameGenerator() {
        // utilería estática
    }

    /**
     * Genera el username base sin verificar unicidad.
     * Ej: "Ana", "Lazaro" → "ana.lazaro".
     */
    public static String generar(String nombres, String apellidoPaterno) {
        String primerNombre = extraerPrimerNombre(nombres);
        String nombreNorm = normalizar(primerNombre);
        String apellidoNorm = normalizar(apellidoPaterno);

        if (nombreNorm.isEmpty() || apellidoNorm.isEmpty()) {
            return nombreNorm + apellidoNorm;  // fallback raro
        }
        return nombreNorm + "." + apellidoNorm;
    }

    /**
     * Genera un username único consultando al predicado por cada candidato.
     *
     * @param nombres         nombres completos del usuario
     * @param apellidoPaterno apellido paterno
     * @param yaExiste        función que responde si un username ya está tomado
     * @return username único garantizado por el predicado
     */
    public static String generarUnico(String nombres, String apellidoPaterno,
                                      Predicate<String> yaExiste) {
        String base = generar(nombres, apellidoPaterno);
        if (!yaExiste.test(base)) {
            return base;
        }
        int contador = 2;
        while (true) {
            String candidato = base + contador;
            if (!yaExiste.test(candidato)) {
                return candidato;
            }
            contador++;
        }
    }

    // -------------------------------------------------------------------
    // Internos
    // -------------------------------------------------------------------

    private static String extraerPrimerNombre(String nombres) {
        if (nombres == null || nombres.isBlank()) return "";
        return nombres.trim().split("\\s+")[0];
    }

    private static String normalizar(String texto) {
        if (texto == null || texto.isBlank()) return "";
        String sinTildes = Normalizer
                .normalize(texto, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        return sinTildes.toLowerCase()
                .replaceAll("[^a-z]", "");  // solo letras a-z
    }
}