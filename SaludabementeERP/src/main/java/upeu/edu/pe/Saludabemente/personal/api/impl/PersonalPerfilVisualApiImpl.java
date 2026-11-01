package upeu.edu.pe.Saludabemente.personal.api.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import upeu.edu.pe.Saludabemente.exception.BusinessRuleException;
import upeu.edu.pe.Saludabemente.exception.ResourceNotFoundException;
import upeu.edu.pe.Saludabemente.personal.api.PersonaAvatarDTO;
import upeu.edu.pe.Saludabemente.personal.api.PersonalPerfilVisualApi;
import upeu.edu.pe.Saludabemente.personal.api.PersonalPerfilVisualDTO;
import upeu.edu.pe.Saludabemente.personal.persona.entity.Persona;
import upeu.edu.pe.Saludabemente.personal.persona.repository.PersonaRepository;
import upeu.edu.pe.Saludabemente.shared.storage.AlmacenamientoImagenService;

import java.util.List;

/** API visual independiente de los servicios de gestion de Persona y Teams. */
@Service
@RequiredArgsConstructor
public class PersonalPerfilVisualApiImpl implements PersonalPerfilVisualApi {

    private final PersonaRepository personaRepository;
    private final AlmacenamientoImagenService almacenamiento;

    @Override
    @Transactional(readOnly = true)
    public PersonalPerfilVisualDTO obtenerPerfilVisual(Long idPersona) {
        return visual(personaActiva(idPersona));
    }

    @Override
    @Transactional
    public PersonalPerfilVisualDTO actualizarPresentacionSocial(Long idPersona, String presentacion) {
        if (presentacion != null && presentacion.length() > 300) {
            throw new BusinessRuleException("Presentacion: maximo 300 caracteres");
        }
        Persona p = personaActiva(idPersona);
        p.setPresentacionSocial(presentacion == null || presentacion.isBlank() ? null : presentacion.trim());
        personaRepository.save(p);
        return visual(p);
    }

    @Override
    @Transactional
    public PersonalPerfilVisualDTO subirFotoPerfilSocial(Long idPersona, MultipartFile archivo, boolean portada) {
        Persona p = personaActiva(idPersona);
        long limite = portada ? 5L * 1024 * 1024 : 2L * 1024 * 1024;
        validarFotoSocial(archivo, limite);
        String anterior = portada ? p.getPortadaSocialUrl() : p.getAvatarUrl();
        String prefijo = portada ? "avatares/portadas/" + idPersona : "avatares";
        String objeto = almacenamiento.guardar(archivo, prefijo, limite);
        String url = almacenamiento.construirUrlPublica(objeto);
        if (portada) {
            p.setPortadaSocialUrl(url);
        } else {
            p.setAvatarUrl(url);
        }
        personaRepository.save(p);
        // Mantener el objeto previo si la subida falla; retirar solo referencias propias.
        if (anterior != null && !anterior.equals(url) && anterior.contains("/" + prefijo + "/")) {
            almacenamiento.eliminar(anterior.substring(anterior.indexOf("/" + prefijo + "/") + 1));
        }
        return visual(p);
    }

    private Persona personaActiva(Long id) {
        if (id == null) {
            throw new ResourceNotFoundException("Persona no encontrada");
        }
        return personaRepository.findById(id)
                .filter(p -> Boolean.TRUE.equals(p.getActivo()))
                .orElseThrow(() -> new ResourceNotFoundException("Persona no disponible"));
    }

    private PersonalPerfilVisualDTO visual(Persona p) {
        return new PersonalPerfilVisualDTO(p.getAvatarUrl(), p.getPortadaSocialUrl(), p.getPresentacionSocial());
    }

    private void validarFotoSocial(MultipartFile archivo, long limite) {
        if (archivo == null || archivo.isEmpty() || archivo.getSize() > limite) {
            throw new BusinessRuleException("Selecciona una imagen de hasta " + limite / 1024 / 1024 + " MiB");
        }
        String nombre = archivo.getOriginalFilename() == null ? "" : archivo.getOriginalFilename().toLowerCase(java.util.Locale.ROOT);
        String tipo = archivo.getContentType();
        try (var input = archivo.getInputStream()) {
            byte[] h = input.readNBytes(12);
            boolean jpeg = "image/jpeg".equals(tipo) && (nombre.endsWith(".jpg") || nombre.endsWith(".jpeg"))
                    && h.length >= 3 && (h[0] & 255) == 255 && (h[1] & 255) == 216 && (h[2] & 255) == 255;
            boolean png = "image/png".equals(tipo) && nombre.endsWith(".png")
                    && h.length >= 8 && (h[0] & 255) == 137 && h[1] == 80 && h[2] == 78 && h[3] == 71
                    && h[4] == 13 && h[5] == 10 && h[6] == 26 && h[7] == 10;
            boolean webp = "image/webp".equals(tipo) && nombre.endsWith(".webp")
                    && h.length >= 12 && h[0] == 82 && h[1] == 73 && h[2] == 70 && h[3] == 70
                    && h[8] == 87 && h[9] == 69 && h[10] == 66 && h[11] == 80;
            if (!(jpeg || png || webp)) {
                throw new BusinessRuleException("Selecciona JPEG, PNG o WebP validos");
            }
        } catch (java.io.IOException e) {
            throw new BusinessRuleException("No se pudo leer la imagen");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<PersonaAvatarDTO> obtenerAvatares(List<Long> idsPersona) {
        if (idsPersona == null || idsPersona.isEmpty()) {
            return List.of();
        }
        if (idsPersona.size() > 100) {
            throw new IllegalArgumentException("Maximo 100 personas por consulta de avatares");
        }
        return personaRepository.findAllById(idsPersona).stream()
                .filter(p -> Boolean.TRUE.equals(p.getActivo()))
                .map(p -> new PersonaAvatarDTO(p.getId(), p.getAvatarUrl()))
                .toList();
    }
}
