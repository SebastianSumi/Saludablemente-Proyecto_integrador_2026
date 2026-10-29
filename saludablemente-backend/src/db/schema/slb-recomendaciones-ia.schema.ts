import { pgSchema, bigserial, bigint, text, varchar, timestamp, integer } from 'drizzle-orm/pg-core';

export const slbRecomendacionesIaSchema = pgSchema('slb_recomendaciones_ia');

export const recomendacionIa = slbRecomendacionesIaSchema.table('recomendacion_ia', {
  idRecomendacion: bigserial('id_recomendacion', { mode: 'number' }).primaryKey(),
  idPersona: bigint('id_persona', { mode: 'number' }).notNull(),
  contenido: text('contenido').notNull(),                        // @Lob migrado a text
  justificacionClinica: text('justificacion_clinica'),          // @Lob migrado a text
  tipo: varchar('tipo', { length: 30 }).notNull(),
  fechaGeneracion: timestamp('fecha_generacion').notNull().defaultNow(),
});

export const recomendacionPendiente = slbRecomendacionesIaSchema.table('recomendacion_pendiente', {
  idPendiente: bigserial('id_pendiente', { mode: 'number' }).primaryKey(),
  idPersona: bigint('id_persona', { mode: 'number' }).notNull(),
  prompt: text('prompt').notNull(),                              // @Lob migrado a text
  respuestaRaw: text('respuesta_raw'),                          // @Lob migrado a text
  datosEntradaJson: text('datos_entrada_json'),                  // @Lob migrado a text
  errorMensaje: text('error_mensaje'),                          // @Lob migrado a text
  estado: varchar('estado', { length: 20 }).notNull().default('PENDIENTE'),
  intentos: integer('intentos').notNull().default(0),
  fechaCreacion: timestamp('fecha_creacion').notNull().defaultNow(),
});
