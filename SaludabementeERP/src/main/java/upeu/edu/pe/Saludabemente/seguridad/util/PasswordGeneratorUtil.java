package upeu.edu.pe.Saludabemente.seguridad.util;

import java.security.SecureRandom;

/**
 * Generador de contraseñas temporales seguras.
 *
 * <p>Formato: 12 caracteres alfanuméricos con al menos 1 mayúscula,
 * 1 minúscula, 1 dígito y 1 símbolo. Cumple con la política mínima
 * de complejidad exigida por el módulo.</p>
 *
 * <p>Usa {@link SecureRandom}, no {@code Random}: apto para contextos
 * criptográficos.</p>
 *
 * <p>Clase de utilería pura: sin estado, sin dependencias, no es bean.
 * Se invoca como método estático.</p>
 */
public final class PasswordGeneratorUtil {

    private static final String MAYUSCULAS = "ABCDEFGHJKLMNPQRSTUVWXYZ";   // sin I ni O
    private static final String MINUSCULAS = "abcdefghijkmnopqrstuvwxyz";  // sin l
    private static final String DIGITOS    = "23456789";                   // sin 0 ni 1
    private static final String SIMBOLOS   = "!@#$%&*";
    private static final String TODOS      = MAYUSCULAS + MINUSCULAS + DIGITOS + SIMBOLOS;
    private static final int LONGITUD      = 12;

    private static final SecureRandom RANDOM = new SecureRandom();

    private PasswordGeneratorUtil() {
        // Utilidad estática: no instanciable
    }

    /**
     * Genera una contraseña temporal segura.
     * Garantiza al menos un carácter de cada categoría.
     */
    public static String generarTemporal() {
        StringBuilder sb = new StringBuilder(LONGITUD);

        sb.append(MAYUSCULAS.charAt(RANDOM.nextInt(MAYUSCULAS.length())));
        sb.append(MINUSCULAS.charAt(RANDOM.nextInt(MINUSCULAS.length())));
        sb.append(DIGITOS.charAt(RANDOM.nextInt(DIGITOS.length())));
        sb.append(SIMBOLOS.charAt(RANDOM.nextInt(SIMBOLOS.length())));

        for (int i = sb.length(); i < LONGITUD; i++) {
            sb.append(TODOS.charAt(RANDOM.nextInt(TODOS.length())));
        }

        // Mezclar para que el orden de "garantizados" no sea predecible
        return mezclar(sb.toString());
    }

    private static String mezclar(String input) {
        char[] chars = input.toCharArray();
        for (int i = chars.length - 1; i > 0; i--) {
            int j = RANDOM.nextInt(i + 1);
            char temp = chars[i];
            chars[i] = chars[j];
            chars[j] = temp;
        }
        return new String(chars);
    }
}