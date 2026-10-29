import { pgSchema, bigserial, varchar, text, bigint, timestamp, boolean } from 'drizzle-orm/pg-core';

export const slbNoticiasSchema = pgSchema('slb_noticias');

export const categoria = slbNoticiasSchema.table('categoria', {
  idCategoria: bigserial('id_categoria', { mode: 'number' }).primaryKey(),
  nombre: varchar('nombre', { length: 50 }).notNull().unique(),
  descripcion: varchar('descripcion', { length: 255 }),
});

export const noticia = slbNoticiasSchema.table('noticia', {
  idNoticia: bigserial('id_noticia', { mode: 'number' }).primaryKey(),
  titulo: varchar('titulo', { length: 200 }).notNull(),
  resumen: varchar('resumen', { length: 500 }),
  contenido: text('contenido').notNull(), // @Lob migrado a text
  imagenPortada: varchar('imagen_portada', { length: 500 }),
  idCategoria: bigint('id_categoria', { mode: 'number' }),
  idAutor: bigint('id_autor', { mode: 'number' }).notNull(),
  fechaPublicacion: timestamp('fecha_publicacion').notNull().defaultNow(),
  activo: boolean('activo').notNull().default(true),
  esOficial: boolean('es_oficial').notNull().default(false),
});

export const noticiaTeam = slbNoticiasSchema.table('noticia_team', {
  idNoticiaTeam: bigserial('id_noticia_team', { mode: 'number' }).primaryKey(),
  idNoticia: bigint('id_noticia', { mode: 'number' }).notNull(),
  idEquipo: bigint('id_equipo', { mode: 'number' }).notNull(),
});
