package upeu.edu.pe.Saludabemente.personal.persona.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;
import upeu.edu.pe.Saludabemente.catalogo.area.dto.request.AreaRequest;
import upeu.edu.pe.Saludabemente.catalogo.area.dto.response.AreaResponse;
import upeu.edu.pe.Saludabemente.catalogo.area.service.AreaService;
import upeu.edu.pe.Saludabemente.exception.BusinessRuleException;
import upeu.edu.pe.Saludabemente.exception.ResourceNotFoundException;
import upeu.edu.pe.Saludabemente.personal.api.PersonalActividadesApiService;
import upeu.edu.pe.Saludabemente.personal.persona.dto.request.PersonaRequest;
import upeu.edu.pe.Saludabemente.personal.persona.dto.response.PersonaResponse;
import upeu.edu.pe.Saludabemente.personal.persona.dto.PersonaSnapshotClinicoDTO;
import upeu.edu.pe.Saludabemente.personal.persona.dto.PersonaSnapshotIdentidadDTO;
import upeu.edu.pe.Saludabemente.personal.persona.entity.Sexo;
import upeu.edu.pe.Saludabemente.teams.dto.request.TeamRequest;
import upeu.edu.pe.Saludabemente.teams.service.TeamService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class PersonaServiceIT {

    @Autowired
    private PersonaService personaService;

    @Autowired
    private TeamService teamService;

    @Autowired
    private AreaService areaService;

    @Autowired
    private PersonalActividadesApiService personalActividadesApiService;

    // -------------------------------------------------------------------
    // CRUD basico
    // -------------------------------------------------------------------

    @Test
    void crearYBuscarPersona() {
        Long areaId = areaService.crear(areaRequest("CRAI-test1")).getId();
        Long equipoId = teamService.crear(nuevoEquipo("Chirimoya-test1")).id();

        PersonaRequest request = new PersonaRequest(
                "Ana",
                "Lazaro",
                null,
                "11111111",
                "951000001",
                LocalDate.of(1990, 3, 16),
                Sexo.F,
                areaId,
                null,
                null,
                null,
                equipoId
        );

        PersonaResponse creada = personaService.crear(request);
        assertThat(creada.id()).isNotNull();
        assertThat(creada.documentoIdentidad()).isEqualTo("11111111");
        assertThat(creada.areaTrabajo().nombre()).isEqualTo("CRAI-test1");
        assertThat(creada.equipo().nombre()).isEqualTo("Chirimoya-test1");
        assertThat(creada.edad()).isPositive();

        var lista = personaService.buscar(null, null, null, null, null, Sort.by("apellidoPaterno"));
        assertThat(lista).isNotEmpty();
    }

    // -------------------------------------------------------------------
    // Validaciones de unicidad
    // -------------------------------------------------------------------

    @Test
    void celularDuplicadoActivo_lanzaBusinessRuleException() {
        PersonaRequest request = new PersonaRequest(
                "Ana", "Lazaro", null, "22222222", "951000002",
                LocalDate.of(1990, 3, 16), Sexo.F, null, null, null, null, null
        );

        personaService.crear(request);

        PersonaRequest duplicada = new PersonaRequest(
                "Otra", "Persona", null, "33333333", "951000002",
                LocalDate.of(1985, 1, 1), Sexo.F, null, null, null, null, null
        );

        assertThatThrownBy(() -> personaService.crear(duplicada))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("celular");
    }

    @Test
    void documentoIdentidadDuplicado_lanzaBusinessRuleException() {
        PersonaRequest request = new PersonaRequest(
                "Ana", "Lazaro", null, "44444444", "951000003",
                LocalDate.of(1990, 3, 16), Sexo.F, null, null, null, null, null
        );

        personaService.crear(request);

        PersonaRequest duplicada = new PersonaRequest(
                "Otra", "Persona", null, "44444444", "951000004",
                LocalDate.of(1985, 1, 1), Sexo.F, null, null, null, null, null
        );

        assertThatThrownBy(() -> personaService.crear(duplicada))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("documento");
    }

    // -------------------------------------------------------------------
    // Snapshot clinico
    // -------------------------------------------------------------------

    @Test
    void obtenerSnapshotClinico_devuelveDatosDemograficosCompletos() {
        Long areaId = areaService.crear(areaRequest("CRAI-snap")).getId();

        PersonaRequest request = new PersonaRequest(
                "Ana", "Lazaro", "Matallana", "55555555", "951000005",
                LocalDate.now().minusYears(35), Sexo.F, areaId, null, null, null, null
        );

        PersonaResponse creada = personaService.crear(request);

        PersonaSnapshotClinicoDTO snapshot = personaService.obtenerSnapshotClinico(creada.id());

        assertThat(snapshot.idPersona()).isEqualTo(creada.id());
        assertThat(snapshot.documentoIdentidad()).isEqualTo("55555555");
        assertThat(snapshot.nombresCompletos()).isEqualTo("Ana Lazaro Matallana");
        assertThat(snapshot.celular()).isEqualTo("951000005");
        assertThat(snapshot.edadAlMomento()).isEqualTo(35);
        assertThat(snapshot.sexo()).isEqualTo("F");
        assertThat(snapshot.areaTrabajo()).isEqualTo("CRAI-snap");
        assertThat(snapshot.isActivo()).isTrue();
    }

    @Test
    void obtenerSnapshotClinico_sinArea_areaTrabajoEsNull() {
        PersonaRequest request = new PersonaRequest(
                "Sin", "Area", null, "66666666", "951000006",
                LocalDate.of(1980, 5, 15), Sexo.M, null, null, null, null, null
        );

        PersonaResponse creada = personaService.crear(request);

        PersonaSnapshotClinicoDTO snapshot = personaService.obtenerSnapshotClinico(creada.id());

        assertThat(snapshot.areaTrabajo()).isNull();
    }

    @Test
    void obtenerSnapshotClinico_personaInactiva_devuelveActivoFalse() {
        PersonaRequest request = new PersonaRequest(
                "Pedro", "Loayza", null, "77777777", "951000007",
                LocalDate.of(1980, 5, 15), Sexo.M, null, null, null, null, null
        );

        PersonaResponse creada = personaService.crear(request);
        personaService.inactivar(creada.id());

        PersonaSnapshotClinicoDTO snapshot = personaService.obtenerSnapshotClinico(creada.id());

        assertThat(snapshot.isActivo()).isFalse();
    }

    @Test
    void obtenerSnapshotClinico_idInexistente_lanzaResourceNotFoundException() {
        assertThatThrownBy(() -> personaService.obtenerSnapshotClinico(999999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // -------------------------------------------------------------------
    // Filtros
    // -------------------------------------------------------------------

    @Test
    void buscar_conAreaId_filtraCorrectamente() {
        Long areaA = areaService.crear(areaRequest("Area A")).getId();
        Long areaB = areaService.crear(areaRequest("Area B")).getId();

        PersonaRequest p1 = personaRequest("82000001", "951000111", areaA);
        PersonaRequest p2 = personaRequest("82000002", "951000112", areaB);
        PersonaRequest p3 = personaRequest("82000003", "951000113", areaA);

        personaService.crear(p1);
        personaService.crear(p2);
        personaService.crear(p3);

        var resultado = personaService.buscar(
                null, null, areaA, null, null, Sort.by("apellidoPaterno"));

        assertThat(resultado).hasSize(2);
    }

    @Test
    void buscar_conEquipoId_filtraCorrectamente() {
        Long equipoA = teamService.crear(nuevoEquipo("Equipo A-test")).id();
        Long equipoB = teamService.crear(nuevoEquipo("Equipo B-test")).id();

        PersonaRequest p1 = personaRequest("83000001", "951000201", null, equipoA);
        PersonaRequest p2 = personaRequest("83000002", "951000202", null, equipoB);
        PersonaRequest p3 = personaRequest("83000003", "951000203", null, equipoA);

        personaService.crear(p1);
        personaService.crear(p2);
        personaService.crear(p3);

        var resultado = personaService.buscar(
                null, null, null, equipoA, null, Sort.by("apellidoPaterno"));

        assertThat(resultado).hasSize(2);
    }

    @Test
    void buscar_porNombreODni_filtraCorrectamente() {
        PersonaRequest p1 = new PersonaRequest(
                "Maria Fernanda", "Quispe", "Ramos", "85000001", "951000401",
                LocalDate.of(1990, 1, 1), Sexo.F, null, null, null, null, null
        );
        PersonaRequest p2 = new PersonaRequest(
                "Carlos", "Torres", null, "85000002", "951000402",
                LocalDate.of(1985, 1, 1), Sexo.M, null, null, null, null, null
        );

        personaService.crear(p1);
        personaService.crear(p2);

        var porNombre = personaService.buscar(
                null, null, null, null, "maria", Sort.by("apellidoPaterno"));
        assertThat(porNombre).hasSize(1);
        assertThat(porNombre.get(0).documentoIdentidad()).isEqualTo("85000001");

        var porDni = personaService.buscar(
                null, null, null, null, "85000002", Sort.by("apellidoPaterno"));
        assertThat(porDni).hasSize(1);
        assertThat(porDni.get(0).nombres()).isEqualTo("Carlos");
    }

    // -------------------------------------------------------------------
    // Resolucion batch de celulares
    // -------------------------------------------------------------------

    @Test
    void obtenerIdsPersonaPorCelulares_devuelveMapaConCoincidencias() {
        PersonaRequest p1 = personaRequest("84000001", "951000301", null, null);
        PersonaRequest p2 = personaRequest("84000002", "951000302", null, null);
        PersonaRequest p3 = personaRequest("84000003", "951000303", null, null);

        PersonaResponse creada1 = personaService.crear(p1);
        PersonaResponse creada2 = personaService.crear(p2);
        personaService.crear(p3);

        Set<String> celulares = Set.of(
                "951000301",
                "951000302",
                "951000999"   // no existe
        );

        Map<String, Long> resultado = personaService.obtenerIdsPersonaPorCelulares(celulares);

        assertThat(resultado).hasSize(2);
        assertThat(resultado.get("951000301")).isEqualTo(creada1.id());
        assertThat(resultado.get("951000302")).isEqualTo(creada2.id());
        assertThat(resultado).doesNotContainKey("951000999");
    }

    @Test
    void obtenerIdsPersonaPorCelulares_personaInactiva_noApareceEnMapa() {
        PersonaRequest request = personaRequest("84000004", "951000304", null, null);
        PersonaResponse creada = personaService.crear(request);
        personaService.inactivar(creada.id());

        Map<String, Long> resultado = personaService
                .obtenerIdsPersonaPorCelulares(Set.of("951000304"));

        assertThat(resultado).isEmpty();
    }

    @Test
    void obtenerIdsPersonaPorCelulares_setVacio_devuelveMapaVacio() {
        assertThat(personaService.obtenerIdsPersonaPorCelulares(Set.of())).isEmpty();
        assertThat(personaService.obtenerIdsPersonaPorCelulares(null)).isEmpty();
    }

    // -------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------

    private TeamRequest nuevoEquipo(String nombre) {
        return new TeamRequest(nombre, "Descripcion de " + nombre);
    }

    private AreaRequest areaRequest(String nombre) {
        return new AreaRequest(nombre, "Descripcion de " + nombre);
    }

    private PersonaRequest personaRequest(String dni, String celular, Long areaId) {
        return personaRequest(dni, celular, areaId, null);
    }

    private PersonaRequest personaRequest(String dni, String celular, Long areaId, Long equipoId) {
        return new PersonaRequest(
                "Test",
                "Apellido",
                null,
                dni,
                celular,
                LocalDate.of(1990, 1, 1),
                Sexo.F,
                areaId,
                null,
                null,
                null,
                equipoId
        );
    }


    // -------------------------------------------------------------------
    // Snapshot identidad (contrato con Seguridad)
    // -------------------------------------------------------------------

    @Test
    void obtenerSnapshotIdentidad_devuelveDatosCompletos() {
        Long areaId = areaService.crear(areaRequest("Recursos Humanos")).getId();

        PersonaRequest request = new PersonaRequest(
                "Maria Fernanda",
                "Quispe",
                "Ramos",
                "99000001",
                "951000901",
                LocalDate.of(1990, 5, 20),
                Sexo.F,
                areaId,
                null,
                null,
                null,
                null
        );

        PersonaResponse creada = personaService.crear(request);

        LocalDateTime antes = LocalDateTime.now().minusSeconds(1);
        PersonaSnapshotIdentidadDTO snapshot =
                personaService.obtenerSnapshotIdentidad(creada.id());
        LocalDateTime despues = LocalDateTime.now().plusSeconds(1);

        assertThat(snapshot.idPersona()).isEqualTo(creada.id());
        assertThat(snapshot.documentoIdentidad()).isEqualTo("99000001");
        assertThat(snapshot.nombres()).isEqualTo("Maria Fernanda");
        assertThat(snapshot.apellidoPaterno()).isEqualTo("Quispe");
        assertThat(snapshot.apellidoMaterno()).isEqualTo("Ramos");
        assertThat(snapshot.areaTrabajo()).isEqualTo("Recursos Humanos");
        assertThat(snapshot.activoPersonaSnapshot()).isTrue();
        assertThat(snapshot.snapshotFecha()).isBetween(antes, despues);
    }

    @Test
    void obtenerSnapshotIdentidad_sinApellidoMaternoYArea_devuelveNulls() {
        PersonaRequest request = new PersonaRequest(
                "Juan",
                "Perez",
                null,
                "99000002",
                "951000902",
                LocalDate.of(1985, 1, 1),
                Sexo.M,
                null,
                null,
                null,
                null,
                null
        );

        PersonaResponse creada = personaService.crear(request);

        PersonaSnapshotIdentidadDTO snapshot =
                personaService.obtenerSnapshotIdentidad(creada.id());

        assertThat(snapshot.apellidoMaterno()).isNull();
        assertThat(snapshot.areaTrabajo()).isNull();
        assertThat(snapshot.activoPersonaSnapshot()).isTrue();
        assertThat(snapshot.snapshotFecha()).isNotNull();
    }

    @Test
    void obtenerSnapshotIdentidad_personaInactiva_devuelveActivoFalse() {
        PersonaRequest request = personaRequest("99000003", "951000903", null);
        PersonaResponse creada = personaService.crear(request);
        personaService.inactivar(creada.id());

        PersonaSnapshotIdentidadDTO snapshot =
                personaService.obtenerSnapshotIdentidad(creada.id());

        assertThat(snapshot.activoPersonaSnapshot()).isFalse();
    }

    @Test
    void obtenerSnapshotIdentidad_idInexistente_lanzaResourceNotFoundException() {
        assertThatThrownBy(() -> personaService.obtenerSnapshotIdentidad(999999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999999");
    }

    @Test
    void obtenerSnapshotIdentidad_idNull_lanzaIllegalArgumentException() {
        assertThatThrownBy(() -> personaService.obtenerSnapshotIdentidad(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("obligatorio");
    }

    @Test
    void listarPersonasActivasIds_devuelveSoloActivas() {
        // Crear 2 personas
        PersonaResponse activa = personaService.crear(personaRequest("97000001", "951009201", null));
        PersonaResponse aInactivar = personaService.crear(personaRequest("97000002", "951009202", null));

        // Inactivar la segunda
        personaService.inactivar(aInactivar.id());

        List<Long> ids = personalActividadesApiService.listarPersonasActivasIds();

        assertThat(ids).contains(activa.id());
        assertThat(ids).doesNotContain(aInactivar.id());
    }
}