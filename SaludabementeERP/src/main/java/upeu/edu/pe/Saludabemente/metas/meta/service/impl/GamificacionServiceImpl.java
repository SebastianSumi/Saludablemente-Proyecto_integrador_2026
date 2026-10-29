package upeu.edu.pe.Saludabemente.metas.meta.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import upeu.edu.pe.Saludabemente.metas.meta.entity.MetaEntity;
import upeu.edu.pe.Saludabemente.metas.meta.repository.CheckInRepository;
import upeu.edu.pe.Saludabemente.metas.meta.repository.MetaRepository;
import upeu.edu.pe.Saludabemente.metas.meta.service.GamificacionService;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GamificacionServiceImpl implements GamificacionService {

    private static final int PUNTOS_POR_CHECK_IN = 10;
    private static final int PUNTOS_POR_DIA_RACHA = 5;

    private final CheckInRepository checkInRepository;
    private final MetaRepository metaRepository;

    @Override
    public int calcularRacha(Long idMeta) {
        List<LocalDate> fechas = checkInRepository.findFechasValidasDesc(idMeta);
        if (fechas.isEmpty()) return 0;

        LocalDate hoy = LocalDate.now();
        LocalDate primera = fechas.get(0);

        // La racha sigue viva solo si el ultimo check-in fue hoy o ayer.
        if (primera.isBefore(hoy.minusDays(1))) return 0;

        int racha = 1;
        LocalDate esperada = primera.minusDays(1);
        for (int i = 1; i < fechas.size(); i++) {
            LocalDate f = fechas.get(i);
            if (f.equals(esperada)) {
                racha++;
                esperada = esperada.minusDays(1);
            } else if (f.isBefore(esperada)) {
                break;
            }
        }
        return racha;
    }

    @Override
    public int calcularPuntosPersona(Long idPersona) {
        List<MetaEntity> metas = metaRepository.findByIdPersona(idPersona);
        int puntos = 0;
        for (MetaEntity m : metas) {
            long checkIns = checkInRepository.countByMetaId(m.getId());
            int racha = calcularRacha(m.getId());
            puntos += checkIns * PUNTOS_POR_CHECK_IN + racha * PUNTOS_POR_DIA_RACHA;
        }
        return puntos;
    }
}