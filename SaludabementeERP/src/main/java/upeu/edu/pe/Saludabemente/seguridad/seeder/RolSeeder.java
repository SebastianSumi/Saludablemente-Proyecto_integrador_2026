package upeu.edu.pe.Saludabemente.seguridad.seeder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import upeu.edu.pe.Saludabemente.seguridad.rol.entity.Rol;
import upeu.edu.pe.Saludabemente.seguridad.rol.repository.RolRepository;

import java.util.List;

/**
 * Carga inicial del catálogo de roles base del módulo Seguridad.
 *
 * <p>Vive dentro del módulo {@code seguridad} porque consume su
 * {@link RolRepository} — acceso intra-módulo permitido por Modulith.</p>
 *
 * <p><b>Orden:</b> {@code @Order(3)}. Corre DESPUÉS de
 * {@code DataSeederCatalogo} (1) y {@code DataSeeder} (2). Los roles
 * base son independientes de los datos de Personal y Catálogo, pero
 * {@code @Order(3)} deja espacio para futuros seeders del módulo
 * (ej. {@code UsuarioRootSeeder} en Sub-fase 2, que necesitará que
 * Personal ya tenga al menos una persona).</p>
 *
 * <p><b>Idempotente:</b> si ya hay roles en la BD, se salta todo.
 * Condición simple {@code count() > 0} — no distingue por nombre porque
 * los 4 roles se insertan como bloque atómico.</p>
 *
 * <p><b>Borrable en PROD:</b> está bajo {@code @Profile("dev")}. En
 * producción, los roles se insertan con el script DDL
 * {@code S01_seguridad_esquema_prod.sql} y este seeder no corre.</p>
 *
 * <p><b>ROOT no se inserta aquí.</b> El rol ROOT se agrega en Sub-fase 2
 * junto con el bootstrap del primer usuario raíz.</p>
 */
@Component
@Profile("dev")
@Order(3)
@RequiredArgsConstructor
@Slf4j
public class RolSeeder implements CommandLineRunner {

    private final RolRepository rolRepository;

    @Override
    public void run(String... args) {
        log.info("=== RolSeeder (Seguridad): verificando estado del catálogo ===");

        long rolesExistentes = rolRepository.count();
        if (rolesExistentes > 0) {
            log.info("RolSeeder (Seguridad): la BD ya tiene {} roles. Se omite el seed.",
                    rolesExistentes);
            return;
        }

        log.info("RolSeeder (Seguridad): catálogo vacío. Insertando roles base...");

        List<Rol> roles = List.of(
                Rol.builder()
                        .nombre("Administrador")
                        .descripcion("Acceso total al sistema, gestión de usuarios y configuración")
                        .build(),
                Rol.builder()
                        .nombre("Evaluador")
                        .descripcion("Registro de evaluaciones nutricionales y pruebas físicas")
                        .build(),
                Rol.builder()
                        .nombre("Coordinador")
                        .descripcion("Supervisión de evaluaciones y reportes del área")
                        .build(),
                Rol.builder()
                        .nombre("Colaborador")
                        .descripcion("Acceso de solo lectura a su propio perfil y evaluaciones")
                        .build()
        );

        rolRepository.saveAll(roles);

        roles.forEach(rol -> log.info("  [+] Rol '{}' creado con id={}",
                rol.getNombre(), rol.getId()));

        log.info("=== RolSeeder (Seguridad): {} roles cargados ===", roles.size());
    }
}