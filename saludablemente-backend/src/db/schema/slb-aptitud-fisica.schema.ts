import { pgSchema, bigserial, bigint, date, smallint, text, boolean, integer, numeric } from 'drizzle-orm/pg-core';

export const slbAptitudFisicaSchema = pgSchema('slb_aptitud_fisica');

export const evaluacionAptitud = slbAptitudFisicaSchema.table('evaluacion', {
  idEvaluacion: bigserial('id_evaluacion', { mode: 'number' }).primaryKey(),
  idPersona: bigint('id_persona', { mode: 'number' }).notNull(),
  idPeriodo: bigint('id_periodo', { mode: 'number' }).notNull(),
  fechaEvaluacion: date('fecha_evaluacion').notNull(),
  edadAlMomento: smallint('edad_al_momento').notNull(), // columnDefinition = NUMBER(3)
  observaciones: text('observaciones'),
  activo: boolean('activo').notNull().default(true),
});

export const detalleCardiovascular = slbAptitudFisicaSchema.table('cardio', {
  idCardio: bigserial('id_cardio', { mode: 'number' }).primaryKey(),
  idEvaluacion: bigint('id_evaluacion', { mode: 'number' }).notNull(),
  frecuenciaCardiacaReposo: smallint('frecuencia_cardiaca_reposo'), // NUMBER(3)
  frecuenciaCardiacaPost: smallint('frecuencia_cardiaca_post'),     // NUMBER(3)
  tiempoRecuperacionSegundos: integer('tiempo_recuperacion_segundos'), // NUMBER(4)
  vo2Max: numeric('vo2_max', { precision: 6, scale: 2 }),
});

export const detalleFuerzaResistencia = slbAptitudFisicaSchema.table('fuerza', {
  idFuerza: bigserial('id_fuerza', { mode: 'number' }).primaryKey(),
  idEvaluacion: bigint('id_evaluacion', { mode: 'number' }).notNull(),
  repeticionesPlanchas: smallint('repeticiones_planchas'),       // NUMBER(3)
  repeticionesAbdominales: smallint('repeticiones_abdominales'), // NUMBER(3)
  distanciaSaltoCm: numeric('distancia_salto_cm', { precision: 6, scale: 2 }),
});
