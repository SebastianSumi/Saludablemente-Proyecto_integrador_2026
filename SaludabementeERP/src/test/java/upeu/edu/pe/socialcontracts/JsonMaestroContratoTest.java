package upeu.edu.pe.socialcontracts;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import upeu.edu.pe.Saludabemente.social.api.dto.*;
import upeu.edu.pe.Saludabemente.social.api.eventos.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class JsonMaestroContratoTest {
    private final JsonMapper mapper = JsonMapper.builder().build();

    private JsonNode ejemplo(String id) throws Exception {
        JsonNode raiz = mapper.readTree(Files.readString(Path.of("..", "docs", "social", "datos-prueba.json")));
        for (JsonNode ejemplo : raiz.get("ejemplos"))
            if (id.equals(ejemplo.get("id").asText())) return ejemplo.get("body");
        throw new AssertionError("Fixture ausente: " + id);
    }

    @Test void j01DeserializaExactamenteActividadSocialDto() throws Exception {
        var dto = mapper.treeToValue(ejemplo("J01"), ActividadSocialDto.class);
        assertEquals(401L, dto.idActividad()); assertEquals(101L, dto.idUsuarioCreador());
        assertEquals(301L, dto.idEquipoActividad()); assertEquals("09:00", dto.horaInicio().toString());
        assertEquals(1, dto.imagenesUrls().size());
        assertEquals(ejemplo("J01").get("nombre").asText(), dto.nombre());
    }

    @Test void j02DeserializaExactamenteNoticiaSocialDto() throws Exception {
        var dto = mapper.treeToValue(ejemplo("J02"), NoticiaSocialDto.class);
        assertEquals(501L, dto.idNoticia()); assertTrue(dto.oficial());
        assertEquals(101L, dto.idUsuarioAutor());
        assertEquals(LocalDateTime.of(2026, 10, 23, 9, 0), dto.fechaPublicacion());
    }

    @Test void eventosConservanCamposDelMaestroYListaInmutable() {
        LocalDateTime fecha = LocalDateTime.of(2026, 10, 23, 9, 0);
        assertEquals(101L, new ComentarioEnPostEvent(601L, 701L, 102L, 101L, fecha).idUsuarioDestinatario());
        assertEquals(701L, new RespuestaAComentarioEvent(601L, 702L, 701L, 103L, 102L, fecha).idComentarioPadre());
        assertEquals(TipoContenidoMencion.POST, new MencionEvent(1L, TipoContenidoMencion.POST, 601L, 102L, 101L, fecha).tipoContenido());
        assertEquals(TipoReaccion.LOVE, new ReaccionEnPostEvent(601L, 102L, 101L, TipoReaccion.LOVE, fecha).tipo());
        var miembros = new ArrayList<>(List.of(101L, 102L));
        var evento = new NuevoPostDeEquipoEvent(601L, 301L, 103L, miembros, fecha);
        miembros.clear(); assertEquals(2, evento.idUsuariosDestinatarios().size());
        assertThrows(UnsupportedOperationException.class, () -> evento.idUsuariosDestinatarios().add(104L));
    }

    @Test void cincoPlantillasCorrespondenALosCodigosPublicos() throws Exception {
        JsonNode raiz = mapper.readTree(Files.readString(Path.of("..", "docs", "social", "plantillas-social.json")));
        Set<String> codigos = new HashSet<>();
        for (JsonNode plantilla : raiz.get("plantillas")) {
            assertTrue(codigos.add(plantilla.get("codigoEvento").asText()));
            assertEquals("IN_APP", plantilla.get("canal").asText());
        }
        assertEquals(Arrays.stream(upeu.edu.pe.Saludabemente.notificaciones.api.social.TipoAvisoSocial.values())
                .map(Enum::name).collect(java.util.stream.Collectors.toSet()), codigos);
    }
}
