import { pgSchema, bigserial, varchar, boolean, date } from 'drizzle-orm/pg-core';

export const slbCatalogoSchema = pgSchema('slb_catalogo');

export const catAreaTrabajo = slbCatalogoSchema.table('cat_area_trabajo', {
  idArea: bigserial('id_area', { mode: 'number' }).primaryKey(),
  nombre: varchar('nombre', { length: 100 }).notNull(),
  descripcion: varchar('descripcion', { length: 255 }),
  activo: boolean('activo').notNull().default(true),
});

export const catPeriodo = slbCatalogoSchema.table('cat_periodo', {
  idPeriodo: bigserial('id_periodo', { mode: 'number' }).primaryKey(),
  codigo: varchar('codigo', { length: 20 }).notNull().unique(),
  descripcion: varchar('descripcion', { length: 150 }).notNull(),
  fechaInicio: date('fecha_inicio').notNull(),
  fechaFin: date('fecha_fin').notNull(),
  estadoPeriodo: varchar('estado_periodo', { length: 20 }).notNull().default('PLANIFICADO'),
});
