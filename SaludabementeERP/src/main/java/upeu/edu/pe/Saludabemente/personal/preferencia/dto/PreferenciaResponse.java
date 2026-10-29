package upeu.edu.pe.Saludabemente.personal.preferencia.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PreferenciaResponse {

    private Long id;
    private Long personaId;
    private String canalPreferido;
    private LocalTime horarioContactoInicio;
    private LocalTime horarioContactoFin;
    private Boolean aceptaRecordatorios;
}