package upeu.edu.pe.Saludabemente.noticias.categoria.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import upeu.edu.pe.Saludabemente.exception.BusinessRuleException;
import upeu.edu.pe.Saludabemente.exception.ResourceNotFoundException;
import upeu.edu.pe.Saludabemente.noticias.categoria.dto.CategoriaRequestDTO;
import upeu.edu.pe.Saludabemente.noticias.categoria.dto.CategoriaResponseDTO;
import upeu.edu.pe.Saludabemente.noticias.categoria.entity.CategoriaEntity;
import upeu.edu.pe.Saludabemente.noticias.categoria.mapper.CategoriaMapper;
import upeu.edu.pe.Saludabemente.noticias.categoria.repository.CategoriaRepository;
import upeu.edu.pe.Saludabemente.noticias.noticia.repository.NoticiaRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoriaServiceImpl implements CategoriaService {

    private final CategoriaRepository categoriaRepository;
    private final NoticiaRepository noticiaRepository;
    private final CategoriaMapper categoriaMapper;

    @Override
    @Transactional(readOnly = true)
    public List<CategoriaResponseDTO> listarActivas() {
        return categoriaRepository.findByActivoTrueOrderByOrdenAsc().stream()
                .map(this::toResponseConConteo)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoriaResponseDTO> listarTodas() {
        return categoriaRepository.findAllByOrderByOrdenAsc().stream()
                .map(this::toResponseConConteo)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CategoriaResponseDTO obtener(Long id) {
        return toResponseConConteo(buscarOFallar(id));
    }

    @Override
    @Transactional
    public CategoriaResponseDTO crear(CategoriaRequestDTO request) {
        if (categoriaRepository.existsByNombre(request.getNombre())) {
            throw new BusinessRuleException("Ya existe una categoría con el nombre: " + request.getNombre());
        }
        CategoriaEntity entity = categoriaMapper.toEntity(request);
        entity.setEsSistema(false);
        entity.setActivo(true);
        if (entity.getOrden() == null) {
            entity.setOrden(0);
        }
        return toResponseConConteo(categoriaRepository.save(entity));
    }

    @Override
    @Transactional
    public CategoriaResponseDTO actualizar(Long id, CategoriaRequestDTO request) {
        CategoriaEntity entity = buscarOFallar(id);
        if (!entity.getNombre().equals(request.getNombre())
                && categoriaRepository.existsByNombreAndIdNot(request.getNombre(), id)) {
            throw new BusinessRuleException("Ya existe una categoría con el nombre: " + request.getNombre());
        }
        entity.setNombre(request.getNombre());
        entity.setDescripcion(request.getDescripcion());
        entity.setColorHex(request.getColorHex());
        entity.setIcono(request.getIcono());
        if (request.getOrden() != null) {
            entity.setOrden(request.getOrden());
        }
        return toResponseConConteo(categoriaRepository.save(entity));
    }

    @Override
    @Transactional
    public CategoriaResponseDTO activar(Long id) {
        CategoriaEntity entity = buscarOFallar(id);
        if (Boolean.TRUE.equals(entity.getActivo())) {
            throw new BusinessRuleException("La categoría ya está activa");
        }
        entity.setActivo(true);
        return toResponseConConteo(categoriaRepository.save(entity));
    }

    @Override
    @Transactional
    public CategoriaResponseDTO inactivar(Long id) {
        CategoriaEntity entity = buscarOFallar(id);
        if (Boolean.FALSE.equals(entity.getActivo())) {
            throw new BusinessRuleException("La categoría ya está inactiva");
        }
        entity.setActivo(false);
        return toResponseConConteo(categoriaRepository.save(entity));
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        CategoriaEntity entity = buscarOFallar(id);
        if (Boolean.TRUE.equals(entity.getEsSistema())) {
            throw new BusinessRuleException(
                    "No se puede eliminar una categoría del sistema. Solo inactivarla");
        }
        long asociadas = noticiaRepository.countPublicadasPorCategoria(id);
        long total = noticiaRepository.countByIdCategoria(id);
        if (total > 0) {
            throw new BusinessRuleException(
                    "No se puede eliminar una categoría con noticias asociadas. Inactívela en su lugar");
        }
        categoriaRepository.delete(entity);
    }

    private CategoriaEntity buscarOFallar(Long id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada: " + id));
    }

    private CategoriaResponseDTO toResponseConConteo(CategoriaEntity entity) {
        CategoriaResponseDTO dto = categoriaMapper.toResponse(entity);
        dto.setCantidadNoticias(noticiaRepository.countByIdCategoria(entity.getId()));
        return dto;
    }
}