package upeu.edu.pe.Saludabemente.notificaciones.notificacion.service.plantilla;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import upeu.edu.pe.Saludabemente.exception.ResourceNotFoundException;
import upeu.edu.pe.Saludabemente.notificaciones.notificacion.entity.PlantillaMensaje;
import upeu.edu.pe.Saludabemente.notificaciones.notificacion.repository.PlantillaMensajeRepository;
import upeu.edu.pe.Saludabemente.notificaciones.shared.enums.CanalNotificacion;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class PlantillaEngineService {

    private static final Pattern PLACEHOLDER = Pattern.compile("\\{\\{(.*?)\\}\\}");

    private final PlantillaMensajeRepository plantillaRepository;

    public String renderizarCuerpo(String codigoEvento,
                                   CanalNotificacion canal,
                                   Map<String, Object> variables) {
        PlantillaMensaje plantilla = buscarOFallar(codigoEvento, canal);
        return reemplazar(plantilla.getCuerpoHtml(), variables);
    }

    public String renderizarAsunto(String codigoEvento,
                                   CanalNotificacion canal,
                                   Map<String, Object> variables) {
        PlantillaMensaje plantilla = buscarOFallar(codigoEvento, canal);
        return reemplazar(plantilla.getAsunto(), variables);
    }

    private PlantillaMensaje buscarOFallar(String codigoEvento, CanalNotificacion canal) {
        return plantillaRepository
                .findByCodigoEventoAndCanalAndActivoTrue(codigoEvento, canal)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Plantilla no encontrada para evento '" + codigoEvento
                                + "' en canal " + canal));
    }

    private String reemplazar(String texto, Map<String, Object> variables) {
        if (texto == null || texto.isBlank()) {
            return texto;
        }
        Matcher matcher = PLACEHOLDER.matcher(texto);
        StringBuilder sb = new StringBuilder();
        while (matcher.find()) {
            String clave = matcher.group(1).trim();
            Object valor = variables.getOrDefault(clave, "");
            matcher.appendReplacement(sb, Matcher.quoteReplacement(valor.toString()));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }
}