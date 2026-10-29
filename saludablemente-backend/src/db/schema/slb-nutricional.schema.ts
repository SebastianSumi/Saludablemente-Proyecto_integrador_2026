import { pgSchema, bigserial, bigint, varchar, date, integer, timestamp, smallint, text, boolean, numeric } from 'drizzle-orm/pg-core';

export const slbNutricionalSchema = pgSchema('slb_nutricional');

export const jornadaEvaluacion = slbNutricionalSchema.table('jornada_evaluacion', {
  idJornada: bigserial('id_jornada', { mode: 'number' }).primaryKey(),
  idPeriodo: bigint('id_periodo', { mode: 'number' }).notNull(),
  nombre: varchar('nombre', { length: 150 }).notNull(),
  fecha: date('fecha').notNull(),
  cuposTotales: integer('cupos_totales').notNull(),
  estado: varchar('estado', { length: 20 }).notNull().default('ABIERTA'),
});

export const inscripcionJornada = slbNutricionalSchema.table('inscripcion_jornada', {
  idInscripcion: bigserial('id_inscripcion', { mode: 'number' }).primaryKey(),
  idJornada: bigint('id_jornada', { mode: 'number' }).notNull(),
  idPersona: bigint('id_persona', { mode: 'number' }).notNull(),
  numeroTurno: integer('numero_turno').notNull(),
  estado: varchar('estado', { length: 20 }).notNull().default('PENDIENTE'),
  fechaInscripcion: timestamp('fecha_inscripcion').notNull().defaultNow(),
});

export const evaluacionNutricional = slbNutricionalSchema.table('evaluacion_nutricional', {
  idEvaluacion: bigserial('id_evaluacion', { mode: 'number' }).primaryKey(),
  idPersona: bigint('id_persona', { mode: 'number' }).notNull(),
  idPeriodo: bigint('id_periodo', { mode: 'number' }).notNull(),
  fechaEvaluacion: date('fecha_evaluacion').notNull(),
  edadAlMomento: smallint('edad_al_momento').notNull(),
  observaciones: text('observaciones'),
  activo: boolean('activo').notNull().default(true),
});

export const detalleAntropometrico = slbNutricionalSchema.table('detalle_antropometrico', {
  idAntropometria: bigserial('id_antropometria', { mode: 'number' }).primaryKey(),
  idEvaluacion: bigint('id_evaluacion', { mode: 'number' }).notNull(),
  pesoKg: numeric('peso_kg', { precision: 5, scale: 2 }).notNull(),
  tallaCm: numeric('talla_cm', { precision: 5, scale: 2 }).notNull(),
  imc: numeric('imc', { precision: 5, scale: 2 }).notNull(),
  perimetroAbdominalCm: numeric('perimetro_abdominal_cm', { precision: 5, scale: 2 }),
  porcentajeGrasa: numeric('porcentaje_grasa', { precision: 5, scale: 2 }),
  masaMuscularKg: numeric('masa_muscular_kg', { precision: 5, scale: 2 }),
  grasaVisceral: smallint('grasa_visceral'),
});

export const detalleBioquimico = slbNutricionalSchema.table('detalle_bioquimico', {
  idBioquimico: bigserial('id_bioquimico', { mode: 'number' }).primaryKey(),
  idEvaluacion: bigint('id_evaluacion', { mode: 'number' }).notNull(),
  glucosaMgDl: numeric('glucosa_mg_dl', { precision: 6, scale: 2 }),
  colesterolTotalMgDl: numeric('colesterol_total_mg_dl', { precision: 6, scale: 2 }),
  trigliceridosMgDl: numeric('trigliceridos_mg_dl', { precision: 6, scale: 2 }),
  presionSistolica: smallint('presion_sistolica'),
  presionDiastolica: smallint('presion_diastolica'),
});
