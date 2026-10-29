export type EstadoJornada = 'PROGRAMADA' | 'ABIERTA' | 'CERRADA' | 'CANCELADA';

export type EstadoInscripcion = 'ACTIVA' | 'CANCELADA' | 'ASISTIO';

export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}

export interface JornadaEvaluacionResponse {
  idJornada: number;
  nombre: string;
  descripcion: string | null;
  fecha: string;
  hora: string;
  capacidad: number;
  idPeriodo: number;
  periodoCodigo: string | null;
  estado: EstadoJornada;
  inscritos: number;
  cuposDisponibles: number;
  fechaCreacion: string | null;
  usuarioCreacion: string | null;
}

export interface JornadaEvaluacionRequest {
  nombre: string;
  descripcion: string | null;
  fecha: string;
  hora: string;
  capacidad: number;
  idPeriodo: number;
  estado?: EstadoJornada;
}

export interface InscripcionJornadaResponse {
  idInscripcion: number;
  idJornada: number;
  nombreJornada: string | null;
  fechaJornada: string | null;
  horaJornada: string | null;
  idPersona: number;
  documentoIdentidad: string | null;
  nombresCompletos: string | null;
  numeroTurno: number;
  fechaInscripcion: string;
  estado: EstadoInscripcion;
}
