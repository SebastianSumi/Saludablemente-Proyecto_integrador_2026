package upeu.edu.pe.Saludabemente.seguridad.rol.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import upeu.edu.pe.Saludabemente.seguridad.rol.entity.Rol;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface RolRepository extends JpaRepository<Rol, Long> {

    /**
     * Trae los roles por sus IDs. Usado en crear/actualizar usuario
     * para validar que no se asignen roles fantasma.
     */
    List<Rol> findByIdIn(Set<Long> ids);

    /**
     * Busca por nombre. Usado por el filtro fantasma para localizar ROOT
     * y por el seeder para validar idempotencia.
     */
    Optional<Rol> findByNombre(String nombre);

    /**
     * ¿Existe un rol con ese nombre? Validación ultrarrápida.
     */
    boolean existsByNombre(String nombre);
}