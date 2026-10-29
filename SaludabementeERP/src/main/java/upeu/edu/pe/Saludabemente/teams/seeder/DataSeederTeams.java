package upeu.edu.pe.Saludabemente.teams.seeder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import upeu.edu.pe.Saludabemente.teams.service.TeamService;

import java.util.List;

/**
 * Carga inicial de datos del módulo Teams: equipos de prueba.
 *
 * <p>Vive dentro del módulo {@code teams} respetando los límites de Spring Modulith.</p>
 * <p><b>Orden:</b> @Order(2). Corre antes de DataSeeder de Personal para que los equipos existan.</p>
 */
@Component
@Profile("dev")
@Order(2)
@RequiredArgsConstructor
@Slf4j
public class DataSeederTeams implements CommandLineRunner {

    private final TeamService teamService;
    private final upeu.edu.pe.Saludabemente.teams.repository.TeamRepository teamRepository;

    @Override
    public void run(String... args) {
        log.info("=== DataSeederTeams: verificando estado de la BD ===");

        long equiposExistentes = teamService.listar(null, Sort.unsorted()).size();
        if (equiposExistentes > 0) {
            log.info("DataSeederTeams: la BD ya tiene {} equipos. Se omite el seed.", equiposExistentes);
            return;
        }

        log.info("DataSeederTeams: cargando equipos de prueba...");

        record SeedEquipo(String nombre, String descripcion) {}

        List<SeedEquipo> equipos = List.of(
                new SeedEquipo("Chirimoya",     "Equipo de prueba - area administrativa"),
                new SeedEquipo("Nutri Bebes",   "Equipo de prueba - servicio de alimentacion"),
                new SeedEquipo("Nutria Salvaje","Equipo de prueba - logistica y limpieza")
        );

        for (SeedEquipo seed : equipos) {
            try {
                var team = teamRepository.save(upeu.edu.pe.Saludabemente.teams.entity.Team.builder()
                        .nombre(seed.nombre()).descripcion(seed.descripcion()).activo(true).build());
                log.info("  [+] Equipo '{}' creado con id={}", team.getNombre(), team.getId());
            } catch (Exception e) {
                log.warn("  [!] No se pudo crear el equipo '{}': {}", seed.nombre(), e.getMessage());
            }
        }

        log.info("=== DataSeederTeams: carga finalizada ===");
    }
}
