package upeu.edu.pe.Saludabemente.actividades.configuracion.service;

import upeu.edu.pe.Saludabemente.actividades.configuracion.dto.request.GamificacionRequest;
import upeu.edu.pe.Saludabemente.actividades.configuracion.dto.request.MaterialesRequest;
import upeu.edu.pe.Saludabemente.actividades.configuracion.dto.response.ConfiguracionResponse;

public interface ConfiguracionService {

    ConfiguracionResponse gestionarMateriales(Long idActividad, MaterialesRequest request);

    ConfiguracionResponse configurarPuntaje(Long idActividad, GamificacionRequest request);

    ConfiguracionResponse obtenerConfiguracion(Long idActividad);
}