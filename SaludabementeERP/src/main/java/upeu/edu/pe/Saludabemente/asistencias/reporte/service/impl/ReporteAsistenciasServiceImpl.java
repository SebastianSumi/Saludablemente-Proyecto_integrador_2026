package upeu.edu.pe.Saludabemente.asistencias.reporte.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import upeu.edu.pe.Saludabemente.asistencias.cuarentena.repository.CuarentenaRepository;
import upeu.edu.pe.Saludabemente.asistencias.enums.EstadoAsistencia;
import upeu.edu.pe.Saludabemente.asistencias.enums.EstadoCuarentena;
import upeu.edu.pe.Saludabemente.asistencias.registro.entity.Asistencia;
import upeu.edu.pe.Saludabemente.asistencias.registro.repository.AsistenciaRepository;
import upeu.edu.pe.Saludabemente.asistencias.reporte.dto.response.*;
import upeu.edu.pe.Saludabemente.asistencias.reporte.service.ReporteAsistenciasService;
import upeu.edu.pe.Saludabemente.asistencias.util.BandasMotivacionales;
import upeu.edu.pe.Saludabemente.asistencias.util.InsigniaCalculator;
import upeu.edu.pe.Saludabemente.asistencias.util.PeriodoUtil;
import upeu.edu.pe.Saludabemente.asistencias.util.RachaCalculator;
import upeu.edu.pe.Saludabemente.exception.BusinessRuleException;
import upeu.edu.pe.Saludabemente.exception.ResourceNotFoundException;
import upeu.edu.pe.Saludabemente.personal.api.PersonalApiService;
import upeu.edu.pe.Saludabemente.personal.api.PersonalSnapshotDTO;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReporteAsistenciasServiceImpl implements ReporteAsistenciasService {

    private final AsistenciaRepository asistenciaRepository;
    private final CuarentenaRepository cuarentenaRepository;
    private final PersonalApiService personalApiService;

    // ============================================================
    // 1. PARTICIPACIÓN POR ACTIVIDAD
    // ============================================================
    @Override
    @Transactional(readOnly = true)
    public ParticipacionActividadResponse participacionPorActividad(Long idActividad) {

        List<Asistencia> asistencias = asistenciaRepository.findByIdActividad(idActividad);
        if (asistencias.isEmpty()) {
            return new ParticipacionActividadResponse(
                    idActividad, 0, 0, 0, 0, 0, 0,
                    BigDecimal.ZERO, BandasMotivacionales.CRITICO, "N/A");
        }

        long asistio = asistencias.stream().filter(this::esAsistencia).count();
        long ausente = asistencias.stream().filter(a -> a.getEstado() == EstadoAsistencia.AUSENTE).count();
        long exonerado = asistencias.stream().filter(a -> a.getEstado() == EstadoAsistencia.EXONERADO).count();
        long incompleta = asistencias.stream().filter(a -> a.getEstado() == EstadoAsistencia.INCOMPLETA).count();
        long total = asistencias.size();

        BigDecimal porcentaje = total > 0
                ? BigDecimal.valueOf(asistio * 100.0 / total).setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        String metodoMasUsado = asistencias.stream()
                .collect(Collectors.groupingBy(
                        a -> a.getMetodoRegistro().name(), Collectors.counting()))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("N/A");

        long cuarentenaPendiente = cuarentenaRepository.countByEstado(EstadoCuarentena.PENDIENTE);

        return new ParticipacionActividadResponse(
                idActividad,
                (int) total,
                (int) asistio,
                (int) ausente,
                (int) exonerado,
                (int) incompleta,
                (int) cuarentenaPendiente,
                porcentaje,
                BandasMotivacionales.porPorcentaje(porcentaje),
                metodoMasUsado
        );
    }

    // ============================================================
    // 2. REPORTE POR PERSONA
    // ============================================================
    @Override
    @Transactional(readOnly = true)
    public ReportePersonaResponse reportePorPersona(Long idPersona, String periodo,
                                                    LocalDate desde, LocalDate hasta) {

        PersonalSnapshotDTO persona = personalApiService.obtenerSnapshot(idPersona);
        if (persona == null) throw new ResourceNotFoundException("Persona no encontrada: " + idPersona);

        PeriodoUtil.Rango rango = resolverRango(periodo, desde, hasta);

        List<Asistencia> historial = asistenciaRepository.findByIdPersona(idPersona)
                .stream()
                .filter(a -> dentroDeRango(a.getHoraMarcado(), rango))
                .toList();

        return construirReportePersona(persona, historial, rango.etiqueta(), null);
    }

    // ============================================================
    // 3. REPORTE POR TEAM
    // ============================================================
    @Override
    @Transactional(readOnly = true)
    public ReporteTeamResponse reportePorTeam(Long idTeam, String periodo,
                                              LocalDate desde, LocalDate hasta, String modo) {

        PeriodoUtil.Rango rango = resolverRango(periodo, desde, hasta);
        String modoUpper = (modo != null) ? modo.toUpperCase() : "PERIODO";

        List<PersonalSnapshotDTO> miembros = personalApiService.listarPorTeam(idTeam);
        if (miembros.isEmpty()) {
            return new ReporteTeamResponse(
                    idTeam, "Sin nombre", rango.etiqueta(), modoUpper, 0, 0,
                    BigDecimal.ZERO, BandasMotivacionales.CRITICO, false,
                    List.of(), 0, 0, List.of(), List.of(), List.of());
        }

        List<Long> idsMiembros = miembros.stream().map(PersonalSnapshotDTO::idPersona).toList();

        List<Asistencia> asistencias = asistenciaRepository
                .findByIdPersonaInAndHoraMarcadoBetween(
                        idsMiembros,
                        rango.desde().atStartOfDay(),
                        rango.hasta().atTime(LocalTime.MAX));

        // Calcular por persona
        Map<Long, List<Asistencia>> porPersona = asistencias.stream()
                .collect(Collectors.groupingBy(Asistencia::getIdPersona));

        List<MiembroRankingResponse> ranking = new ArrayList<>();
        for (PersonalSnapshotDTO m : miembros) {
            List<Asistencia> hist = porPersona.getOrDefault(m.idPersona(), List.of());
            ranking.add(construirMiembroRanking(m, hist, null, null, null));
        }

        // Ordenar por porcentaje DESC
        ranking.sort(Comparator.comparing(MiembroRankingResponse::porcentajeCumplimiento).reversed());
        // Reasignar posiciones
        for (int i = 0; i < ranking.size(); i++) {
            MiembroRankingResponse r = ranking.get(i);
            ranking.set(i, new MiembroRankingResponse(
                    r.idPersona(), r.nombreCompleto(), r.porcentajeCumplimiento(),
                    r.banda(), r.insignias(), i + 1, r.posicionAnterior(),
                    r.movimiento(), r.totalAsistio(), r.totalAusente(),
                    r.rachaAsistenciasActual(), r.rachaFaltasActual(), r.al100()));
        }

        // Totales del team
        long totalActividades = asistencias.stream()
                .map(Asistencia::getIdActividad).distinct().count();

        long totalEsperado = (long) miembros.size() * totalActividades;
        long totalAsistio = asistencias.stream()
                .filter(this::esAsistencia).count();

        BigDecimal porcentajeTeam = totalEsperado > 0
                ? BigDecimal.valueOf(totalAsistio * 100.0 / totalEsperado).setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        boolean al100 = ranking.stream().allMatch(r -> Boolean.TRUE.equals(r.al100()));

        // Miembros al 100% y en riesgo
        int miembrosAl100 = (int) ranking.stream().filter(r -> Boolean.TRUE.equals(r.al100())).count();
        int miembrosEnRiesgo = (int) ranking.stream()
                .filter(r -> "CRITICO".equals(r.banda().codigo()) || "BAJO".equals(r.banda().codigo()))
                .count();

        // Días (solo si modo=DIARIO)
        List<DiaTeamResponse> dias = new ArrayList<>();
        if ("DIARIO".equals(modoUpper)) {
            dias = construirDias(idsMiembros, asistencias, miembros.size());
        }

        // Top faltantes
        List<MiembroRankingResponse> topFaltantes = ranking.stream()
                .sorted(Comparator.comparing(MiembroRankingResponse::totalAusente).reversed())
                .limit(5)
                .toList();

        // Con racha de faltas
        List<Long> conRacha = ranking.stream()
                .filter(r -> r.rachaFaltasActual() != null && r.rachaFaltasActual() >= 3)
                .map(MiembroRankingResponse::idPersona)
                .toList();

        return new ReporteTeamResponse(
                idTeam,
                miembros.get(0).nombreTeam() != null ? miembros.get(0).nombreTeam() : "Team " + idTeam,
                rango.etiqueta(),
                modoUpper,
                miembros.size(),
                (int) totalActividades,
                porcentajeTeam,
                BandasMotivacionales.porPorcentaje(porcentajeTeam),
                al100,
                dias,
                miembrosAl100,
                miembrosEnRiesgo,
                ranking,
                topFaltantes,
                conRacha
        );
    }

    // ============================================================
    // 4. RANKING DE TEAMS
    // ============================================================
    @Override
    @Transactional(readOnly = true)
    public RankingTeamsResponse rankingTeams(String periodo, LocalDate desde, LocalDate hasta, String modo) {

        PeriodoUtil.Rango rango = resolverRango(periodo, desde, hasta);
        String modoUpper = (modo != null) ? modo.toUpperCase() : "PERIODO";

        // Agrupar todos los activos por team
        List<PersonalSnapshotDTO> activos = personalApiService.listarPersonalActivo();
        Map<Long, List<PersonalSnapshotDTO>> porTeam = activos.stream()
                .filter(p -> p.idTeam() != null)
                .collect(Collectors.groupingBy(PersonalSnapshotDTO::idTeam));

        List<TeamRankingResponse> ranking = new ArrayList<>();

        for (Map.Entry<Long, List<PersonalSnapshotDTO>> entry : porTeam.entrySet()) {
            Long idTeam = entry.getKey();
            List<PersonalSnapshotDTO> miembros = entry.getValue();
            List<Long> ids = miembros.stream().map(PersonalSnapshotDTO::idPersona).toList();

            List<Asistencia> asistencias = asistenciaRepository
                    .findByIdPersonaInAndHoraMarcadoBetween(
                            ids,
                            rango.desde().atStartOfDay(),
                            rango.hasta().atTime(LocalTime.MAX));

            long totalActividades = asistencias.stream()
                    .map(Asistencia::getIdActividad).distinct().count();
            long totalEsperado = (long) miembros.size() * totalActividades;
            long totalAsistio = asistencias.stream()
                    .filter(this::esAsistencia).count();
            long totalFaltas = asistencias.stream()
                    .filter(a -> a.getEstado() == EstadoAsistencia.AUSENTE).count();

            BigDecimal pct = totalEsperado > 0
                    ? BigDecimal.valueOf(totalAsistio * 100.0 / totalEsperado).setScale(2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;

            int diasAl100 = 0;
            BigDecimal pctDiasAl100 = BigDecimal.ZERO;
            if ("DIARIO".equals(modoUpper)) {
                List<DiaTeamResponse> dias = construirDias(ids, asistencias, miembros.size());
                diasAl100 = (int) dias.stream().filter(DiaTeamResponse::al100).count();
                pctDiasAl100 = dias.isEmpty() ? BigDecimal.ZERO
                        : BigDecimal.valueOf(diasAl100 * 100.0 / dias.size())
                        .setScale(2, RoundingMode.HALF_UP);
            }

            String nombreTeam = miembros.get(0).nombreTeam() != null
                    ? miembros.get(0).nombreTeam() : "Team " + idTeam;

            ranking.add(new TeamRankingResponse(
                    null, idTeam, nombreTeam, pct,
                    BandasMotivacionales.porPorcentaje(pct),
                    List.of(), "NUEVO", null,
                    miembros.size(), (int) totalFaltas,
                    diasAl100, pctDiasAl100));
        }

        // Ordenar por porcentaje DESC
        ranking.sort(Comparator.comparing(TeamRankingResponse::porcentajeAsistencia).reversed());

        // Reasignar posiciones
        for (int i = 0; i < ranking.size(); i++) {
            TeamRankingResponse r = ranking.get(i);
            ranking.set(i, new TeamRankingResponse(
                    i + 1, r.idTeam(), r.nombreTeam(), r.porcentajeAsistencia(),
                    r.banda(), r.insignias(), r.movimiento(), r.posicionAnterior(),
                    r.totalMiembros(), r.totalFaltas(), r.diasAl100(), r.porcentajeDiasAl100()));
        }

        // Global
        BigDecimal porcentajeGeneral = ranking.isEmpty()
                ? BigDecimal.ZERO
                : BigDecimal.valueOf(ranking.stream()
                        .mapToDouble(r -> r.porcentajeAsistencia().doubleValue())
                        .average().orElse(0.0))
                .setScale(2, RoundingMode.HALF_UP);

        int teamsAl100 = (int) ranking.stream()
                .filter(r -> r.porcentajeAsistencia().compareTo(BigDecimal.valueOf(100)) >= 0)
                .count();
        int teamsEnRiesgo = (int) ranking.stream()
                .filter(r -> "CRITICO".equals(r.banda().codigo()) || "BAJO".equals(r.banda().codigo()))
                .count();

        Map<String, Object> destacados = new HashMap<>();
        if (!ranking.isEmpty()) {
            destacados.put("mejorTeam", Map.of(
                    "idTeam", ranking.get(0).idTeam(),
                    "nombre", ranking.get(0).nombreTeam()));
        }

        return new RankingTeamsResponse(
                rango.etiqueta(), modoUpper,
                porcentajeGeneral,
                BandasMotivacionales.porPorcentaje(porcentajeGeneral),
                teamsAl100, teamsEnRiesgo, ranking.size(),
                ranking, destacados);
    }

    // ============================================================
    // 5. AUSENTISMO CRÓNICO
    // ============================================================
    @Override
    @Transactional(readOnly = true)
    public List<Long> ausentismoCronico(int minRacha) {
        List<PersonalSnapshotDTO> activos = personalApiService.listarPersonalActivo();
        List<Long> resultado = new ArrayList<>();
        for (PersonalSnapshotDTO p : activos) {
            int racha = RachaCalculator.calcularRachaFaltas(
                    asistenciaRepository.findByIdPersona(p.idPersona()));
            if (racha >= minRacha) resultado.add(p.idPersona());
        }
        return resultado;
    }

    // ============================================================
    // 6. GAMIFICACIÓN
    // ============================================================
    @Override
    @Transactional(readOnly = true)
    public List<Long> asistentesParaGamificacion(Long idActividad) {
        return asistenciaRepository.findByIdActividad(idActividad).stream()
                .filter(a -> a.getEstado() == EstadoAsistencia.ASISTIO)
                .map(Asistencia::getIdPersona)
                .distinct()
                .toList();
    }

    // ============================================================
    // HELPERS
    // ============================================================
    /**
     * TARDE cuenta como asistencia en participación y rankings.
     * Gamificación, rachas e INCOMPLETA mantienen su regla propia.
     */
    private boolean esAsistencia(Asistencia a) {
        return a.getEstado() == EstadoAsistencia.ASISTIO
                || a.getEstado() == EstadoAsistencia.TARDE;
    }

    private PeriodoUtil.Rango resolverRango(String periodo, LocalDate desde, LocalDate hasta) {
        if (periodo != null && !periodo.isBlank()) {
            PeriodoUtil.Rango r = PeriodoUtil.parsear(periodo);
            if (r == null) throw new BusinessRuleException("Formato de periodo inválido: " + periodo);
            return r;
        }
        if (desde != null && hasta != null) {
            return PeriodoUtil.porFechas(desde, hasta);
        }
        throw new BusinessRuleException("Debe enviar 'periodo' o 'desde' y 'hasta'");
    }

    private boolean dentroDeRango(LocalDateTime fecha, PeriodoUtil.Rango rango) {
        if (fecha == null) return false;
        LocalDate f = fecha.toLocalDate();
        return !f.isBefore(rango.desde()) && !f.isAfter(rango.hasta());
    }

    private ReportePersonaResponse construirReportePersona(
            PersonalSnapshotDTO persona, List<Asistencia> historial,
            String etiqueta, Integer posicionEnTeam) {

        long total = historial.size();
        long asistio = historial.stream().filter(this::esAsistencia).count();
        long ausente = historial.stream().filter(a -> a.getEstado() == EstadoAsistencia.AUSENTE).count();
        long exonerado = historial.stream().filter(a -> a.getEstado() == EstadoAsistencia.EXONERADO).count();
        long incompleta = historial.stream().filter(a -> a.getEstado() == EstadoAsistencia.INCOMPLETA).count();

        BigDecimal porcentaje = total > 0
                ? BigDecimal.valueOf(asistio * 100.0 / total).setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        int rachaPos = RachaCalculator.calcularRachaAsistencias(historial);
        int rachaFaltas = RachaCalculator.calcularRachaFaltas(historial);

        List<InsigniaResponse> insignias = InsigniaCalculator.calcular(
                historial, porcentaje, false, false);

        return new ReportePersonaResponse(
                persona.idPersona(),
                persona.nombreCompleto(),
                persona.idTeam(),
                persona.nombreTeam(),
                etiqueta,
                porcentaje,
                BandasMotivacionales.porPorcentaje(porcentaje),
                insignias,
                (int) total,
                (int) asistio,
                (int) ausente,
                (int) exonerado,
                (int) incompleta,
                rachaPos,
                rachaPos,
                rachaFaltas,
                posicionEnTeam,
                null,
                "NUEVO",
                porcentaje.compareTo(BigDecimal.valueOf(100)) >= 0
        );
    }

    private MiembroRankingResponse construirMiembroRanking(
            PersonalSnapshotDTO m, List<Asistencia> historial,
            Integer posicion, Integer posicionAnterior, String movimiento) {

        long total = historial.size();
        long asistio = historial.stream().filter(this::esAsistencia).count();
        long ausente = historial.stream().filter(a -> a.getEstado() == EstadoAsistencia.AUSENTE).count();

        BigDecimal pct = total > 0
                ? BigDecimal.valueOf(asistio * 100.0 / total).setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        return new MiembroRankingResponse(
                m.idPersona(),
                m.nombreCompleto(),
                pct,
                BandasMotivacionales.porPorcentaje(pct),
                InsigniaCalculator.calcular(historial, pct, false, false),
                posicion,
                posicionAnterior,
                movimiento != null ? movimiento : "NUEVO",
                (int) asistio,
                (int) ausente,
                RachaCalculator.calcularRachaAsistencias(historial),
                RachaCalculator.calcularRachaFaltas(historial),
                pct.compareTo(BigDecimal.valueOf(100)) >= 0
        );
    }

    private List<DiaTeamResponse> construirDias(
            List<Long> idsMiembros, List<Asistencia> asistencias, int totalMiembros) {

        Map<LocalDate, List<Asistencia>> porDia = asistencias.stream()
                .filter(a -> a.getHoraMarcado() != null)
                .collect(Collectors.groupingBy(a -> a.getHoraMarcado().toLocalDate()));

        List<DiaTeamResponse> dias = new ArrayList<>();
        for (Map.Entry<LocalDate, List<Asistencia>> e : porDia.entrySet()) {
            long asistieron = e.getValue().stream()
                    .filter(this::esAsistencia).count();
            BigDecimal pct = totalMiembros > 0
                    ? BigDecimal.valueOf(asistieron * 100.0 / totalMiembros).setScale(2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;

            Set<Long> idsPresentes = e.getValue().stream()
                    .filter(this::esAsistencia)
                    .map(Asistencia::getIdPersona)
                    .collect(Collectors.toSet());

            List<Long> faltantes = idsMiembros.stream()
                    .filter(id -> !idsPresentes.contains(id))
                    .toList();

            dias.add(new DiaTeamResponse(
                    e.getKey(), pct,
                    BandasMotivacionales.porPorcentaje(pct),
                    (int) asistieron, totalMiembros,
                    faltantes,
                    pct.compareTo(BigDecimal.valueOf(100)) >= 0
            ));
        }
        dias.sort(Comparator.comparing(DiaTeamResponse::fecha));
        return dias;
    }
}