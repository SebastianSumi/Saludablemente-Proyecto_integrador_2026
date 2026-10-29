package upeu.edu.pe.Saludabemente.asistencias.sincronizacion.service;

import upeu.edu.pe.Saludabemente.asistencias.sincronizacion.dto.request.LoteRequest;
import upeu.edu.pe.Saludabemente.asistencias.sincronizacion.dto.response.LoteResponse;

public interface SincronizacionService {

    LoteResponse procesarLote(LoteRequest request);

    boolean ping();
}