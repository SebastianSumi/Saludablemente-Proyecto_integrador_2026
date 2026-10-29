package upeu.edu.pe.Saludabemente.seguridad.rol.enums;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Roles permitidos en el sistema Saludablemente.
 */
public enum RolEnum {
    ADMINISTRADOR("Administrador"),
    EVALUADOR("Evaluador"),
    COORDINADOR("Coordinador"),
    COLABORADOR("Colaborador");

    private final String nombre;

    RolEnum(String nombre) {
        this.nombre = nombre;
    }

    public String getNombre() {
        return nombre;
    }

    public static Set<String> getNombresValidos() {
        return Arrays.stream(values())
                .map(RolEnum::getNombre)
                .collect(Collectors.toSet());
    }

    public static boolean esValido(String nombre) {
        if (nombre == null) {
            return false;
        }
        return Arrays.stream(values())
                .anyMatch(r -> r.getNombre().equalsIgnoreCase(nombre.trim()));
    }
}
