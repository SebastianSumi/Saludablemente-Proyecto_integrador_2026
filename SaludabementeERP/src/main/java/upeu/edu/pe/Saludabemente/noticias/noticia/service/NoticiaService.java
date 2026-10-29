package upeu.edu.pe.Saludabemente.noticias.noticia.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;
import upeu.edu.pe.Saludabemente.noticias.noticia.dto.NoticiaFeedResponseDTO;
import upeu.edu.pe.Saludabemente.noticias.noticia.dto.NoticiaRequestDTO;
import upeu.edu.pe.Saludabemente.noticias.noticia.dto.NoticiaResponseDTO;
import upeu.edu.pe.Saludabemente.noticias.shared.enums.EstadoNoticia;

public interface NoticiaService {

    NoticiaResponseDTO crearBorrador(NoticiaRequestDTO request);

    NoticiaResponseDTO actualizarBorrador(Long id, NoticiaRequestDTO request);

    NoticiaResponseDTO subirImagen(Long id, MultipartFile imagen);

    void eliminarImagen(Long id);

    NoticiaResponseDTO publicar(Long id);

    NoticiaResponseDTO despublicar(Long id);

    NoticiaResponseDTO archivar(Long id);

    NoticiaResponseDTO obtenerDetalleAdmin(Long id);

    Page<NoticiaResponseDTO> listarAdmin(EstadoNoticia estado, Long idCategoria,
                                         Long idArea, Long idTeam, Pageable pageable);

    Page<NoticiaFeedResponseDTO> listarFeedPublico(Long idCategoria, Long idPersona, Pageable pageable);

    NoticiaResponseDTO obtenerDetallePublico(Long id, Long idPersona);
}