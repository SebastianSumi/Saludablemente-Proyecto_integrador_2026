import { pgSchema, bigserial, bigint, varchar, numeric, date, text } from 'drizzle-orm/pg-core';

export const slbMetasSchema = pgSchema('slb_metas');

export const meta = slbMetasSchema.table('meta', {
  idMeta: bigserial('id_meta', { mode: 'number' }).primaryKey(),
  idPersona: bigint('id_persona', { mode: 'number' }).notNull(),
  idPeriodo: bigint('id_periodo', { mode: 'number' }).notNull(),
  tipoMeta: varchar('tipo_meta', { length: 50 }).notNull(),
  valorObjetivo: numeric('valor_objetivo', { precision: 8, scale: 2 }).notNull(),
  unidad: varchar('unidad', { length: 20 }).notNull(),
  fechaInicio: date('fecha_inicio').notNull(),
  fechaLimite: date('fecha_limite').notNull(),
  estado: varchar('estado', { length: 20 }).notNull().default('EN_PROCESO'),
});

export const checkIn = slbMetasSchema.table('check_in', {
  idCheckIn: bigserial('id_check_in', { mode: 'number' }).primaryKey(),
  idMeta: bigint('id_meta', { mode: 'number' }).notNull(),
  fecha: date('fecha').notNull(),
  valorRegistrado: numeric('valor_registrado', { precision: 8, scale: 2 }).notNull(),
  nota: text('nota'),
});
