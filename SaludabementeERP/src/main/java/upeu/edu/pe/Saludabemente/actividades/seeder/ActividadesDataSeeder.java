package upeu.edu.pe.Saludabemente.actividades.seeder;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import upeu.edu.pe.Saludabemente.actividades.agenda.entity.Actividad;
import upeu.edu.pe.Saludabemente.actividades.agenda.repository.ActividadRepository;
import upeu.edu.pe.Saludabemente.actividades.configuracion.entity.ActividadMaterial;
import upeu.edu.pe.Saludabemente.actividades.configuracion.repository.ActividadMaterialRepository;
import upeu.edu.pe.Saludabemente.actividades.inscripcion.entity.ActividadInscrito;
import upeu.edu.pe.Saludabemente.actividades.inscripcion.repository.ActividadInscritoRepository;
import upeu.edu.pe.Saludabemente.actividades.integracion.client.SeguridadClientService;
import upeu.edu.pe.Saludabemente.actividades.integracion.dto.CoordinadorSnapshotDto;
import upeu.edu.pe.Saludabemente.personal.api.PersonalActividadesApiService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Component
@Profile("dev")
@Order(30)   // ejecutar despues de los seeders de Personal y Seguridad
@RequiredArgsConstructor
public class ActividadesDataSeeder implements CommandLineRunner {

    private final ActividadRepository actividadRepository;
    private final ActividadMaterialRepository materialRepository;
    private final ActividadInscritoRepository inscritoRepository;
    private final PersonalActividadesApiService personalApiService;
    private final SeguridadClientService seguridadClientService;

    @Override
    @Transactional
    public void run(String... args) {
        if (actividadRepository.count() > 0) {
            System.out.println("[ActividadesSeeder] Ya existen actividades. Se omite.");
            return;
        }

        // ============================================================
        // Resolver ids reales (nunca hardcodear: los ids dependen de la BD).
        // Orden garantizado: Teams(2) < Personal(3) < Seguridad/Usuario(5) < este seeder(30).
        // ============================================================
        List<CoordinadorSnapshotDto> admins =
                seguridadClientService.listarUsuariosPorRol("Administrador");
        if (admins.isEmpty()) {
            System.out.println("[ActividadesSeeder] No hay usuarios Administrador. Se omite el seed.");
            return;
        }
        List<CoordinadorSnapshotDto> coordinadores =
                seguridadClientService.listarUsuariosPorRol("Coordinador");
        if (coordinadores.isEmpty()) {
            System.out.println("[ActividadesSeeder] No hay usuarios Coordinador. Se omite el seed.");
            return;
        }
        Long idCreador = admins.get(0).idUsuario();
        Long idCoordinador = coordinadores.get(0).idUsuario();
        System.out.println("[ActividadesSeeder] Creando actividades de prueba con creador="
                + idCreador + ", coordinador=" + idCoordinador + "...");

        // ============================================================
        // Actividad 1: Ruta Saludable - FINALIZADA
        // ============================================================
        Actividad ruta = crearActividad(
                "Ruta Saludable Octubre",
                "Caminata matutina por el campus universitario",
                LocalDate.now().minusDays(5),
                LocalTime.of(7, 0),
                LocalTime.of(8, 30),
                "Campus UPeU - Entrada principal",
                "FINALIZADA",
                -12.0345, -77.0543,
                100, true, 90,
                "2026-II",
                true, true,
                50,
                idCreador, idCoordinador
        );

        crearMateriales(ruta, List.of(
                "Ropa deportiva",
                "Botella de agua",
                "Protector solar",
                "Gorra"
        ));

        inscribirATodos(ruta);

        // ============================================================
        // Actividad 2: Charla de Nutricion - PROGRAMADA
        // ============================================================
        Actividad charla = crearActividad(
                "Charla de Nutricion",
                "Alimentacion saludable para el personal administrativo",
                LocalDate.now().plusDays(3),
                LocalTime.of(10, 0),
                LocalTime.of(11, 30),
                "Auditorio A - Pabellon 3",
                "PROGRAMADA",
                -12.0350, -77.0550,
                50, false, 90,
                "2026-II",
                true, true,
                30,
                idCreador, idCoordinador
        );

        crearMateriales(charla, List.of(
                "Cuaderno de apuntes",
                "Boligrafo"
        ));

        // ============================================================
        // Actividad 3: Jornada de Salud - EN_CURSO
        // ============================================================
        Actividad jornada = crearActividad(
                "Jornada de Salud Integral",
                "Evaluacion nutricional y aptitud fisica al personal",
                LocalDate.now(),
                LocalTime.of(8, 0),
                LocalTime.of(14, 0),
                "Polideportivo UPeU",
                "EN_CURSO",
                -12.0360, -77.0560,
                75, true, 360,
                "2026-II",
                true, true,
                100,
                idCreador, idCoordinador
        );

        crearMateriales(jornada, List.of(
                "Ropa comoda",
                "Documento de identidad"
        ));

        inscribirATodos(jornada);

        // ============================================================
        // Actividad 4: Taller de Yoga - CANCELADA
        // ============================================================
        Actividad yoga = crearActividad(
                "Taller de Yoga y Relajacion",
                "Sesion de yoga para reducir estres laboral",
                LocalDate.now().plusDays(7),
                LocalTime.of(17, 0),
                LocalTime.of(18, 0),
                "Sala de usos multiples",
                "CANCELADA",
                null, null,
                100, false, 60,
                "2026-II",
                true, true,
                40,
                idCreador, idCoordinador
        );

        crearMateriales(yoga, List.of("Mat de yoga", "Ropa deportiva"));

        System.out.println("[ActividadesSeeder] 4 actividades creadas correctamente.");
    }

    // ============================================================
    // Helpers
    // ============================================================
    private Actividad crearActividad(String nombre, String descripcion,
                                     LocalDate fecha, LocalTime inicio, LocalTime fin,
                                     String lugar, String estado,
                                     Double lat, Double lon, Integer tolerancia,
                                     Boolean requiereCheckInOut, Integer duracionMin,
                                     String periodo,
                                     Boolean permiteAutoservicio, Boolean permiteEscaneo,
                                     Integer puntaje,
                                     Long idUsuarioCreador, Long idUsuarioCoordinador) {

        Actividad a = new Actividad();
        a.setNombre(nombre);
        a.setDescripcion(descripcion);
        a.setFechaActividad(fecha);
        a.setHoraInicio(inicio);
        a.setHoraFin(fin);
        a.setLugar(lugar);
        a.setEstado(estado);
        a.setIdUsuarioCreador(idUsuarioCreador);
        a.setIdUsuarioCoordinador(idUsuarioCoordinador);
        a.setPuntajeSalud(puntaje);

        if (lat != null) {
            a.setLatitudOficial(BigDecimal.valueOf(lat));
        }
        if (lon != null) {
            a.setLongitudOficial(BigDecimal.valueOf(lon));
        }
        a.setToleranciaGeocercaMetros(tolerancia);
        a.setRequiereCheckInOut(requiereCheckInOut);
        a.setDuracionMinutos(duracionMin);
        a.setPeriodo(periodo);
        a.setPermiteAutoservicio(permiteAutoservicio);
        a.setPermiteEscaneoCoordinador(permiteEscaneo);
        a.setCierreAsistenciasRealizado(!"FINALIZADA".equals(estado));

        return actividadRepository.save(a);
    }

    private void crearMateriales(Actividad a, List<String> materiales) {
        List<ActividadMaterial> lista = materiales.stream()
                .map(m -> new ActividadMaterial(a.getIdActividad(), m))
                .toList();
        materialRepository.saveAll(lista);
    }

    private void inscribirATodos(Actividad a) {
        List<Long> idsPersonas = personalApiService.listarPersonasActivasIds();
        if (idsPersonas.isEmpty()) return;

        List<ActividadInscrito> inscritos = idsPersonas.stream()
                .map(id -> new ActividadInscrito(a.getIdActividad(), id, true))
                .toList();
        inscritoRepository.saveAll(inscritos);
    }
}