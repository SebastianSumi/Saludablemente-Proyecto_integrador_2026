package upeu.edu.pe.Saludabemente.personal.seeder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import upeu.edu.pe.Saludabemente.catalogo.area.dto.response.AreaResumen;
import upeu.edu.pe.Saludabemente.catalogo.area.service.AreaService;
import upeu.edu.pe.Saludabemente.personal.persona.dto.request.PersonaRequest;
import upeu.edu.pe.Saludabemente.personal.persona.dto.response.PersonaResponse;
import upeu.edu.pe.Saludabemente.personal.persona.entity.Sexo;
import upeu.edu.pe.Saludabemente.personal.persona.service.PersonaService;
import upeu.edu.pe.Saludabemente.teams.api.TeamApiService;
import upeu.edu.pe.Saludabemente.teams.api.TeamSnapshotDTO;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Carga inicial de datos de Personal: personas de prueba.
 *
 * <p>Vive dentro del modulo {@code personal}. Consume {@code TeamApiService}
 * de {@code teams} y {@code AreaService} de {@code catalogo}.</p>
 *
 * <p><b>Orden:</b> @Order(3). Corre DESPUES de DataSeederCatalogo y DataSeederTeams.</p>
 */
@Component
@Profile("dev")
@Order(3)
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final TeamApiService teamApiService;
    private final PersonaService personaService;
    private final AreaService areaService;

    @Override
    public void run(String... args) {
        log.info("=== DataSeeder (Personal): verificando estado de la BD ===");

        long personasExistentes = personaService.buscar(null, null, null, null, null, Sort.unsorted()).size();
        if (personasExistentes > 0) {
            log.info("DataSeeder (Personal): la BD ya tiene {} personas. Se omite el seed.",
                    personasExistentes);
            return;
        }

        // Pre-cargar areas disponibles para resolver por nombre
        Map<String, Long> areasPorNombre = cargarAreasPorNombre();
        if (areasPorNombre.isEmpty()) {
            log.warn("DataSeeder (Personal): no hay areas de trabajo en la BD. "
                    + "Se omite el seed de personas. Ejecute primero DataSeederCatalogo.");
            return;
        }

        // Cargar equipos disponibles desde TeamApiService
        Map<String, Long> equiposPorNombre = cargarEquiposPorNombre();

        log.info("DataSeeder (Personal): BD vacia. Iniciando carga de datos de prueba...");
        log.info("  Areas disponibles: {}", areasPorNombre.keySet());
        log.info("  Equipos disponibles: {}", equiposPorNombre.keySet());

        seedPersonas(equiposPorNombre, areasPorNombre);

        log.info("=== DataSeeder (Personal): carga finalizada ===");
    }

    // -------------------------------------------------------------------
    // Carga de areas
    // -------------------------------------------------------------------

    /**
     * Carga todas las areas de trabajo activas y arma un mapa nombre -> id.
     * Se resuelve UNA sola vez al inicio para no repetir la consulta por
     * cada persona.
     */
    /**
     * Carga todas las areas de trabajo activas y arma un mapa nombre -> id.
     * Consume {@code areaService.listarResumen()} expuesto a través de @NamedInterface("area-service").
     */
    private Map<String, Long> cargarAreasPorNombre() {
        try {
            List<AreaResumen> areas = areaService.listarResumen();
            Map<String, Long> mapa = new HashMap<>();
            for (AreaResumen area : areas) {
                mapa.put(area.nombre(), area.id());
            }
            return mapa;
        } catch (Exception e) {
            log.warn("  [!] No se pudieron cargar las areas de trabajo: {}", e.getMessage());
            return Map.of();
        }
    }

    /**
     * Carga todos los equipos activos y arma un mapa nombre -> id.
     * Consume {@code teamApiService.listarEquiposActivos()} expuesto a través de @NamedInterface("teams-api").
     */
    private Map<String, Long> cargarEquiposPorNombre() {
        try {
            List<TeamSnapshotDTO> equipos = teamApiService.listarEquiposActivos();
            Map<String, Long> mapa = new HashMap<>();
            for (TeamSnapshotDTO equipo : equipos) {
                mapa.put(equipo.nombre(), equipo.idTeam());
            }
            return mapa;
        } catch (Exception e) {
            log.warn("  [!] No se pudieron cargar los equipos: {}", e.getMessage());
            return Map.of();
        }
    }

    // -------------------------------------------------------------------
    // Personas
    // -------------------------------------------------------------------

    private void seedPersonas(Map<String, Long> equipos, Map<String, Long> areasPorNombre) {
        record SeedPersona(
                String nombres, String apellidoPaterno, String apellidoMaterno,
                String documentoIdentidad, String celular,
                LocalDate fechaNacimiento, Sexo sexo,
                String areaNombre, String tallaPolo, String estadoCivil,
                String nivelEducativo, String equipo
        ) {}

        List<SeedPersona> personas = List.of(
                new SeedPersona("Ana", "Lazaro", "Matallana",
                        "11111111", "951357327", LocalDate.of(1980, 3, 16), Sexo.F,
                        "CRAI", "S", "Soltera", "Universitario", "Chirimoya"),

                new SeedPersona("Eulalia", "Soncco", "Ortiz",
                        "22222222", "982627095", LocalDate.of(1975, 8, 19), Sexo.F,
                        "Limpieza", "M", "Casada", "Secundaria", "Chirimoya"),

                new SeedPersona("Barbara", "Bernedo", "Benavente",
                        "33333333", "945520053", LocalDate.of(1968, 12, 4), Sexo.F,
                        "Limpieza", "M", "Viuda", "Secundaria", "Chirimoya"),

                new SeedPersona("Freyde Job", "Mayhuiri", "Chincheromi",
                        "44444444", "914420182", LocalDate.of(1985, 2, 9), Sexo.M,
                        "DTI-Redes", "M", "Soltero", "Universitario", "Nutri Bebes"),

                new SeedPersona("Omar Paul", "Huichi", "Aguilar",
                        "55555555", "951752202", LocalDate.of(1988, 1, 25), Sexo.M,
                        "Caja", "M", "Soltero", "Universitaria", "Nutri Bebes"),

                new SeedPersona("Wilmer", "Velasquez", "Mamani",
                        "66666666", "951301909", LocalDate.of(1990, 11, 1), Sexo.M,
                        "DTI-Redes", "M", "Soltero", "Universitario", "Nutria Salvaje"),

                new SeedPersona("Maribel", "Torres", "Quispe",
                        "77777777", "966764335", LocalDate.of(2001, 6, 11), Sexo.F,
                        "Logistica", "M", "Soltera", "Universitaria", "Nutria Salvaje"),

                new SeedPersona("Gabriel", "Leon", "Aviles",
                        "88888888", "914126160", LocalDate.of(1978, 3, 18), Sexo.M,
                        "Limpieza", "L", "Soltero", "Secundaria", "Nutria Salvaje")
        );

        int creadas = 0;
        for (SeedPersona seed : personas) {
            try {
                // Resolver el area por nombre. Si no existe, saltar la persona.
                Long areaId = areasPorNombre.get(seed.areaNombre());
                if (areaId == null) {
                    log.warn("  [!] Area '{}' no encontrada. Se omite a '{} {}'",
                            seed.areaNombre(), seed.nombres(), seed.apellidoPaterno());
                    continue;
                }

                PersonaRequest request = new PersonaRequest(
                        seed.nombres(),
                        seed.apellidoPaterno(),
                        seed.apellidoMaterno(),
                        seed.documentoIdentidad(),
                        seed.celular(),
                        seed.fechaNacimiento(),
                        seed.sexo(),
                        areaId,
                        seed.tallaPolo(),
                        seed.estadoCivil(),
                        seed.nivelEducativo(),
                        equipos.get(seed.equipo())
                );

                PersonaResponse response = personaService.crear(request);
                creadas++;
                log.info("  [+] Persona '{} {}' creada con id={}, edad={}, area={}, equipo={}",
                        response.nombres(), response.apellidoPaterno(),
                        response.id(), response.edad(),
                        response.areaTrabajo() != null ? response.areaTrabajo().nombre() : "-",
                        response.equipo() != null ? response.equipo().nombre() : "-");
            } catch (Exception e) {
                log.warn("  [!] No se pudo crear la persona '{} {}': {}",
                        seed.nombres(), seed.apellidoPaterno(), e.getMessage());
            }
        }
        log.info("DataSeeder (Personal): {} personas cargadas de {} intentos.",
                creadas, personas.size());
    }
}