@ApplicationModule(
        displayName = "Asistencias",
        allowedDependencies = {
                "personal::personal-api",
                "actividades::actividades-api",
                "actividades::actividades-api-dto",
                "exception"
        }
)
package upeu.edu.pe.Saludabemente.asistencias;

import org.springframework.modulith.ApplicationModule;