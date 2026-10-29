package upeu.edu.pe.Saludabemente.seguridad.rol.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Rol de negocio del catálogo SLB_SEGURIDAD.ROL.
 *
 * <p>Catálogo estático: los roles base (Administrador, Evaluador,
 * Coordinador, Colaborador) se insertan vía {@code RolSeeder} en DEV,
 * y vía script DDL en PROD. ROOT se agrega en Sub-fase 2.</p>
 */
@Entity
@Table(name = "ROL", schema = "SLB_SEGURIDAD")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Rol {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_rol")
    private Long id;

    @Column(name = "nombre", nullable = false, unique = true, length = 40)
    private String nombre;

    @Column(name = "descripcion", length = 150)
    private String descripcion;
}