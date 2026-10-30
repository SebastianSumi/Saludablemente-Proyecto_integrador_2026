package upeu.edu.pe.Saludabemente.seguridad.seeder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import upeu.edu.pe.Saludabemente.personal.persona.dto.PersonaSnapshotIdentidadDTO;
import upeu.edu.pe.Saludabemente.seguridad.integracion.PersonalAdapter;
import upeu.edu.pe.Saludabemente.seguridad.rol.entity.Rol;
import upeu.edu.pe.Saludabemente.seguridad.rol.repository.RolRepository;
import upeu.edu.pe.Saludabemente.seguridad.usuario.dto.request.UsuarioRequest;
import upeu.edu.pe.Saludabemente.seguridad.usuario.entity.Usuario;
import upeu.edu.pe.Saludabemente.seguridad.usuario.repository.UsuarioRepository;
import upeu.edu.pe.Saludabemente.seguridad.usuario.service.UsuarioService;
import upeu.edu.pe.Saludabemente.seguridad.util.UsernameGenerator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Carga inicial de usuarios de prueba del módulo Seguridad.
 *
 * <p>Crea un usuario por cada persona del seed de Personal, asignándole
 * distintos roles para cubrir todos los tipos de cuenta. La contraseña
 * se persiste cifrada en Oracle (vía UsuarioService).</p>
 *
 * <p><b>Orden:</b> @Order(5). Corre DESPUÉS de RolSeeder (4).</p>
 *
 * <p><b>Idempotente:</b> si ya hay usuarios en Oracle, se salta todo.</p>
 *
 * <p><b>Vinculación por documento:</b> los usuarios se asignan por
 * {@code documentoIdentidad}, no por índice, para ser inmunes al orden
 * alfabético que use el módulo Personal al listar.</p>
 */
@Component
@Profile("prod")
@Order(5)
@RequiredArgsConstructor
@Slf4j
public class UsuarioSeeder implements CommandLineRunner {

    private static final String PASSWORD_DEFAULT = "Temporal123!";

    private final UsuarioService usuarioService;
    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PersonalAdapter personalAdapter;

    @Override
    public void run(String... args) {
        log.info("=== UsuarioSeeder (Seguridad): verificando estado de la BD ===");

        long usuariosExistentes = usuarioRepository.count();
        if (usuariosExistentes > 0) {
            backfillUsernames();
            log.info("UsuarioSeeder (Seguridad): la BD ya tiene {} usuarios. Se omite el seed.",
                    usuariosExistentes);
            return;
        }

        Map<String, Long> rolesPorNombre = cargarRolesPorNombre();
        if (rolesPorNombre.isEmpty()) {
            log.warn("UsuarioSeeder (Seguridad): no hay roles en la BD. "
                    + "Se omite el seed. Ejecute primero RolSeeder.");
            return;
        }

        List<PersonaSnapshotIdentidadDTO> personas = personalAdapter.listarPersonasDisponibles();
        if (personas.isEmpty()) {
            log.warn("UsuarioSeeder (Seguridad): no hay personas en Personal. "
                    + "Se omite el seed. Ejecute primero DataSeeder de personal.");
            return;
        }

        log.info("UsuarioSeeder (Seguridad): BD vacía. Iniciando carga de usuarios...");
        log.info("  Roles disponibles: {}", rolesPorNombre.keySet());
        log.info("  Personas disponibles: {}", personas.size());

        seedUsuarios(personas, rolesPorNombre);

        log.info("=== UsuarioSeeder (Seguridad): carga finalizada ===");
    }

    private Map<String, Long> cargarRolesPorNombre() {
        Map<String, Long> mapa = new HashMap<>();
        for (Rol rol : rolRepository.findAll()) {
            mapa.put(rol.getNombre(), rol.getId());
        }
        return mapa;
    }

    /**
     * Plan de asignación por documento de identidad.
     * El email local se usa para construir el email institucional de la cuenta.
     */
    private void seedUsuarios(List<PersonaSnapshotIdentidadDTO> personas,
                              Map<String, Long> rolesPorNombre) {

        record PlanUsuario(String documentoIdentidad, String rol, String emailLocal) {}

        List<PlanUsuario> plan = List.of(
                new PlanUsuario("11111111", "Administrador", "ana.lazaro"),
                new PlanUsuario("22222222", "Coordinador",   "eulalia.soncco"),
                new PlanUsuario("33333333", "Colaborador",   "barbara.bernedo"),
                new PlanUsuario("44444444", "Evaluador",     "freyde.mayhuiri"),
                new PlanUsuario("55555555", "Coordinador",   "omar.huichi"),
                new PlanUsuario("66666666", "Evaluador",     "wilmer.velasquez"),
                new PlanUsuario("77777777", "Colaborador",   "maribel.torres"),
                new PlanUsuario("88888888", "Evaluador",     "gabriel.leon")
        );

        // Índice por documento de identidad (inmune al orden)
        Map<String, PersonaSnapshotIdentidadDTO> personasPorDocumento = personas.stream()
                .collect(Collectors.toMap(
                        PersonaSnapshotIdentidadDTO::documentoIdentidad,
                        p -> p
                ));

        int creados = 0;
        for (PlanUsuario planItem : plan) {
            PersonaSnapshotIdentidadDTO persona =
                    personasPorDocumento.get(planItem.documentoIdentidad());

            if (persona == null) {
                log.warn("  [!] Persona con documento '{}' no encontrada. Se omite '{}'",
                        planItem.documentoIdentidad(), planItem.emailLocal());
                continue;
            }

            Long rolId = rolesPorNombre.get(planItem.rol());
            if (rolId == null) {
                log.warn("  [!] Rol '{}' no encontrado en el catálogo. Se omite '{}'",
                        planItem.rol(), planItem.emailLocal());
                continue;
            }

            try {
                String email = planItem.emailLocal() + "@upeu.edu.pe";

                UsuarioRequest request = new UsuarioRequest(
                        email,
                        PASSWORD_DEFAULT,
                        persona.idPersona(),
                        Set.of(rolId)
                );

                var response = usuarioService.crear(request);
                creados++;
                log.info("  [+] Usuario '{}' creado con id={}, rol={}, persona={} {}",
                        response.email(),
                        response.id(),
                        planItem.rol(),
                        persona.nombres(),
                        persona.apellidoPaterno());
            } catch (Exception e) {
                log.warn("  [!] No se pudo crear el usuario '{}' (persona doc={}): {}",
                        planItem.emailLocal(), planItem.documentoIdentidad(), e.getMessage());
            }
        }

        log.info("UsuarioSeeder (Seguridad): {} usuarios cargados de {} intentos.",
                creados, plan.size());
    }
    /**
     * Rellena el username de usuarios existentes que no lo tengan.
     * Solo aplica en transición de {@code ddl-auto: create} a {@code update/validate}.
     * En DEV con {@code create} la tabla se recrea y este método no hace nada.
     */
    private void backfillUsernames() {
        List<Usuario> sinUsername = usuarioRepository.findAll().stream()
                .filter(u -> u.getUsername() == null || u.getUsername().isBlank())
                .toList();

        if (sinUsername.isEmpty()) return;

        log.info("UsuarioSeeder (Seguridad): backfill de username para {} usuarios",
                sinUsername.size());

        for (Usuario u : sinUsername) {
            String username = UsernameGenerator.generarUnico(
                    u.getNombresSnapshot(),
                    u.getApellidoPaternoSnapshot(),
                    usuarioRepository::existsByUsername
            );
            u.setUsername(username);
            usuarioRepository.save(u);
            log.info("  [~] Backfill username: {} → {}", u.getEmail(), username);
        }
    }
}
