package upeu.edu.pe.Saludabemente.metas.api;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import upeu.edu.pe.Saludabemente.exception.ResourceNotFoundException;
import upeu.edu.pe.Saludabemente.metas.api.dto.MetaPublicaDTO;
import upeu.edu.pe.Saludabemente.metas.api.dto.ResumenProgresoPublicDTO;
import upeu.edu.pe.Saludabemente.metas.meta.entity.MetaEntity;
import upeu.edu.pe.Saludabemente.metas.meta.repository.CheckInRepository;
import upeu.edu.pe.Saludabemente.metas.meta.repository.MetaRepository;
import upeu.edu.pe.Saludabemente.metas.meta.service.GamificacionService;
import upeu.edu.pe.Saludabemente.metas.meta.service.impl.MetaServiceImpl;
import upeu.edu.pe.Saludabemente.metas.shared.enums.EstadoMeta;
import upeu.edu.pe.Saludabemente.metas.shared.enums.TipoOrigen;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
class MetaQueryApiImpl implements MetaQueryApi {

    private final MetaRepository metaRepository;
    private final CheckInRepository checkInRepository;
    private final GamificacionService gamificacionService;

    @Override
    public ResumenProgresoPublicDTO resumenPorPersona(Long idPersona) {

        long activas     = metaRepository.countByIdPersonaAndEstado(idPersona, EstadoMeta.ACTIVA);
        long completas   = metaRepository.countByIdPersonaAndEstado(idPersona, EstadoMeta.COMPLETADA);
        long desviadas   = metaRepository.countByIdPersonaAndEstado(idPersona, EstadoMeta.DESVIADA);
        long abandonadas = metaRepository.countByIdPersonaAndEstado(idPersona, EstadoMeta.ABANDONADA);

        long total = activas + completas + desviadas + abandonadas;
        double cumplimiento = (total == 0)
                ? 0.0
                : Math.round((completas * 100.0 / total) * 100.0) / 100.0;

        int puntos = gamificacionService.calcularPuntosPersona(idPersona);

        return new ResumenProgresoPublicDTO(
                idPersona,
                activas,
                completas,
                desviadas,
                abandonadas,
                cumplimiento,
                puntos);
    }

    @Override
    public boolean tieneMetaClinicaDesviada(Long idPersona) {
        return metaRepository.existsByIdPersonaAndTipoOrigenAndEstado(
                idPersona, TipoOrigen.CLINICA, EstadoMeta.DESVIADA);
    }

    @Override
    public MetaPublicaDTO obtenerMetaPublica(Long idMeta) {

        MetaEntity m = metaRepository.findById(idMeta)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Meta no encontrada: " + idMeta));

        BigDecimal avance = MetaServiceImpl.calcularAvance(m);
        var ultimoCheckIn = checkInRepository.findUltimaFecha(idMeta);

        return new MetaPublicaDTO(
                m.getId(),
                m.getIdPersona(),
                m.getDescripcion(),
                m.getPilar().name(),
                m.getTipoOrigen().name(),
                m.getEstado().name(),
                m.getValorObjetivo(),
                m.getValorActual(),
                avance,
                m.getFechaInicio(),
                m.getFechaObjetivo(),
                ultimoCheckIn);
    }
}