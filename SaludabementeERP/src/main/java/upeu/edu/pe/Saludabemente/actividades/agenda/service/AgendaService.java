package upeu.edu.pe.Saludabemente.actividades.agenda.service;

import upeu.edu.pe.Saludabemente.actividades.agenda.dto.request.ActividadRequest;
import upeu.edu.pe.Saludabemente.actividades.agenda.dto.request.SedeRequest;
import upeu.edu.pe.Saludabemente.actividades.agenda.dto.response.ActividadDetalleResponse;
import upeu.edu.pe.Saludabemente.actividades.agenda.dto.response.ActividadResponse;
import upeu.edu.pe.Saludabemente.actividades.agenda.dto.response.CalendarioResponse;
import upeu.edu.pe.Saludabemente.actividades.imagen.dto.response.ImagenResponse;

import java.util.List;

public interface AgendaService {

    CalendarioResponse listarParaCalendario(int mes, int anio);

    List<ActividadResponse> obtenerActivasDelDia();

    ActividadResponse crearActividadIndividual(ActividadRequest request);
    ActividadDetalleResponse obtenerPorId(Long id);
    List<ActividadResponse> listarTodas();

    ActividadResponse actualizarSede(Long id, SedeRequest request);
}