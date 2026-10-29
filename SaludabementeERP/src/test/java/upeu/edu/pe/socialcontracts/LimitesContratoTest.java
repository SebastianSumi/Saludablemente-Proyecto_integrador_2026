package upeu.edu.pe.socialcontracts;

import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.Test;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/** Límites locales: no sustituye modules.verify() del grafo completo. */
class LimitesContratoTest {
    @Test void socialNoImportaRepositoriosNiEntidadesExternos() {
        var clases = new ClassFileImporter().withImportOption(new ImportOption.DoNotIncludeTests())
                .importPackages("upeu.edu.pe.Saludabemente.social");
        noClasses().should().dependOnClassesThat().resideInAnyPackage(
                "..seguridad.usuario..", "..seguridad.rol..", "..personal.persona..",
                "..teams.entity..", "..teams.repository..", "..actividades.agenda..",
                "..noticias.noticia..", "..notificaciones.notificacion..")
                .check(clases);
    }
}
