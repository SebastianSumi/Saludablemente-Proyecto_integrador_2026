package upeu.edu.pe.Saludabemente.asistencias.registro.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import upeu.edu.pe.Saludabemente.actividades.api.ActividadApiService;
import upeu.edu.pe.Saludabemente.actividades.api.dto.ActividadSnapshotDto;
import upeu.edu.pe.Saludabemente.asistencias.enums.EscaneadoPor;
import upeu.edu.pe.Saludabemente.asistencias.enums.EstadoAsistencia;
import upeu.edu.pe.Saludabemente.asistencias.enums.MarcaAsistencia;
import upeu.edu.pe.Saludabemente.asistencias.enums.MetodoRegistro;
import upeu.edu.pe.Saludabemente.asistencias.enums.SyncStatus;
import upeu.edu.pe.Saludabemente.asistencias.enums.TipoAsistencia;
import upeu.edu.pe.Saludabemente.asistencias.qr.service.QrActividadService;
import upeu.edu.pe.Saludabemente.asistencias.registro.dto.request.AsistenciaManualRequest;
import upeu.edu.pe.Saludabemente.asistencias.registro.dto.request.AsistenciaQrCoordinadorRequest;
import upeu.edu.pe.Saludabemente.asistencias.registro.dto.request.MarcacionLoteRequest;
import upeu.edu.pe.Saludabemente.asistencias.registro.dto.request.MarcacionRequest;
import upeu.edu.pe.Saludabemente.asistencias.registro.dto.response.AsistenciaHistorialResponse;
import upeu.edu.pe.Saludabemente.asistencias.registro.dto.response.AsistenciaResponse;
import upeu.edu.pe.Saludabemente.asistencias.registro.dto.response.CierreAsistenciasResponse;
import upeu.edu.pe.Saludabemente.asistencias.registro.dto.response.MarcacionLoteResponse;
import upeu.edu.pe.Saludabemente.asistencias.registro.entity.Asistencia;
import upeu.edu.pe.Saludabemente.asistencias.registro.mapper.AsistenciaMapper;
import upeu.edu.pe.Saludabemente.asistencias.registro.repository.AsistenciaRepository;
import upeu.edu.pe.Saludabemente.asistencias.registro.service.AsistenciaService;
import upeu.edu.pe.Saludabemente.asistencias.util.ClasificacionGeocerca;
import upeu.edu.pe.Saludabemente.asistencias.util.ConstantesAsistencia;
import upeu.edu.pe.Saludabemente.asistencias.util.GeocercaUtil;
import upeu.edu.pe.Saludabemente.exception.BusinessRuleException;
import upeu.edu.pe.Saludabemente.exception.ResourceNotFoundException;
import upeu.edu.pe.Saludabemente.personal.api.CredencialApiService;
import upeu.edu.pe.Saludabemente.personal.api.PersonalApiService;
import upeu.edu.pe.Saludabemente.personal.api.PersonalSnapshotDTO;
import upeu.edu.pe.Saludabemente.personal.api.VerificacionQrResponse;

import java.time.LocalDateTime;
import java.util.HashSet;
import upeu.edu.pe.Saludabemente.asistencias.registro.dto.request.AsistenciaManualDetalleRequest;
import upeu.edu.pe.Saludabemente.asistencias.registro.dto.response.AsistenciaActividadResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AsistenciaServiceImpl implements AsistenciaService {

    private final AsistenciaRepository asistenciaRepository;
    private final AsistenciaMapper asistenciaMapper;
    private final PersonalApiService personalApiService;
    private final CredencialApiService credencialApiService;
    private final ActividadApiService actividadApiService;
    private final QrActividadService qrActividadService;

    @Override
    @Transactional
    public AsistenciaActividadResponse guardarManualDetalle(AsistenciaManualDetalleRequest request) {
        ActividadSnapshotDto actividad = validarActividadEnCurso(request.idActividad());
        Set<Long> personas = new HashSet<>();
        for (var item : request.detalle()) {
            if (!personas.add(item.idPersona())) {
                throw new BusinessRuleException("Persona repetida en el detalle: " + item.idPersona());
            }
        }
        // Una sola transacción: cualquier error revierte todos los cambios de la lista.
        for (var item : request.detalle()) {
            aplicarMarcaConActividad(actividad, request.idActividad(), item.idPersona(),
                    item.marca(), request.tipoAsistencia(), request.latitud(), request.longitud());
        }
        return new AsistenciaActividadResponse(request.idActividad(), actividad.nombre(),
                listarPorActividad(request.idActividad()));
    }

    @Override
    @Transactional(readOnly = true)
    public AsistenciaActividadResponse consultarDetalle(Long idActividad) {
        ActividadSnapshotDto actividad = actividadApiService.obtenerSnapshot(idActividad);
        if (actividad == null) {
            throw new ResourceNotFoundException("Actividad no encontrada: " + idActividad);
        }
        return new AsistenciaActividadResponse(idActividad, actividad.nombre(), listarPorActividad(idActividad));
    }

    // ============================================================
    // REGISTRO MANUAL
    // ============================================================
    @Override
    @Transactional
    public AsistenciaResponse registrarManual(AsistenciaManualRequest request) {

        ActividadSnapshotDto actividad = validarActividad(request.idActividad());

        PersonalSnapshotDTO persona = personalApiService.obtenerSnapshot(request.idPersona());
        if (persona == null) {
            throw new ResourceNotFoundException("Persona no encontrada: " + request.idPersona());
        }
        if (!Boolean.TRUE.equals(persona.activo())) {
            throw new BusinessRuleException("La persona está inactiva");
        }

        validarInscrito(request.idActividad(), request.idPersona());

        validarDuplicado(request.idActividad(), request.idPersona(), request.tipoAsistencia());

        GeocercaUtil.ResultadoGeocerca geo = GeocercaUtil.evaluar(
                request.latitud(), request.longitud(),
                actividad.latitudOficial(), actividad.longitudOficial(),
                actividad.toleranciaGeocercaMetros());

        Asistencia asistencia = Asistencia.builder()
                .idLocal(UUID.randomUUID().toString())
                .idActividad(request.idActividad())
                .idPersona(request.idPersona())
                .tipoAsistencia(request.tipoAsistencia())
                .metodoRegistro(MetodoRegistro.MANUAL)
                .escaneadoPor(EscaneadoPor.COORDINADOR)
                .estado(EstadoAsistencia.ASISTIO)
                .horaMarcado(LocalDateTime.now())
                .latitud(request.latitud())
                .longitud(request.longitud())
                .distanciaGeocercaM(geo.distanciaMetros())
                .flagSospechoso(true) // manual siempre sospechoso
                .syncStatus(SyncStatus.SYNCED)
                .retryCount(0)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Asistencia guardada = asistenciaRepository.save(asistencia);
        log.info("Asistencia manual registrada id={} persona={} actividad={} clasificacion={}",
                guardada.getIdAsistencia(), request.idPersona(), request.idActividad(), geo.clasificacion());

        return asistenciaMapper.toResponse(guardada);
    }

    // ============================================================
    // MARCACIÓN DEL WIZARD (upsert, solo EN_CURSO)
    // ============================================================
    @Override
    @Transactional
    public AsistenciaResponse marcar(MarcacionRequest request) {
        return aplicarMarca(request.idActividad(), request.idPersona(), request.marca(),
                request.tipoAsistencia(), request.latitud(), request.longitud()).respuesta();
    }

    @Override
    @Transactional
    public MarcacionLoteResponse marcarLote(MarcacionLoteRequest request) {
        ActividadSnapshotDto actividad = validarActividadEnCurso(request.idActividad());
        List<Long> ids = request.idsPersona() == null ? List.of()
                : request.idsPersona().stream().filter(java.util.Objects::nonNull).distinct().toList();

        int creados = 0, actualizados = 0;
        List<MarcacionLoteResponse.FalloMarcacion> fallos = new ArrayList<>();
        for (Long idPersona : ids) {
            try {
                // Reutiliza la validación de actividad ya cargada.
                boolean existia = asistenciaRepository
                        .existsByIdActividadAndIdPersonaAndTipoAsistencia(
                                request.idActividad(), idPersona, request.tipoAsistencia());
                aplicarMarcaConActividad(actividad, request.idActividad(), idPersona,
                        MarcaAsistencia.PRESENTE, request.tipoAsistencia(),
                        request.latitud(), request.longitud());
                if (existia) actualizados++;
                else creados++;
            } catch (BusinessRuleException | ResourceNotFoundException e) {
                fallos.add(new MarcacionLoteResponse.FalloMarcacion(idPersona, e.getMessage()));
            } catch (Exception e) {
                log.error("Error marcando lote actividad={} persona={}",
                        request.idActividad(), idPersona, e);
                fallos.add(new MarcacionLoteResponse.FalloMarcacion(idPersona, "ERROR_INTERNO"));
            }
        }
        return new MarcacionLoteResponse(ids.size(), creados, actualizados, fallos);
    }

    private record ResultadoMarca(AsistenciaResponse respuesta, boolean creado) {}

    private ResultadoMarca aplicarMarca(Long idActividad, Long idPersona, MarcaAsistencia marca,
                                        TipoAsistencia tipo, java.math.BigDecimal latitud,
                                        java.math.BigDecimal longitud) {
        ActividadSnapshotDto actividad = validarActividadEnCurso(idActividad);
        return aplicarMarcaConActividad(actividad, idActividad, idPersona, marca, tipo, latitud, longitud);
    }

    private ResultadoMarca aplicarMarcaConActividad(ActividadSnapshotDto actividad, Long idActividad,
                                                   Long idPersona, MarcaAsistencia marca,
                                                   TipoAsistencia tipo, java.math.BigDecimal latitud,
                                                   java.math.BigDecimal longitud) {
        PersonalSnapshotDTO persona = personalApiService.obtenerSnapshot(idPersona);
        if (persona == null) {
            throw new ResourceNotFoundException("Persona no encontrada: " + idPersona);
        }
        if (!Boolean.TRUE.equals(persona.activo())) {
            throw new BusinessRuleException("La persona está inactiva");
        }

        validarInscrito(idActividad, idPersona);

        EstadoAsistencia estado = switch (marca) {
            case PRESENTE -> EstadoAsistencia.ASISTIO;
            case TARDE -> EstadoAsistencia.TARDE;
            case FALTA -> EstadoAsistencia.AUSENTE;
        };

        GeocercaUtil.ResultadoGeocerca geo = GeocercaUtil.evaluar(
                latitud, longitud,
                actividad.latitudOficial(), actividad.longitudOficial(),
                actividad.toleranciaGeocercaMetros());
        boolean sospechoso = geo.clasificacion() != ClasificacionGeocerca.OK;

        Asistencia existente = asistenciaRepository
                .findByIdActividadAndIdPersonaAndTipoAsistencia(idActividad, idPersona, tipo)
                .orElse(null);
        Asistencia guardada;
        boolean creado;
        if (existente == null) {
            guardada = Asistencia.builder()
                    .idLocal(UUID.randomUUID().toString())
                    .idActividad(idActividad)
                    .idPersona(idPersona)
                    .tipoAsistencia(tipo)
                    .metodoRegistro(MetodoRegistro.MANUAL)
                    .escaneadoPor(EscaneadoPor.COORDINADOR)
                    .estado(estado)
                    .horaMarcado(LocalDateTime.now())
                    .latitud(latitud)
                    .longitud(longitud)
                    .distanciaGeocercaM(geo.distanciaMetros())
                    .flagSospechoso(sospechoso)
                    .syncStatus(SyncStatus.SYNCED)
                    .retryCount(0)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            creado = true;
        } else {
            existente.setEstado(estado);
            existente.setHoraMarcado(LocalDateTime.now());
            existente.setLatitud(latitud);
            existente.setLongitud(longitud);
            existente.setDistanciaGeocercaM(geo.distanciaMetros());
            existente.setFlagSospechoso(sospechoso);
            existente.setUpdatedAt(LocalDateTime.now());
            guardada = existente;
            creado = false;
        }
        Asistencia saved = asistenciaRepository.saveAndFlush(guardada);
        log.info("Marcación {} registrada id={} persona={} actividad={}",
                marca, saved.getIdAsistencia(), idPersona, idActividad);
        return new ResultadoMarca(asistenciaMapper.toResponse(saved), creado);
    }
    // ============================================================
    // REGISTRO POR QR COORDINADOR (FASE 5)
    // ============================================================
    @Override
    @Transactional
    public AsistenciaResponse registrarPorQrCoordinador(AsistenciaQrCoordinadorRequest request) {

        VerificacionQrResponse verificacion =
                credencialApiService.verificarQrParaAsistencia(request.codigoQrPersonal());

        if (verificacion == null || !verificacion.valido()) {
            throw new BusinessRuleException(
                    verificacion != null ? verificacion.mensaje() : "QR inválido");
        }
        if (verificacion.idPersona() == null) {
            throw new BusinessRuleException("QR válido pero sin persona asociada");
        }

        Long idPersona = verificacion.idPersona();

        ActividadSnapshotDto actividad = validarActividad(request.idActividad());

        // QR de actividad obligatorio: solo vale el vigente (o en gracia).
        qrActividadService.validarCodigoQr(request.codigoQrActividad(), LocalDateTime.now());

        PersonalSnapshotDTO persona = personalApiService.obtenerSnapshot(idPersona);
        if (persona == null) {
            throw new ResourceNotFoundException("Persona no encontrada: " + idPersona);
        }
        if (!Boolean.TRUE.equals(persona.activo())) {
            throw new BusinessRuleException("La persona está inactiva");
        }

        validarInscrito(request.idActividad(), idPersona);

        validarDuplicado(request.idActividad(), idPersona, request.tipoAsistencia());

        GeocercaUtil.ResultadoGeocerca geo = GeocercaUtil.evaluar(
                request.latitud(), request.longitud(),
                actividad.latitudOficial(), actividad.longitudOficial(),
                actividad.toleranciaGeocercaMetros());

        boolean sospechoso = geo.clasificacion() != ClasificacionGeocerca.OK;

        String idLocal = (request.idLocal() != null && !request.idLocal().isBlank())
                ? request.idLocal()
                : UUID.randomUUID().toString();

        Asistencia asistencia = Asistencia.builder()
                .idLocal(idLocal)
                .idActividad(request.idActividad())
                .idPersona(idPersona)
                .tipoAsistencia(request.tipoAsistencia())
                .metodoRegistro(MetodoRegistro.QR_COORDINADOR)
                .escaneadoPor(EscaneadoPor.COORDINADOR)
                .estado(EstadoAsistencia.ASISTIO)
                .horaMarcado(LocalDateTime.now())
                .latitud(request.latitud())
                .longitud(request.longitud())
                .distanciaGeocercaM(geo.distanciaMetros())
                .flagSospechoso(sospechoso)
                .dispositivoId(request.dispositivoId())
                .syncStatus(SyncStatus.SYNCED)
                .retryCount(0)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Asistencia guardada = asistenciaRepository.save(asistencia);
        log.info("Asistencia QR coordinador registrada id={} persona={} actividad={} clasificacion={}",
                guardada.getIdAsistencia(), idPersona, request.idActividad(), geo.clasificacion());

        return asistenciaMapper.toResponse(guardada);
    }

    // ============================================================
    // CONSULTAS
    // ============================================================
    @Override
    @Transactional(readOnly = true)
    public List<AsistenciaResponse> listarPorActividad(Long idActividad) {
        if (idActividad == null) {
            throw new IllegalArgumentException("idActividad no puede ser nulo");
        }
        return asistenciaMapper.toResponseList(
                asistenciaRepository.findByIdActividad(idActividad));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AsistenciaHistorialResponse> historialPorPersona(Long idPersona) {
        if (idPersona == null) {
            throw new IllegalArgumentException("idPersona no puede ser nulo");
        }

        PersonalSnapshotDTO persona = personalApiService.obtenerSnapshot(idPersona);
        if (persona == null) {
            throw new ResourceNotFoundException("Persona no encontrada: " + idPersona);
        }

        List<Asistencia> asistencias = asistenciaRepository.findByIdPersona(idPersona);

        return asistencias.stream()
                .map(a -> {
                    ActividadSnapshotDto act = actividadApiService.obtenerSnapshot(a.getIdActividad());
                    String nombre = act != null ? act.nombre() : "Actividad desconocida";
                    LocalDateTime fecha = (act != null && act.fechaActividad() != null)
                            ? act.fechaActividad().atStartOfDay()
                            : null;
                    return asistenciaMapper.toHistorialResponse(a, nombre, fecha);
                })
                .toList();
    }

    // ============================================================
    // HELPERS PRIVADOS
    // ============================================================
    /**
     * Solo EN_CURSO (wizard de marcación). No toca validarActividad de los flujos antiguos.
     */
    private ActividadSnapshotDto validarActividadEnCurso(Long idActividad) {
        ActividadSnapshotDto actividad = actividadApiService.obtenerSnapshot(idActividad);
        if (actividad == null) {
            throw new ResourceNotFoundException("Actividad no encontrada: " + idActividad);
        }
        if (!ConstantesAsistencia.ACTIVIDAD_EN_CURSO.equals(actividad.estado())) {
            throw new BusinessRuleException(
                    "Solo se puede marcar asistencia en una actividad EN_CURSO. Estado actual: "
                            + actividad.estado());
        }
        return actividad;
    }

    private ActividadSnapshotDto validarActividad(Long idActividad) {
        ActividadSnapshotDto actividad = actividadApiService.obtenerSnapshot(idActividad);
        if (actividad == null) {
            throw new ResourceNotFoundException("Actividad no encontrada: " + idActividad);
        }
        if (ConstantesAsistencia.ACTIVIDAD_CANCELADA.equals(actividad.estado())) {
            throw new BusinessRuleException("No se puede registrar asistencia en una actividad cancelada");
        }
        if (ConstantesAsistencia.ACTIVIDAD_FINALIZADA.equals(actividad.estado())) {
            throw new BusinessRuleException("No se puede registrar asistencia en una actividad finalizada");
        }
        return actividad;
    }

    /**
     * Valida que la persona este explicitamente inscrita en la actividad.
     * Sin inscripcion no se permite registrar ni marcar asistencia.
     */
    private void validarInscrito(Long idActividad, Long idPersona) {
        if (!actividadApiService.estaInscrito(idActividad, idPersona)) {
            throw new BusinessRuleException(
                    "La persona no está inscrita en esta actividad");
        }
    }

    /**
     * Verifica duplicado por clave de negocio (actividad + persona + tipo).
     * Recibe TipoAsistencia (enum) para que Hibernate pueda bindearlo correctamente.
     */
    private void validarDuplicado(Long idActividad, Long idPersona, TipoAsistencia tipoAsistencia) {
        boolean existe = asistenciaRepository
                .existsByIdActividadAndIdPersonaAndTipoAsistencia(
                        idActividad, idPersona, tipoAsistencia);
        if (existe) {
            throw new BusinessRuleException(
                    "Ya existe una asistencia de este tipo para la persona en esta actividad");
        }
    }

    // ============================================================
    // CIERRE DE ASISTENCIAS (FASE 9)
    // ============================================================
    @Override
    @Transactional
    public CierreAsistenciasResponse generarCierreDeAsistencias(Long idActividad) {

        log.info("Iniciando cierre de asistencias para actividad {}", idActividad);

        // 1. Validar actividad
        ActividadSnapshotDto actividad = actividadApiService.obtenerSnapshot(idActividad);
        if (actividad == null) {
            throw new ResourceNotFoundException("Actividad no encontrada: " + idActividad);
        }
        if (!ConstantesAsistencia.ACTIVIDAD_FINALIZADA.equals(actividad.estado())) {
            throw new BusinessRuleException(
                    "Solo se puede cerrar una actividad FINALIZADA. Estado actual: " + actividad.estado());
        }

        // 2. Determinar esperados: SOLO inscritos explicitos.
        List<Long> idsEsperados = actividadApiService.listarInscritos(idActividad);
        if (idsEsperados == null || idsEsperados.isEmpty()) {
            throw new BusinessRuleException(
                    "No se puede cerrar: la actividad no tiene inscritos explícitos");
        }

        // 3. Asistencias existentes
        List<Asistencia> existentes = asistenciaRepository
                .findByIdActividad(idActividad);

        Set<Long> idsConAsistencia = existentes.stream()
                .map(Asistencia::getIdPersona)
                .collect(Collectors.toSet());

        // 4. Insertar AUSENTE para faltantes
        int insertados = 0;
        for (Long idPersona : idsEsperados) {
            if (idsConAsistencia.contains(idPersona)) {
                continue;
            }
            Asistencia ausente = Asistencia.builder()
                    .idLocal(UUID.randomUUID().toString())
                    .idActividad(idActividad)
                    .idPersona(idPersona)
                    .tipoAsistencia(TipoAsistencia.SIMPLE)
                    .metodoRegistro(MetodoRegistro.SISTEMA_CIERRE)
                    .estado(EstadoAsistencia.AUSENTE)
                    .horaMarcado(LocalDateTime.now())
                    .flagSospechoso(false)
                    .syncStatus(SyncStatus.SYNCED)
                    .retryCount(0)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            asistenciaRepository.save(ausente);
            insertados++;
        }

        // 5. Calcular tiempo efectivo si aplica
        if (Boolean.TRUE.equals(actividad.requiereCheckInOut())) {
            for (Long idPersona : idsConAsistencia) {
                try {
                    calcularTiempoEfectivo(idActividad, idPersona);
                } catch (Exception e) {
                    log.error("Error calculando tiempo efectivo actividad={} persona={}",
                            idActividad, idPersona, e);
                }
            }
        }

        // 6. Contar totales (TARDE cuenta como asistente)
        List<Asistencia> todas = asistenciaRepository.findByIdActividad(idActividad);
        int asistentes = (int) todas.stream()
                .filter(a -> a.getEstado() == EstadoAsistencia.ASISTIO
                        || a.getEstado() == EstadoAsistencia.TARDE)
                .count();
        int ausentes = (int) todas.stream()
                .filter(a -> a.getEstado() == EstadoAsistencia.AUSENTE)
                .count();
        int exonerados = (int) todas.stream()
                .filter(a -> a.getEstado() == EstadoAsistencia.EXONERADO)
                .count();
        int incompletas = (int) todas.stream()
                .filter(a -> a.getEstado() == EstadoAsistencia.INCOMPLETA)
                .count();

        // 7. Marcar cierre realizado en Actividades
        actividadApiService.marcarCierreRealizado(idActividad);

        log.info("Cierre completado actividad={} esperados={} asistentes={} ausentes={} incompletas={}",
                idActividad, idsEsperados.size(), asistentes, ausentes, incompletas);

        return new CierreAsistenciasResponse(
                idActividad,
                idsEsperados.size(),
                asistentes,
                ausentes,
                exonerados,
                incompletas,
                true
        );
    }

    // ============================================================
    // TIEMPO EFECTIVO
    // ============================================================
    @Override
    @Transactional
    public void calcularTiempoEfectivo(Long idActividad, Long idPersona) {

        // 1. Traer actividad
        ActividadSnapshotDto actividad = actividadApiService.obtenerSnapshot(idActividad);
        if (actividad == null || !Boolean.TRUE.equals(actividad.requiereCheckInOut())) {
            return;
        }

        // 2. Traer los CHECK_IN y CHECK_OUT de la persona
        List<Asistencia> marcas = asistenciaRepository
                .findByIdActividadAndIdPersonaIn(idActividad, List.of(idPersona));

        LocalDateTime checkIn = marcas.stream()
                .filter(a -> a.getTipoAsistencia() == TipoAsistencia.CHECK_IN)
                .map(Asistencia::getHoraMarcado)
                .findFirst()
                .orElse(null);

        LocalDateTime checkOut = marcas.stream()
                .filter(a -> a.getTipoAsistencia() == TipoAsistencia.CHECK_OUT)
                .map(Asistencia::getHoraMarcado)
                .findFirst()
                .orElse(null);

        // 3. Si falta check-in o check-out → INCOMPLETA
        if (checkIn == null || checkOut == null) {
            marcarIncompleta(idActividad, idPersona);
            return;
        }

        // 4. Permanencia en minutos
        long permanenciaMin = java.time.Duration.between(checkIn, checkOut).toMinutes();

        // 5. Duración esperada
        Integer duracion = actividad.duracionMinutos();
        if (duracion == null || duracion <= 0) {
            log.warn("Actividad {} sin duración válida. No se calcula tiempo efectivo", idActividad);
            return;
        }

        // 6. Umbral 80%
        double umbral = duracion * ConstantesAsistencia.PORCENTAJE_TIEMPO_EFECTIVO_MINIMO;

        if (permanenciaMin < umbral) {
            marcarIncompleta(idActividad, idPersona);
            log.info("Tiempo efectivo insuficiente persona={} permanencia={}min umbral={}min",
                    idPersona, permanenciaMin, umbral);
        } else {
            log.info("Tiempo efectivo OK persona={} permanencia={}min", idPersona, permanenciaMin);
        }

        // 7. Actualizar horaCheckIn / horaCheckOut en la marca principal
        marcas.stream()
                .filter(a -> a.getEstado() != EstadoAsistencia.AUSENTE)
                .forEach(a -> {
                    a.setHoraCheckIn(checkIn);
                    a.setHoraCheckOut(checkOut);
                    a.setUpdatedAt(LocalDateTime.now());
                    asistenciaRepository.save(a);
                });
    }

    // ============================================================
    // HELPERS
    // ============================================================
    private void marcarIncompleta(Long idActividad, Long idPersona) {
        asistenciaRepository.findByIdActividadAndIdPersonaIn(idActividad, List.of(idPersona))
                .stream()
                .filter(a -> a.getEstado() == EstadoAsistencia.ASISTIO)
                .forEach(a -> {
                    a.setEstado(EstadoAsistencia.INCOMPLETA);
                    a.setUpdatedAt(LocalDateTime.now());
                    asistenciaRepository.save(a);
                });
    }
}
