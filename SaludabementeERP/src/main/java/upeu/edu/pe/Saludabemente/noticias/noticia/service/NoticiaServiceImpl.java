package upeu.edu.pe.Saludabemente.noticias.noticia.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import upeu.edu.pe.Saludabemente.catalogo.area.dto.response.AreaResumen;
import upeu.edu.pe.Saludabemente.catalogo.area.service.AreaService;
import upeu.edu.pe.Saludabemente.exception.BusinessRuleException;
import upeu.edu.pe.Saludabemente.exception.ResourceNotFoundException;
import upeu.edu.pe.Saludabemente.noticias.categoria.dto.CategoriaResponseDTO;
import upeu.edu.pe.Saludabemente.noticias.categoria.entity.CategoriaEntity;
import upeu.edu.pe.Saludabemente.noticias.categoria.mapper.CategoriaMapper;
import upeu.edu.pe.Saludabemente.noticias.categoria.repository.CategoriaRepository;
import upeu.edu.pe.Saludabemente.noticias.noticia.dto.NoticiaFeedResponseDTO;
import upeu.edu.pe.Saludabemente.noticias.noticia.dto.NoticiaRequestDTO;
import upeu.edu.pe.Saludabemente.noticias.noticia.dto.NoticiaResponseDTO;
import upeu.edu.pe.Saludabemente.noticias.noticia.entity.NoticiaEntity;
import upeu.edu.pe.Saludabemente.noticias.noticia.entity.NoticiaTeamEntity;
import upeu.edu.pe.Saludabemente.noticias.noticia.mapper.NoticiaMapper;
import upeu.edu.pe.Saludabemente.noticias.noticia.repository.NoticiaRepository;
import upeu.edu.pe.Saludabemente.noticias.noticia.repository.NoticiaTeamRepository;
import upeu.edu.pe.Saludabemente.noticias.shared.enums.EstadoNoticia;
import upeu.edu.pe.Saludabemente.personal.api.PersonalNoticiasApiService;
import upeu.edu.pe.Saludabemente.seguridad.api.noticias.NoticiasUsuarioApi;
import upeu.edu.pe.Saludabemente.seguridad.api.noticias.UsuarioNoticiasDTO;
import upeu.edu.pe.Saludabemente.shared.storage.AlmacenamientoImagenService;
import upeu.edu.pe.Saludabemente.teams.api.TeamApiService;
import upeu.edu.pe.Saludabemente.teams.api.TeamSnapshotDTO;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NoticiaServiceImpl implements NoticiaService {

    private final NoticiaRepository noticiaRepository;
    private final NoticiaTeamRepository noticiaTeamRepository;
    private final CategoriaRepository categoriaRepository;
    private final CategoriaMapper categoriaMapper;
    private final NoticiaMapper noticiaMapper;
    private final AlmacenamientoImagenService almacenamientoImagenService;
    private final NoticiasUsuarioApi noticiasUsuarioApi;
    private final PersonalNoticiasApiService personalNoticiasApiService;
    private final TeamApiService teamApiService;
    private final AreaService areaService;
    private final upeu.edu.pe.Saludabemente.noticias.api.impl.NoticiaSocialSyncService socialSync;

    @Override
    @Transactional
    public NoticiaResponseDTO crearBorrador(NoticiaRequestDTO request) {
        noticiasUsuarioApi.validarAutorActivo(request.getIdUsuarioAutor());
        CategoriaEntity categoria = buscarCategoriaActiva(request.getIdCategoria());
        validarSegmentacion(request);

        NoticiaEntity entity = noticiaMapper.toEntity(request);
        entity.setEstado(EstadoNoticia.BORRADOR);
        entity.setFechaPublicacion(null);

        NoticiaEntity guardada = noticiaRepository.save(entity);
        guardarTeams(guardada.getId(), request.getIdsTeams());
        return enriquecer(guardada, categoria, request.getIdArea());
    }

    @Override
    @Transactional
    public NoticiaResponseDTO actualizarBorrador(Long id, NoticiaRequestDTO request) {
        NoticiaEntity entity = buscarOFallar(id);

        if (entity.getEstado() != EstadoNoticia.BORRADOR) {
            throw new BusinessRuleException(
                    "Solo se pueden editar noticias en estado BORRADOR. Estado actual: " + entity.getEstado());
        }

        noticiasUsuarioApi.validarAutorActivo(request.getIdUsuarioAutor());
        CategoriaEntity categoria = buscarCategoriaActiva(request.getIdCategoria());
        validarSegmentacion(request);

        entity.setTitulo(request.getTitulo());
        entity.setSubtitulo(request.getSubtitulo());
        entity.setResumen(request.getResumen());
        entity.setOficial(request.isOficial());
        entity.setIdEquipoAutor(request.getIdEquipoAutor());
        entity.setUrlCompleta(request.getUrlCompleta());
        entity.setContenido(request.getContenido());
        entity.setIdCategoria(request.getIdCategoria());
        entity.setIdArea(request.getIdArea());
        entity.setIdUsuarioAutor(request.getIdUsuarioAutor());

        NoticiaEntity guardada = noticiaRepository.save(entity);

        noticiaTeamRepository.deleteByIdNoticia(id);
        guardarTeams(id, request.getIdsTeams());

        return enriquecer(guardada, categoria, request.getIdArea());
    }

    @Override
    @Transactional
    public NoticiaResponseDTO subirImagen(Long id, MultipartFile imagen) {
        NoticiaEntity entity = buscarOFallar(id);

        if (entity.getEstado() == EstadoNoticia.ARCHIVADO) {
            throw new BusinessRuleException("No se puede modificar la imagen de una noticia archivada");
        }
        if (imagen == null || imagen.isEmpty()) {
            throw new BusinessRuleException("La imagen es obligatoria");
        }

        eliminarObjetoAnterior(entity.getImagenUrl());
        String objetoNuevo = almacenamientoImagenService.guardar(imagen, "noticias");
        entity.setImagenUrl(almacenamientoImagenService.construirUrlPublica(objetoNuevo));
        noticiaRepository.saveAndFlush(entity);
        socialSync.sincronizar(id);
        return enriquecer(noticiaRepository.save(entity), null, entity.getIdArea());
    }

    @Override
    @Transactional
    public void eliminarImagen(Long id) {
        NoticiaEntity entity = buscarOFallar(id);

        if (entity.getEstado() == EstadoNoticia.ARCHIVADO) {
            throw new BusinessRuleException("No se puede modificar la imagen de una noticia archivada");
        }

        eliminarObjetoAnterior(entity.getImagenUrl());
        entity.setImagenUrl(null);
        noticiaRepository.save(entity);
        socialSync.sincronizar(id);
    }

    private void eliminarObjetoAnterior(String imagenUrl) {
        if (imagenUrl == null || imagenUrl.isBlank()) {
            return;
        }
        almacenamientoImagenService.eliminar(imagenUrl);
    }

    @Override
    @Transactional
    public NoticiaResponseDTO publicar(Long id) {
        NoticiaEntity entity = buscarOFallar(id);

        if (entity.getEstado() == EstadoNoticia.PUBLICADO) {
            throw new BusinessRuleException("La noticia ya está publicada");
        }
        if (entity.getEstado() == EstadoNoticia.ARCHIVADO) {
            throw new BusinessRuleException("No se puede publicar una noticia archivada");
        }
        if (entity.getTitulo() == null || entity.getTitulo().isBlank()
                || entity.getContenido() == null || entity.getContenido().isBlank()) {
            throw new BusinessRuleException("La noticia debe tener título y contenido antes de publicarse");
        }

        entity.setEstado(EstadoNoticia.PUBLICADO);
        entity.setFechaPublicacion(LocalDateTime.now());
        noticiaRepository.saveAndFlush(entity);
        socialSync.sincronizar(id);
        return enriquecer(noticiaRepository.save(entity), null, entity.getIdArea());
    }

    @Override
    @Transactional
    public NoticiaResponseDTO despublicar(Long id) {
        NoticiaEntity entity = buscarOFallar(id);

        if (entity.getEstado() != EstadoNoticia.PUBLICADO) {
            throw new BusinessRuleException("Solo se puede despublicar una noticia PUBLICADA");
        }

        entity.setEstado(EstadoNoticia.BORRADOR);
        entity.setFechaPublicacion(null);
        noticiaRepository.saveAndFlush(entity);
        socialSync.sincronizar(id);
        return enriquecer(noticiaRepository.save(entity), null, entity.getIdArea());
    }

    @Override
    @Transactional
    public NoticiaResponseDTO archivar(Long id) {
        NoticiaEntity entity = buscarOFallar(id);

        if (entity.getEstado() != EstadoNoticia.PUBLICADO) {
            throw new BusinessRuleException("Solo se puede archivar una noticia PUBLICADA");
        }

        entity.setEstado(EstadoNoticia.ARCHIVADO);
        noticiaRepository.saveAndFlush(entity);
        socialSync.sincronizar(id);
        return enriquecer(noticiaRepository.save(entity), null, entity.getIdArea());
    }

    @Override
    @Transactional(readOnly = true)
    public NoticiaResponseDTO obtenerDetalleAdmin(Long id) {
        NoticiaEntity entity = buscarOFallar(id);
        return enriquecer(entity, null, entity.getIdArea());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NoticiaResponseDTO> listarAdmin(EstadoNoticia estado, Long idCategoria,
                                                Long idArea, Long idTeam, Pageable pageable) {
        List<Long> idsTeamFiltro = idTeam != null
                ? noticiaTeamRepository.findIdsNoticiasByIdTeam(idTeam)
                : Collections.emptyList();

        Page<NoticiaEntity> page = noticiaRepository.buscarAdmin(
                estado, idCategoria, idArea, idTeam, idsTeamFiltro, pageable);

        List<NoticiaResponseDTO> dtos = page.getContent().stream()
                .map(e -> enriquecer(e, null, e.getIdArea()))
                .toList();

        return new PageImpl<>(dtos, pageable, page.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NoticiaFeedResponseDTO> listarFeedPublico(Long idCategoria, Long idPersona, Pageable pageable) {
        Long idArea = null;
        Long idTeam = null;

        if (idPersona != null) {
            idArea = personalNoticiasApiService.obtenerAreaDePersona(idPersona)
                    .map(AreaResumen::id).orElse(null);
            idTeam = personalNoticiasApiService.obtenerTeamDePersona(idPersona)
                    .map(TeamSnapshotDTO::idTeam).orElse(null);
        }

        Pageable pageableSinSort = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize());

        return noticiaRepository.buscarFeedPublico(idCategoria, idArea, idTeam, pageableSinSort);
    }

    @Override
    @Transactional(readOnly = true)
    public NoticiaResponseDTO obtenerDetallePublico(Long id, Long idPersona) {
        NoticiaEntity entity = noticiaRepository.findByIdAndEstado(id, EstadoNoticia.PUBLICADO)
                .orElseThrow(() -> new ResourceNotFoundException("Noticia publicada no encontrada: " + id));

        if (entity.getIdArea() != null && idPersona != null) {
            boolean pertenece = personalNoticiasApiService.personaPerteneceAArea(idPersona, entity.getIdArea());
            if (!pertenece) {
                throw new ResourceNotFoundException("Noticia no disponible para el colaborador");
            }
        }
        return enriquecer(entity, null, entity.getIdArea());
    }

    private NoticiaEntity buscarOFallar(Long id) {
        return noticiaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Noticia no encontrada: " + id));
    }

    private CategoriaEntity buscarCategoriaActiva(Long idCategoria) {
        CategoriaEntity c = categoriaRepository.findById(idCategoria)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada: " + idCategoria));
        if (Boolean.FALSE.equals(c.getActivo())) {
            throw new BusinessRuleException("La categoría está inactiva: " + c.getNombre());
        }
        return c;
    }

    private void validarSegmentacion(NoticiaRequestDTO request) {
        if (request.getIdEquipoAutor() != null) {
            teamApiService.validarEquipos(List.of(request.getIdEquipoAutor()));
        }
        if (request.getIdArea() != null) {
            areaService.buscarOFallar(request.getIdArea());
        }
        if (request.getIdsTeams() != null && !request.getIdsTeams().isEmpty()) {
            teamApiService.validarEquipos(request.getIdsTeams());
        }
    }

    private void guardarTeams(Long idNoticia, List<Long> idsTeams) {
        if (idsTeams == null || idsTeams.isEmpty()) return;
        for (Long idTeam : idsTeams) {
            noticiaTeamRepository.save(NoticiaTeamEntity.builder()
                    .idNoticia(idNoticia)
                    .idTeam(idTeam)
                    .build());
        }
    }

    private AreaResumen resolverArea(Long idArea) {
        if (idArea == null) return null;
        return areaService.obtenerAreaResumen(idArea).orElse(null);
    }

    private NoticiaResponseDTO enriquecer(NoticiaEntity entity, CategoriaEntity categoriaOverride, Long idArea) {
        NoticiaResponseDTO dto = noticiaMapper.toResponse(entity);

        // Normalizar la URL pública de la portada
        if (entity.getImagenUrl() != null && !entity.getImagenUrl().isBlank()) {
            dto.setImagenUrl(almacenamientoImagenService.construirUrlPublica(entity.getImagenUrl()));
        }

        CategoriaEntity categoria = categoriaOverride != null
                ? categoriaOverride
                : categoriaRepository.findById(entity.getIdCategoria()).orElse(null);
        if (categoria != null) {
            CategoriaResponseDTO catDto = categoriaMapper.toResponse(categoria);
            catDto.setCantidadNoticias(0);
            dto.setCategoria(catDto);
        }

        dto.setArea(resolverArea(idArea));

        List<Long> idsTeam = noticiaTeamRepository.findByIdNoticia(entity.getId()).stream()
                .map(NoticiaTeamEntity::getIdTeam).toList();
        if (!idsTeam.isEmpty()) {
            dto.setTeams(teamApiService.obtenerEquiposPorIds(idsTeam));
        } else {
            dto.setTeams(Collections.emptyList());
        }

        try {
            UsuarioNoticiasDTO autor = noticiasUsuarioApi.obtenerPorId(entity.getIdUsuarioAutor());
            dto.setNombreAutor(autor.nombreCompleto());
        } catch (ResourceNotFoundException e) {
            dto.setNombreAutor("Desconocido");
        }

        dto.setEstado(entity.getEstado().name());
        return dto;
    }
}
