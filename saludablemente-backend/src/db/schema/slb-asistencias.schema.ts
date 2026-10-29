import { pgSchema, bigserial, varchar, text, timestamp, bigint, integer } from 'drizzle-orm/pg-core';

export const slbAsistenciasSchema = pgSchema('slb_asistencias');

export const asistencia = slbAsistenciasSchema.table('asistencia', {
  idAsistencia: bigserial('id_asistencia', { mode: 'number' }).primaryKey(),
  idActividad: bigint('id_actividad', { mode: 'number' }).notNull(),
  idPersona: bigint('id_persona', { mode: 'number' }).notNull(),
  tipoAsistencia: varchar('tipo_asistencia', { length: 20 }).notNull(),
  fechaHora: timestamp('fecha_hora').notNull().defaultNow(),
  idLocal: varchar('id_local', { length: 50 }).unique(),
  observacion: varchar('observacion', { length: 255 }),
});

export const asistenciaCuarentena = slbAsistenciasSchema.table('asistencia_cuarentena', {
  idCuarentena: bigserial('id_cuarentena', { mode: 'number' }).primaryKey(),
  idLocal: varchar('id_local', { length: 50 }).unique(),
  payload: text('payload').notNull(),
  motivo: text('motivo'), // @Lob migrado a text
  fechaCaptura: timestamp('fecha_captura').notNull().defaultNow(),
});

export const justificacion = slbAsistenciasSchema.table('justificacion', {
  idJustificacion: bigserial('id_justificacion', { mode: 'number' }).primaryKey(),
  idActividad: bigint('id_actividad', { mode: 'number' }).notNull(),
  idPersona: bigint('id_persona', { mode: 'number' }).notNull(),
  motivo: varchar('motivo', { length: 500 }).notNull(),
  archivoAdjunto: varchar('archivo_adjunto', { length: 500 }),
  estado: varchar('estado', { length: 20 }).notNull().default('PENDIENTE'),
  fechaSolicitud: timestamp('fecha_solicitud').notNull().defaultNow(),
  fechaResolucion: timestamp('fecha_resolucion'),
});

export const qrActividad = slbAsistenciasSchema.table('qr_actividad', {
  idQr: bigserial('id_qr', { mode: 'number' }).primaryKey(),
  idActividad: bigint('id_actividad', { mode: 'number' }).notNull(),
  codigoQr: varchar('codigo_qr', { length: 100 }).notNull().unique(),
  version: integer('version').notNull(),
  fechaGeneracion: timestamp('fecha_generacion').notNull(),
  fechaExpiracion: timestamp('fecha_expiracion').notNull(),
});
