package upeu.edu.pe.Saludabemente.alertaclinica.api.impl;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import upeu.edu.pe.Saludabemente.alertaclinica.api.MotorAlertasApiService;
import upeu.edu.pe.Saludabemente.alertaclinica.motor.service.AnalizadorFisicoService;
import upeu.edu.pe.Saludabemente.alertaclinica.motor.service.AnalizadorNutricionalService;
import upeu.edu.pe.Saludabemente.aptitudfisica.api.EvaluacionFisicaIngresoDto;
import upeu.edu.pe.Saludabemente.nutricional.api.EvaluacionNutricionalIngresoDto;

@Service
@RequiredArgsConstructor
public class MotorAlertasApiServiceImpl implements MotorAlertasApiService {

    private static final Logger log = LoggerFactory.getLogger(MotorAlertasApiServiceImpl.class);

    private final AnalizadorNutricionalService analizadorNutricionalService;
    private final AnalizadorFisicoService analizadorFisicoService;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void procesarNutricional(EvaluacionNutricionalIngresoDto dto) {
        log.debug("Motor de alertas: recibida evaluacion nutricional {} para persona {}",
                dto.idEvaluacionNutricional(), dto.idPersona());
        analizadorNutricionalService.procesar(dto);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void procesarFisica(EvaluacionFisicaIngresoDto dto) {
        log.debug("Motor de alertas: recibida evaluacion fisica {} para persona {}",
                dto.idEvaluacionFisica(), dto.idPersona());
        analizadorFisicoService.procesar(dto);
    }
}