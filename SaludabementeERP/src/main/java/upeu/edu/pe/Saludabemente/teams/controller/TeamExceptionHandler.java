package upeu.edu.pe.Saludabemente.teams.controller;

import java.util.Map;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice(assignableTypes = TeamController.class)
public class TeamExceptionHandler {
    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<Map<String, String>> conflicto(ObjectOptimisticLockingFailureException ex) {
        return ResponseEntity.status(409).body(Map.of("message", "El equipo cambió durante el guardado. Recarga e intenta nuevamente."));
    }
}
