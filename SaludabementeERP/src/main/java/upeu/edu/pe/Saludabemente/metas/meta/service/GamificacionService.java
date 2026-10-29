package upeu.edu.pe.Saludabemente.metas.meta.service;

public interface GamificacionService {

    /** Dias consecutivos con check-in valido, hasta hoy o ayer. */
    int calcularRacha(Long idMeta);

    /** Puntos totales del colaborador: 10 por check-in + 5 por dia de racha. */
    int calcularPuntosPersona(Long idPersona);
}