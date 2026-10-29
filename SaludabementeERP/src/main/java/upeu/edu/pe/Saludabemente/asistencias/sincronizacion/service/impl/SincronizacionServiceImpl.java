package upeu.edu.pe.Saludabemente.asistencias.sincronizacion.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import upeu.edu.pe.Saludabemente.actividades.api.ActividadApiService;
import upeu.edu.pe.Saludabemente.actividades.api.dto.ActividadSnapshotDto;
import upeu.edu.pe.Saludabemente.asistencias.cuarentena.entity.AsistenciaCuarentena;
import upeu.edu.pe.Saludabemente.asistencias.cuarentena.repository.CuarentenaRepository;
import upeu.edu.pe.Saludabemente.asistencias.enums.*;
import upeu.edu.pe.Saludabemente.asistencias.qr.entity.QrActividad;
import upeu.edu.pe.Saludabemente.asistencias.qr.repository.QrActividadRepository;
import upeu.edu.pe.Saludabemente.asistencias.qr.service.QrActividadService;
import upeu.edu.pe.Saludabemente.asistencias.registro.entity.Asistencia;
import upeu.edu.pe.Saludabemente.asistencias.registro.repository.AsistenciaRepository;
import upeu.edu.pe.Saludabemente.asistencias.sincronizacion.dto.request.LoteRequest;
import upeu.edu.pe.Saludabemente.asistencias.sincronizacion.dto.request.RegistroLoteRequest;
import upeu.edu.pe.Saludabemente.asistencias.sincronizacion.dto.response.LoteResponse;
import upeu.edu.pe.Saludabemente.asistencias.sincronizacion.dto.response.ResultadoRegistroResponse;
import upeu.edu.pe.Saludabemente.asistencias.sincronizacion.service.SincronizacionService;
import upeu.edu.pe.Saludabemente.asistencias.util.ConstantesAsistencia;
import upeu.edu.pe.Saludabemente.asistencias.util.GeocercaUtil;
import upeu.edu.pe.Saludabemente.asistencias.util.QrActividadUtil;
import upeu.edu.pe.Saludabemente.exception.BusinessRuleException;
import upeu.edu.pe.Saludabemente.personal.api.CredencialApiService;
import upeu.edu.pe.Saludabemente.personal.api.PersonalApiService;
import upeu.edu.pe.Saludabemente.personal.api.PersonalSnapshotDTO;
import upeu.edu.pe.Saludabemente.personal.api.VerificacionQrResponse;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SincronizacionServiceImpl implements SincronizacionService {

    private final AsistenciaRepository asistenciaRepository;
    private final CuarentenaRepository cuarentenaRepository;
    private final QrActividadRepository qrActividadRepository;
    private final PersonalApiService personalApiService;
    private final CredencialApiService credencialApiService;
    private final ActividadApiService actividadApiService;
    private final QrActividadService qrActividadService;

    @Override
    public boolean ping() {
        return true;
    }

    @Override
    public LoteResponse procesarLote(LoteRequest request) {

        List<RegistroLoteRequest> registros = request.registros();
        log.info("Procesando lote dispositivo={} tipo={} registros={}",
                request.dispositivoId(), request.tipoFlujo(), registros.size());

        // 1. Deduplicar por idLocal dentro del mismo lote
        Map<String, RegistroLoteRequest> unicos = new LinkedHashMap<>();
        List<ResultadoRegistroResponse> resultados = new ArrayList<>();
        for (RegistroLoteRequest reg : registros) {
            if (reg.idLocal() == null || reg.idLocal().isBlank()) {
                resultados.add(fail(null, "ID_LOCAL_REQUERIDO"));
                continue;
            }
            if (unicos.containsKey(reg.idLocal())) {
                resultados.add(fail(reg.idLocal(), "DUPLICADO_EN_LOTE"));
                continue;
            }
            unicos.put(reg.idLocal(), reg);
        }

        // 2. Cargar existentes (idempotencia)
        List<String> idsLocales = new ArrayList<>(unicos.keySet());
        Set<String> existentesAsistencia = asistenciaRepository
                .findByIdLocalIn(idsLocales).stream()
                .map(Asistencia::getIdLocal)
                .collect(Collectors.toSet());
        Set<String> existentesCuarentena = cuarentenaRepository
                .findByIdLocalIn(idsLocales).stream()
                .map(AsistenciaCuarentena::getIdLocal)
                .collect(Collectors.toSet());

        // 3. Batch de actividades
        Set<Long> idsActividades = unicos.values().stream()
                .map(RegistroLoteRequest::idActividad)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<Long, ActividadSnapshotDto> actividadesMap = new HashMap<>();
        if (!idsActividades.isEmpty()) {
            actividadesMap = actividadApiService.obtenerSnapshots(new ArrayList<>(idsActividades))
                    .stream()
                    .collect(Collectors.toMap(ActividadSnapshotDto::idActividad, a -> a));
        }

        // 4. Procesar registro por registro
        int synced = 0, cuarentena = 0, failed = 0;
        for (RegistroLoteRequest reg : unicos.values()) {
            ResultadoRegistroResponse r = procesarRegistro(
                    reg, request.tipoFlujo(), actividadesMap,
                    existentesAsistencia, existentesCuarentena);
            resultados.add(r);
            switch (r.resultado()) {
                case "SYNCED"    -> synced++;
                case "CUARENTENA"-> cuarentena++;
                case "FAILED"    -> failed++;
            }
        }

        log.info("Lote procesado: synced={} cuarentena={} failed={}", synced, cuarentena, failed);
        return new LoteResponse(registros.size(), synced, cuarentena, failed, resultados);
    }

    private ResultadoRegistroResponse procesarRegistro(
            RegistroLoteRequest reg,
            String tipoFlujo,
            Map<Long, ActividadSnapshotDto> actividadesMap,
            Set<String> existentesAsistencia,
            Set<String> existentesCuarentena) {

        // 1. Idempotencia
        if (existentesAsistencia.contains(reg.idLocal())) {
            return syncedIdempotente(reg.idLocal());
        }
        if (existentesCuarentena.contains(reg.idLocal())) {
            return cuarentenaIdempotente(reg.idLocal());
        }

        // 2. Resolver idPersona según flujo
        Long idPersona = null;
        if ("COORDINADOR".equalsIgnoreCase(tipoFlujo)) {
            VerificacionQrResponse v = credencialApiService
                    .verificarQrParaAsistencia(reg.codigoQrPersonal());
            if (v == null || !v.valido()) {
                return fail(reg.idLocal(), "QR_INVALIDO");
            }
            idPersona = v.idPersona();
        } else if ("AUTOSERVICIO".equalsIgnoreCase(tipoFlujo)) {
            // Nota: idPersona viene del JWT; en este servicio se resuelve vía PersonalApiService
            // o se inyecta desde el controller. Aquí simplificamos: se espera un idPersona
            // resuelto fuera, o se valida en el controller.
            return fail(reg.idLocal(), "AUTOSERVICIO_NO_SOPORTADO_AUN");
        } else {
            return fail(reg.idLocal(), "TIPO_FLUJO_INVALIDO");
        }

        if (idPersona == null) {
            return fail(reg.idLocal(), "PERSONA_NO_RESUELTA");
        }

        // 3. Validar actividad
        ActividadSnapshotDto actividad = actividadesMap.get(reg.idActividad());
        if (actividad == null) {
            return fail(reg.idLocal(), "ACTIVIDAD_NO_EXISTE");
        }
        if (ConstantesAsistencia.ACTIVIDAD_CANCELADA.equals(actividad.estado())) {
            return fail(reg.idLocal(), "ACTIVIDAD_CANCELADA");
        }
        if (ConstantesAsistencia.ACTIVIDAD_FINALIZADA.equals(actividad.estado())) {
            return derivarCuarentena(reg, idPersona, MotivoCuarentena.SYNC_TARDIA,
                    "Actividad finalizada");
        }

        // 3b. QR de actividad obligatorio (validado contra horaCaptura offline).
        if (reg.codigoQrActividad() == null || reg.codigoQrActividad().isBlank()) {
            return fail(reg.idLocal(), "QR_INVALIDO");
        }
        try {
            qrActividadService.validarCodigoQr(reg.codigoQrActividad(), reg.horaCaptura());
        } catch (BusinessRuleException e) {
            String msg = e.getMessage() != null && e.getMessage().toLowerCase().contains("expirado")
                    ? "QR_EXPIRADO" : "QR_INVALIDO";
            return fail(reg.idLocal(), msg);
        } catch (Exception e) {
            log.warn("Error validando QR actividad idLocal={}: {}", reg.idLocal(), e.getMessage());
            return fail(reg.idLocal(), "QR_INVALIDO");
        }

        // 4. Validar persona
        PersonalSnapshotDTO persona = personalApiService.obtenerSnapshot(idPersona);
        if (persona == null) {
            return fail(reg.idLocal(), "PERSONA_NO_EXISTE");
        }
        if (!Boolean.TRUE.equals(persona.activo())) {
            return fail(reg.idLocal(), "PERSONA_INACTIVA");
        }

        // 5. Duplicado por clave de negocio
        boolean existe = asistenciaRepository
                .existsByIdActividadAndIdPersonaAndTipoAsistencia(
                        reg.idActividad(), idPersona, reg.tipoAsistencia());
        if (existe) {
            return fail(reg.idLocal(), "DUPLICADO");
        }

        // 6. Geocerca
        BigDecimal distancia = GeocercaUtil.calcularDistanciaMetros(
                reg.latitud(), reg.longitud(),
                actividad.latitudOficial(), actividad.longitudOficial());

        int tolerancia = actividad.toleranciaGeocercaMetros() != null
                ? actividad.toleranciaGeocercaMetros()
                : ConstantesAsistencia.TOLERANCIA_GEOCERCA_DEFAULT_METROS;

        boolean fuera = distancia != null
                && distancia.compareTo(BigDecimal.valueOf(tolerancia)) > 0;

        if (fuera) {
            return derivarCuarentenaConDistancia(
                    reg, idPersona, MotivoCuarentena.FUERA_GEOCERCA, distancia,
                    "Distancia " + distancia + "m supera " + tolerancia + "m");
        }

        // 7. Insertar en ASISTENCIA
        try {
            Asistencia asistencia = Asistencia.builder()
                    .idLocal(reg.idLocal())
                    .idActividad(reg.idActividad())
                    .idPersona(idPersona)
                    .tipoAsistencia(reg.tipoAsistencia())
                    .metodoRegistro("COORDINADOR".equalsIgnoreCase(tipoFlujo)
                            ? MetodoRegistro.OFFLINE_QR_COORDINADOR
                            : MetodoRegistro.OFFLINE_QR_AUTOSERVICIO)
                    .escaneadoPor("COORDINADOR".equalsIgnoreCase(tipoFlujo)
                            ? EscaneadoPor.COORDINADOR
                            : EscaneadoPor.TITULAR)
                    .estado(EstadoAsistencia.ASISTIO)
                    .horaMarcado(reg.horaCaptura())
                    .latitud(reg.latitud())
                    .longitud(reg.longitud())
                    .distanciaGeocercaM(distancia)
                    .flagSospechoso(false)
                    .dispositivoId(reg.dispositivoId())
                    .syncStatus(SyncStatus.SYNCED)
                    .retryCount(0)
                    .lastSyncAttempt(LocalDateTime.now())
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            Asistencia guardada = asistenciaRepository.save(asistencia);
            return new ResultadoRegistroResponse(
                    reg.idLocal(), "SYNCED", "OK", guardada.getIdAsistencia(), null);
        } catch (Exception e) {
            log.error("Error insertando asistencia idLocal={}", reg.idLocal(), e);
            return fail(reg.idLocal(), "ERROR_INSERT");
        }
    }

    private ResultadoRegistroResponse derivarCuarentena(
            RegistroLoteRequest reg, Long idPersona,
            MotivoCuarentena motivo, String observaciones) {
        return derivarCuarentenaConDistancia(reg, idPersona, motivo, null, observaciones);
    }

    private ResultadoRegistroResponse derivarCuarentenaConDistancia(
            RegistroLoteRequest reg, Long idPersona,
            MotivoCuarentena motivo, BigDecimal distancia, String observaciones) {
        try {
            AsistenciaCuarentena cuarentena = AsistenciaCuarentena.builder()
                    .idLocal(reg.idLocal())
                    .idActividad(reg.idActividad())
                    .idPersona(idPersona)
                    .tipoAsistencia(reg.tipoAsistencia())
                    .horaMarcado(reg.horaCaptura())
                    .latitud(reg.latitud())
                    .longitud(reg.longitud())
                    .distanciaGeocercaM(distancia)
                    .motivo(motivo)
                    .payloadJson(buildPayload(reg))
                    .estado(EstadoCuarentena.PENDIENTE)
                    .observaciones(observaciones)
                    .fechaCreacion(LocalDateTime.now())
                    .build();

            AsistenciaCuarentena guardada = cuarentenaRepository.save(cuarentena);
            return new ResultadoRegistroResponse(
                    reg.idLocal(), "CUARENTENA", motivo.name(), null, guardada.getIdCuarentena());
        } catch (Exception e) {
            log.error("Error insertando cuarentena idLocal={}", reg.idLocal(), e);
            return fail(reg.idLocal(), "ERROR_CUARENTENA");
        }
    }

    private String buildPayload(RegistroLoteRequest reg) {
        return String.format(
                "{\"idLocal\":\"%s\",\"idActividad\":%d,\"tipoAsistencia\":\"%s\",\"horaCaptura\":\"%s\"}",
                reg.idLocal(), reg.idActividad(), reg.tipoAsistencia(), reg.horaCaptura());
    }

    private ResultadoRegistroResponse syncedIdempotente(String idLocal) {
        return new ResultadoRegistroResponse(idLocal, "SYNCED", "IDEMPOTENTE", null, null);
    }

    private ResultadoRegistroResponse cuarentenaIdempotente(String idLocal) {
        return new ResultadoRegistroResponse(idLocal, "CUARENTENA", "IDEMPOTENTE", null, null);
    }

    private ResultadoRegistroResponse fail(String idLocal, String motivo) {
        return new ResultadoRegistroResponse(idLocal, "FAILED", motivo, null, null);
    }
}