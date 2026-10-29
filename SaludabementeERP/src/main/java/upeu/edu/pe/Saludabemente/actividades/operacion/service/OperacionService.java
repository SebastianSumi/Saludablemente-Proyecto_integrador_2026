package upeu.edu.pe.Saludabemente.actividades.operacion.service;

import upeu.edu.pe.Saludabemente.actividades.agenda.dto.response.ActividadResponse;
import upeu.edu.pe.Saludabemente.actividades.operacion.dto.request.CancelacionRequest;
import upeu.edu.pe.Saludabemente.actividades.operacion.dto.request.ReprogramacionRequest;
import upeu.edu.pe.Saludabemente.actividades.operacion.dto.request.TransferenciaRequest;

public interface OperacionService {

    ActividadResponse reprogramar(Long id, ReprogramacionRequest request);

    ActividadResponse cancelar(Long id, CancelacionRequest request);

    ActividadResponse iniciar(Long id);

    ActividadResponse finalizar(Long id);

    ActividadResponse transferirCoordinador(Long id, TransferenciaRequest request);
}