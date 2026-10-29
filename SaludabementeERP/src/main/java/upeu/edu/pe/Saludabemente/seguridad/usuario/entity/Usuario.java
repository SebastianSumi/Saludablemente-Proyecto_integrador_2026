package upeu.edu.pe.Saludabemente.seguridad.usuario.entity;

import jakarta.persistence.*;
import lombok.*;
import upeu.edu.pe.Saludabemente.seguridad.rol.entity.Rol;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Cuenta digital del sistema Saludablemente.
 *
 * <p><b>Snapshot de identidad:</b> los campos con sufijo {@code *Snapshot}
 * son copias desnormalizadas del módulo Personal, capturadas en el momento
 * del alta. Son inmutables por diseño (capa anticorrupción). Personal nunca
 * se consulta de nuevo para este usuario, salvo una operación explícita
 * de refresh (fuera de esta fase).</p>
 *
 * <p><b>Roles:</b> relación N:M con {@link Rol} a través de la tabla
 * {@code SLB_SEGURIDAD.USUARIO_ROL}. Sin columnas extra en la puente.</p>
 */
@Entity
@Table(name = "USUARIO", schema = "SLB_SEGURIDAD")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private Long id;

    // -------------------------------------------------------------------
    // Campos propios del módulo Seguridad
    // -------------------------------------------------------------------

    @Column(name = "email", nullable = false, unique = true, length = 120)
    private String email;

    @Column(name = "username", nullable = false, unique = true, length = 60)
    private String username;



    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(name = "activo", nullable = false)
    @Builder.Default
    private Boolean activo = true;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    // -------------------------------------------------------------------
    // Snapshot de identidad (capa anticorrupción hacia Personal)
    // -------------------------------------------------------------------

    @Column(name = "id_persona", nullable = false, unique = true)
    private Long idPersonaSnapshot;

    @Column(name = "documento_identidad", nullable = false, length = 15)
    private String documentoIdentidadSnapshot;

    @Column(name = "nombres", nullable = false, length = 100)
    private String nombresSnapshot;

    @Column(name = "apellido_paterno", nullable = false, length = 100)
    private String apellidoPaternoSnapshot;

    @Column(name = "apellido_materno", length = 100)
    private String apellidoMaternoSnapshot;

    @Column(name = "area_trabajo", length = 100)
    private String areaTrabajoSnapshot;

    @Column(name = "equipo_snapshot", length = 100)
    private String equipoSnapshot;

    @Column(name = "activo_persona", nullable = false)
    private Boolean activoPersonaSnapshot;

    @Column(name = "snapshot_fecha", nullable = false)
    private LocalDateTime snapshotFecha;

    // -------------------------------------------------------------------
    // Roles (N:M)
    // -------------------------------------------------------------------

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "USUARIO_ROL",
            schema = "SLB_SEGURIDAD",
            joinColumns = @JoinColumn(name = "id_usuario"),
            inverseJoinColumns = @JoinColumn(name = "id_rol")
    )
    @Builder.Default
    private Set<Rol> roles = new HashSet<>();

    // -------------------------------------------------------------------
    // Helpers de dominio (no exponen la colección mutable directamente)
    // -------------------------------------------------------------------

    public void addRol(Rol rol) {
        this.roles.add(rol);
    }

    public void removeRol(Rol rol) {
        this.roles.remove(rol);
    }

    public boolean tieneRol(String nombreRol) {
        return roles.stream().anyMatch(r -> nombreRol.equals(r.getNombre()));
    }
}