package upeu.edu.pe.Saludabemente.catalogo.seeder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import upeu.edu.pe.Saludabemente.catalogo.area.dto.request.AreaRequest;
import upeu.edu.pe.Saludabemente.catalogo.area.dto.response.AreaResponse;
import upeu.edu.pe.Saludabemente.catalogo.area.service.AreaService;
import upeu.edu.pe.Saludabemente.catalogo.periodo.dto.request.PeriodoRequest;
import upeu.edu.pe.Saludabemente.catalogo.periodo.dto.response.PeriodoResponse;
import upeu.edu.pe.Saludabemente.catalogo.periodo.service.PeriodoService;

import java.time.LocalDate;
import java.util.List;

/**
 * Carga inicial de datos del modulo Catalogo: periodos y areas de trabajo.
 *
 * <p>Vive dentro del modulo {@code catalogo} porque consume sus clases
 * internas ({@code PeriodoService}, {@code AreaService}, DTOs).</p>
 *
 * <p><b>Orden:</b> {@code @Order(1)}. Corre ANTES que el DataSeeder de
 * Personal, porque ese necesita las areas de trabajo para asignar a las
 * personas que crea.</p>
 *
 * <p><b>Idempotente:</b> por seccion. Si ya hay periodos, omite periodos.
 * Si ya hay areas, omite areas. Permite agregar areas en una segunda
 * ejecucion sin duplicar periodos.</p>
 */
@Component
@Profile("dev")
@Order(1)
@RequiredArgsConstructor
@Slf4j
public class DataSeederCatalogo implements CommandLineRunner {

    private final PeriodoService periodoService;
    private final AreaService areaService;

    @Override
    public void run(String... args) {
        log.info("=== DataSeederCatalogo: verificando estado de la BD ===");

        seedPeriodos();
        seedAreas();

        log.info("=== DataSeederCatalogo: carga finalizada ===");
    }

    // -------------------------------------------------------------------
    // Periodos
    // -------------------------------------------------------------------

    private void seedPeriodos() {
        long existentes = periodoService.buscar(null, Sort.unsorted()).size();
        if (existentes > 0) {
            log.info("DataSeederCatalogo: la BD ya tiene {} periodos. Se omite el seed de periodos.",
                    existentes);
            return;
        }

        log.info("DataSeederCatalogo: cargando periodos...");

        crearPeriodo("2026-1", "Primer Semestre 2026",
                LocalDate.of(2026, 3, 1), LocalDate.of(2026, 7, 31));

        crearPeriodo("2026-2", "Segundo Semestre 2026",
                LocalDate.of(2026, 8, 1), LocalDate.of(2026, 12, 20));

        crearPeriodo("2025-2", "Segundo Semestre 2025",
                LocalDate.of(2025, 8, 1), LocalDate.of(2025, 12, 20));
    }

    private void crearPeriodo(String codigo, String descripcion,
                              LocalDate inicio, LocalDate fin) {
        try {
            PeriodoRequest request = new PeriodoRequest(codigo, descripcion, inicio, fin);
            PeriodoResponse response = periodoService.crear(request);

            if ("2026-1".equals(codigo)) {
                periodoService.marcarVigente(response.getId());
                log.info("  [+] Periodo '{}' creado y marcado VIGENTE (id={})",
                        codigo, response.getId());
            } else if ("2025-2".equals(codigo)) {
                periodoService.cerrar(response.getId());
                log.info("  [+] Periodo '{}' creado y CERRADO (id={})",
                        codigo, response.getId());
            } else {
                log.info("  [+] Periodo '{}' creado en PLANIFICADO (id={})",
                        codigo, response.getId());
            }
        } catch (Exception e) {
            log.warn("  [!] No se pudo crear el periodo '{}': {}", codigo, e.getMessage());
        }
    }

    // -------------------------------------------------------------------
    // Areas de trabajo
    // -------------------------------------------------------------------

    private void seedAreas() {
        long existentes = areaService.buscar(null, Sort.unsorted()).size();
        if (existentes > 0) {
            log.info("DataSeederCatalogo: la BD ya tiene {} areas. Se omite el seed de areas.",
                    existentes);
            return;
        }

        log.info("DataSeederCatalogo: cargando areas de trabajo...");

        record SeedArea(String nombre, String descripcion) {}

        List<SeedArea> areas = List.of(
                new SeedArea("CRAI",
                        "Centro de Recursos para el Aprendizaje y la Investigacion"),
                new SeedArea("Limpieza",
                        "Personal de limpieza y mantenimiento"),
                new SeedArea("Logistica",
                        "Area de logistica y almacen"),
                new SeedArea("DTI-Redes",
                        "Direccion de Tecnologias de la Informacion - Redes"),
                new SeedArea("Caja",
                        "Area de caja y atencion al publico"),
                new SeedArea("Servicio de Alimentacion",
                        "Comedor y servicio de alimentacion")
        );

        for (SeedArea seed : areas) {
            try {
                AreaRequest request = new AreaRequest(seed.nombre(), seed.descripcion());
                AreaResponse response = areaService.crear(request);
                // AreaResponse es una clase con @Getter/@Setter, NO un record:
                // se usan getters, no la sintaxis de record nombre()/id().
                log.info("  [+] Area '{}' creada con id={}",
                        response.getNombre(), response.getId());
            } catch (Exception e) {
                log.warn("  [!] No se pudo crear el area '{}': {}",
                        seed.nombre(), e.getMessage());
            }
        }
    }
}