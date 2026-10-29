package upeu.edu.pe.Saludabemente.actividades.imagen.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import upeu.edu.pe.Saludabemente.actividades.agenda.entity.Actividad;
import upeu.edu.pe.Saludabemente.actividades.agenda.repository.ActividadRepository;
import upeu.edu.pe.Saludabemente.actividades.imagen.dto.response.ImagenResponse;
import upeu.edu.pe.Saludabemente.actividades.imagen.entity.ActividadImagen;
import upeu.edu.pe.Saludabemente.actividades.imagen.repository.ActividadImagenRepository;
import upeu.edu.pe.Saludabemente.actividades.imagen.service.ImagenService;
import upeu.edu.pe.Saludabemente.exception.BusinessRuleException;
import upeu.edu.pe.Saludabemente.exception.ResourceNotFoundException;
import upeu.edu.pe.Saludabemente.shared.storage.AlmacenamientoImagenService;


import java.util.List;

@Service
@RequiredArgsConstructor
public class ImagenServiceImpl implements ImagenService {

    private final ActividadRepository actividadRepository;
    private final ActividadImagenRepository imagenRepository;
    private final AlmacenamientoImagenService almacenamiento;
    private final upeu.edu.pe.Saludabemente.actividades.integracion.client.ActividadSocialSyncService socialSync;

    private static final int MAX_IMAGENES = 10;
    private static final long MAX_BYTES_LOGO = 2L * 1024 * 1024;    // 2 MB
    private static final long MAX_BYTES_IMAGEN = 5L * 1024 * 1024;  // 5 MB

    // ============================================================
    // Logo (1 por actividad)
    // ============================================================
    @Override
    @Transactional
    public String subirLogo(Long idActividad, MultipartFile archivo) {
        Actividad a = obtenerActividad(idActividad);

        // 1. Subir al almacenamiento con prefijo actividades/{id}/logo
        String key = almacenamiento.guardar(
                archivo, "actividades/" + idActividad + "/logo", MAX_BYTES_LOGO);
        String url = almacenamiento.construirUrlPublica(key);

        // 2. Eliminar logo anterior si existia
        if (a.getLogoKey() != null) {
            almacenamiento.eliminar(a.getLogoKey());
        }

        // 3. Actualizar actividad
        a.setLogoKey(key);
        a.setLogoUrl(url);
        actividadRepository.save(a);
        socialSync.sincronizar(idActividad);
        return url;
    }

    @Override
    @Transactional
    public void eliminarLogo(Long idActividad) {
        Actividad a = obtenerActividad(idActividad);
        if (a.getLogoKey() != null) {
            almacenamiento.eliminar(a.getLogoKey());
            a.setLogoKey(null);
            a.setLogoUrl(null);
            actividadRepository.save(a);
        }
        socialSync.sincronizar(idActividad);
    }

    // ============================================================
    // Galeria (N por actividad)
    // ============================================================
    @Override
    @Transactional
    public ImagenResponse subirImagen(Long idActividad, MultipartFile archivo, String descripcion) {
        Actividad a = obtenerActividad(idActividad);

        // 1. Validar limite
        long actuales = imagenRepository.countByIdActividad(idActividad);
        if (actuales >= MAX_IMAGENES) {
            throw new BusinessRuleException(
                    "La actividad ya tiene el maximo de " + MAX_IMAGENES + " imagenes");
        }

        // 2. Subir al almacenamiento
        String key = almacenamiento.guardar(
                archivo, "actividades/" + idActividad + "/imagenes", MAX_BYTES_IMAGEN);
        String url = almacenamiento.construirUrlPublica(key);

        // 3. Calcular siguiente orden basado en el maximo actual (no en el count,
        //    para evitar colisiones si hubo eliminaciones previas que dejaron huecos)
        int siguienteOrden = imagenRepository.findByIdActividadOrderByOrdenAsc(idActividad)
                .stream()
                .mapToInt(ActividadImagen::getOrden)
                .max()
                .orElse(0) + 1;

        ActividadImagen img = new ActividadImagen(
                idActividad, key, url, descripcion, siguienteOrden);
        ActividadImagen guardada = imagenRepository.save(img);
        socialSync.sincronizar(idActividad);

        return new ImagenResponse(
                guardada.getIdImagen(), guardada.getImagenUrl(),
                guardada.getDescripcion(), guardada.getOrden());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ImagenResponse> listarImagenes(Long idActividad) {
        return imagenRepository.findByIdActividadOrderByOrdenAsc(idActividad)
                .stream()
                .map(img -> new ImagenResponse(
                        img.getIdImagen(), img.getImagenUrl(),
                        img.getDescripcion(), img.getOrden()))
                .toList();
    }

    @Override
    @Transactional
    public void eliminarImagen(Long idActividad, Long idImagen) {
        ActividadImagen img = imagenRepository.findById(idImagen)
                .orElseThrow(() -> new ResourceNotFoundException("Imagen no encontrada: " + idImagen));

        if (!img.getIdActividad().equals(idActividad)) {
            throw new ResourceNotFoundException(
                    "La imagen " + idImagen + " no pertenece a la actividad " + idActividad);
        }

        almacenamiento.eliminar(img.getImagenKey());
        imagenRepository.delete(img);
        imagenRepository.flush(); // Asegurar que el delete sea visible antes de la renumeracion

        // Re-numerar consecutivamente las imagenes restantes (1, 2, 3...)
        List<ActividadImagen> restantes =
                imagenRepository.findByIdActividadOrderByOrdenAsc(idActividad);
        for (int i = 0; i < restantes.size(); i++) {
            restantes.get(i).setOrden(i + 1);
        }
        imagenRepository.saveAll(restantes);
        socialSync.sincronizar(idActividad);
    }

    // ============================================================
    // Helpers
    // ============================================================
    private Actividad obtenerActividad(Long idActividad) {
        return actividadRepository.findById(idActividad)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Actividad no encontrada: " + idActividad));
    }
}
