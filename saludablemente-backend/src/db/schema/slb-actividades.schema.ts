import { pgSchema, bigserial, varchar, text, date, integer, bigint, boolean, timestamp } from 'drizzle-orm/pg-core';

export const slbActividadesSchema = pgSchema('slb_actividades');

export const actividad = slbActividadesSchema.table('actividad', {
  idActividad: bigserial('id_actividad', { mode: 'number' }).primaryKey(),
  titulo: varchar('titulo', { length: 150 }).notNull(),
  descripcion: text('descripcion'),
  fecha: date('fecha').notNull(),
  horaInicio: varchar('hora_inicio', { length: 8 }),
  horaFin: varchar('hora_fin', { length: 8 }),
  lugar: varchar('lugar', { length: 150 }),
  cupos: integer('cupos'),
  estado: varchar('estado', { length: 20 }).notNull().default('PROGRAMADA'),
  idCoordinador: bigint('id_coordinador', { mode: 'number' }),
  activo: boolean('activo').notNull().default(true),
});

export const actividadMaterial = slbActividadesSchema.table('actividad_material', {
  idMaterial: bigserial('id_material', { mode: 'number' }).primaryKey(),
  idActividad: bigint('id_actividad', { mode: 'number' }).notNull(),
  nombre: varchar('nombre', { length: 100 }).notNull(),
  cantidad: integer('cantidad').notNull(),
});

export const actividadImagen = slbActividadesSchema.table('actividad_imagen', {
  idImagen: bigserial('id_imagen', { mode: 'number' }).primaryKey(),
  idActividad: bigint('id_actividad', { mode: 'number' }).notNull(),
  imagenUrl: varchar('imagen_url', { length: 500 }).notNull(),
  orden: integer('orden'),
});

export const actividadInscrito = slbActividadesSchema.table('actividad_inscrito', {
  idInscrito: bigserial('id_inscrito', { mode: 'number' }).primaryKey(),
  idActividad: bigint('id_actividad', { mode: 'number' }).notNull(),
  idPersona: bigint('id_persona', { mode: 'number' }).notNull(),
  fechaInscripcion: timestamp('fecha_inscripcion').notNull().defaultNow(),
  asistio: boolean('asistio').notNull().default(false),
});
