import { pgSchema, bigserial, varchar, text, bigint, boolean, timestamp, integer } from 'drizzle-orm/pg-core';

export const slbNotificacionSchema = pgSchema('slb_notificacion');

export const plantillaMensaje = slbNotificacionSchema.table('plantilla_mensaje', {
  idPlantilla: bigserial('id_plantilla', { mode: 'number' }).primaryKey(),
  codigo: varchar('codigo', { length: 50 }).notNull().unique(),
  asunto: varchar('asunto', { length: 200 }).notNull(),
  cuerpoHtml: text('cuerpo_html'),   // @Lob migrado a text
  cuerpoTexto: text('cuerpo_texto'), // @Lob migrado a text
  canal: varchar('canal', { length: 20 }).notNull(),
});

export const notificacion = slbNotificacionSchema.table('notificacion', {
  idNotificacion: bigserial('id_notificacion', { mode: 'number' }).primaryKey(),
  idUsuario: bigint('id_usuario', { mode: 'number' }).notNull(),
  titulo: varchar('titulo', { length: 150 }).notNull(),
  mensaje: text('mensaje').notNull(), // @Lob migrado a text
  canal: varchar('canal', { length: 20 }).notNull(),
  leida: boolean('leida').notNull().default(false),
  fechaCreacion: timestamp('fecha_creacion').notNull().defaultNow(),
  fechaLeida: timestamp('fecha_leida'),
});

export const notificacionDlq = slbNotificacionSchema.table('notificacion_dlq', {
  idDlq: bigserial('id_dlq', { mode: 'number' }).primaryKey(),
  payload: text('payload').notNull(),           // @Lob migrado a text
  mensajeError: text('mensaje_error'),         // @Lob migrado a text
  reintentos: integer('reintentos').notNull().default(0),
  fechaFallo: timestamp('fecha_fallo').notNull().defaultNow(),
});
