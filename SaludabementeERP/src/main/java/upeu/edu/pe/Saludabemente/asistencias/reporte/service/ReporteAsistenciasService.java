package upeu.edu.pe.Saludabemente.asistencias.reporte.service;

import upeu.edu.pe.Saludabemente.asistencias.reporte.dto.response.ParticipacionActividadResponse;
import upeu.edu.pe.Saludabemente.asistencias.reporte.dto.response.RankingTeamsResponse;
import upeu.edu.pe.Saludabemente.asistencias.reporte.dto.response.ReportePersonaResponse;
import upeu.edu.pe.Saludabemente.asistencias.reporte.dto.response.ReporteTeamResponse;

import java.time.LocalDate;

public interface ReporteAsistenciasService {

    ParticipacionActividadResponse participacionPorActividad(Long idActividad);

    ReportePersonaResponse reportePorPersona(Long idPersona, String periodo,
                                             LocalDate desde, LocalDate hasta);

    ReporteTeamResponse reportePorTeam(Long idTeam, String periodo,
                                       LocalDate desde, LocalDate hasta, String modo);

    RankingTeamsResponse rankingTeams(String periodo, LocalDate desde, LocalDate hasta, String modo);

    java.util.List<Long> ausentismoCronico(int minRacha);

    java.util.List<Long> asistentesParaGamificacion(Long idActividad);
}