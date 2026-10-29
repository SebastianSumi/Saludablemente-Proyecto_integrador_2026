import { AreaResumen } from '../../catalogo/area/area.model';

/**
 * Modelos que reflejan el contrato del módulo Personal del backend.
 * Coinciden 1 a 1 con los DTOs y enums expuestos por PersonaController.
 */

export type Sexo = 'M' | 'F';
export type EstadoCredencial = 'ACTIVA' | 'ANONIMIZADA';

/**
 * Vista reducida de un equipo. Coincide con EquipoResumen del backend.
 */
export interface EquipoResumen {
  id: number;
  nombre: string;
}

/**
 * Respuesta completa de una persona. Coincide con PersonaResponse.
 */
export interface Persona {
  id: number;
  nombres: string;
  apellidoPaterno: string;
  apellidoMaterno?: string;
  documentoIdentidad: string;
  celular: string;
  fechaNacimiento: string;
  edad: number;
  sexo: Sexo;
  areaTrabajo?: AreaResumen;
  tallaPolo?: string;
  estadoCivil?: string;
  nivelEducativo?: string;
  equipo?: EquipoResumen;
  activo: boolean;
  avatarUrl?: string;
}

/**
 * Vista reducida de una persona. Coincide con PersonaResumen.
 */
export interface PersonaResumen {
  id: number;
  nombres: string;
  apellidoPaterno: string;
  apellidoMaterno?: string;
  nombreCompleto: string;
}

/**
 * Reporte agregado del módulo Personal. Coincide con PersonalAgregado.
 */
export interface PersonalAgregado {
  totalPersonal: number;
  totalActivos: number;
  totalHombres: number;
  totalMujeres: number;
}

/**
 * Payload de entrada para crear/actualizar personas. Coincide con PersonaRequest.
 * El areaTrabajo ahora se envía como areaId (FK hacia Catálogo).
 */
export interface PersonaRequest {
  nombres: string;
  apellidoPaterno: string;
  apellidoMaterno?: string;
  documentoIdentidad: string;
  celular: string;
  fechaNacimiento: string;
  sexo: Sexo;
  areaId?: number;
  tallaPolo?: string;
  estadoCivil?: string;
  nivelEducativo?: string;
  equipoId?: number;
}

/**
 * Payload del PATCH parcial de celular. Coincide con CelularUpdateRequest.
 */
export interface CelularUpdateRequest {
  celular: string;
}

/**
 * Payload para configurar preferencias de comunicación. Coincide con PreferenciaRequest.
 */
export interface PreferenciaRequest {
  canalPreferido: string;
  horarioContactoInicio?: string;
  horarioContactoFin?: string;
  aceptaRecordatorios?: boolean;
}

/**
 * Respuesta de las preferencias configuradas. Coincide con PreferenciaResponse.
 */
export interface PreferenciaResponse {
  id: number;
  personaId: number;
  canalPreferido: string;
  horarioContactoInicio?: string;
  horarioContactoFin?: string;
  aceptaRecordatorios: boolean;
}

/**
 * Respuesta de una credencial QR. Coincide con CredencialResponse.
 */
export interface CredencialResponse {
  id: number;
  personaId: number;
  codigoQrHash: string;
  fechaEmision: string;
  estado: EstadoCredencial;
  qrBase64?: string;
}

/**
 * Filtros opcionales para el listado de personas.
 * areaId reemplaza al anterior areaTrabajo (String).
 */
export interface PersonaFiltros {
  estado?: boolean;
  sexo?: Sexo;
  areaId?: number;
  equipoId?: number;
  texto?: string;
  ordenarPor?: string;
  direccion?: 'ASC' | 'DESC';
}
