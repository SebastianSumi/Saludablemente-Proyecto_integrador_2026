package upeu.edu.pe.Saludabemente.alertaclinica.etiqueta.service;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
/** Catalogo pequeno: serializa escrituras en orden fijo sin cambios de esquema. */
@Component
@RequiredArgsConstructor
public class BloqueoAsociaciones {
 private final EntityManager em;
 @Transactional(propagation = Propagation.MANDATORY)
 public void adquirir() {
  em.createQuery("select p from ParametroEvaluacion p order by p.id", upeu.edu.pe.Saludabemente.alertaclinica.shared.parametro.entity.ParametroEvaluacion.class)
    .setLockMode(LockModeType.PESSIMISTIC_WRITE).getResultList();
  // Bloquear solo cabeceras: evita FOR UPDATE sobre un outer join de la coleccion EAGER en Oracle.
  em.createNativeQuery("select id from SLB_ALERTAS.etiqueta_clinica order by id for update", Long.class)
    .getResultList();
 }
}
