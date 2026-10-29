package upeu.edu.pe.Saludabemente.personal.credencial.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import upeu.edu.pe.Saludabemente.personal.credencial.entity.CredencialPrograma;
import upeu.edu.pe.Saludabemente.personal.credencial.entity.EstadoCredencial;

import java.util.List;
import java.util.Optional;

public interface CredencialRepository extends JpaRepository<CredencialPrograma, Long> {

    /**
     * Verificación de QR para Asistencia (servicio público).
     */
    Optional<CredencialPrograma> findByCodigoQrHashAndEstado(
            String codigoQrHash,
            EstadoCredencial estado
    );

    /**
     * Credencial activa de una persona. A lo más una.
     */
    Optional<CredencialPrograma> findByPersonaIdAndEstado(
            Long idPersona,
            EstadoCredencial estado
    );

    /**
     * Historial de credenciales de una persona, más reciente primero.
     */
    List<CredencialPrograma> findByPersonaIdOrderByFechaEmisionDesc(Long idPersona);

    /**
     * Update masivo: pasa a un estado dado TODAS las credenciales de una persona.
     * Se usa al inactivar una persona y al rotar credencial.
     */
    @Modifying
    @Query("""
        UPDATE CredencialPrograma c
           SET c.estado = :estado
         WHERE c.persona.id = :idPersona
        """)
    int actualizarEstadoPorPersona(
            @Param("idPersona") Long idPersona,
            @Param("estado") EstadoCredencial estado
    );
}