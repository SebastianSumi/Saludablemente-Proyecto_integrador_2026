/**
 * Frontera publica del modulo metas.
 *
 * Expone la unica via por la que otros modulos (perfil, exportacion,
 * recomendaciones-ia, alertaclinica) pueden consultar el estado de las
 * metas de un colaborador. Nunca se exponen MetaEntity ni CheckInEntity
 * ni sus repositorios.
 */
@org.springframework.modulith.NamedInterface("metas-api")
package upeu.edu.pe.Saludabemente.metas.api;