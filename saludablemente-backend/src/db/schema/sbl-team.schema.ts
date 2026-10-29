import { pgSchema, bigserial, varchar, bigint, boolean } from 'drizzle-orm/pg-core';

export const sblTeamSchema = pgSchema('sbl_team');

export const equipo = sblTeamSchema.table('equipo', {
  idEquipo: bigserial('id_equipo', { mode: 'number' }).primaryKey(),
  nombre: varchar('nombre', { length: 100 }).notNull(),
  descripcion: varchar('descripcion', { length: 255 }),
  imagenKey: varchar('imagen_key', { length: 512 }),
  color: varchar('color', { length: 7 }),
  slogan: varchar('slogan', { length: 150 }),
  referenciaBiblica: varchar('referencia_biblica', { length: 100 }),
  textoVersiculo: varchar('texto_versiculo', { length: 1000 }),
  idUsuarioLider: bigint('id_usuario_lider', { mode: 'number' }),
  version: bigint('version', { mode: 'number' }).notNull().default(0),
  activo: boolean('activo').notNull().default(true),
});
