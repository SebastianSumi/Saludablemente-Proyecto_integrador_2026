import { pgSchema, bigserial, varchar, bigint, date, timestamp, boolean, integer, primaryKey } from 'drizzle-orm/pg-core';

export const slbSocialSchema = pgSchema('slb_social');

export const post = slbSocialSchema.table('post', {
  idPost: bigserial('id_post', { mode: 'number' }).primaryKey(),
  tipoContenido: varchar('tipo_contenido', { length: 20 }).notNull(), // ACTIVIDAD, NOTICIA, MANUAL, SHARE
  refId: bigint('ref_id', { mode: 'number' }),
  origenPostId: bigint('origen_post_id', { mode: 'number' }),
  idEquipoAutor: bigint('id_equipo_autor', { mode: 'number' }),
  titulo: varchar('titulo', { length: 150 }).notNull(),
  resumen: varchar('resumen', { length: 500 }),
  lugar: varchar('lugar', { length: 150 }),
  fechaEvento: date('fecha_evento'),
  horaInicio: varchar('hora_inicio', { length: 8 }),
  horaFin: varchar('hora_fin', { length: 8 }),
  refEstado: varchar('ref_estado', { length: 20 }),
  imagenUrl: varchar('imagen_url', { length: 500 }),
  coordinadorNombre: varchar('coordinador_nombre', { length: 200 }),
  autorId: bigint('autor_id', { mode: 'number' }).notNull(),
  fechaPublicacion: timestamp('fecha_publicacion').notNull().defaultNow(),
  fechaActualizacion: timestamp('fecha_actualizacion'),
  activo: boolean('activo').notNull().default(true),
  esOficial: boolean('es_oficial').notNull().default(false),
  alcanceTotal: bigint('alcance_total', { mode: 'number' }).notNull().default(0),
  sharesTotal: bigint('shares_total', { mode: 'number' }).notNull().default(0),
});

export const postImagen = slbSocialSchema.table('post_imagen', {
  idPostImagen: bigserial('id_post_imagen', { mode: 'number' }).primaryKey(),
  idPost: bigint('id_post', { mode: 'number' }).notNull(),
  url: varchar('url', { length: 500 }).notNull(),
  orden: integer('orden'),
});

export const postHashtag = slbSocialSchema.table('post_hashtag', {
  idPostHashtag: bigserial('id_post_hashtag', { mode: 'number' }).primaryKey(),
  idPost: bigint('id_post', { mode: 'number' }).notNull(),
  tag: varchar('tag', { length: 50 }).notNull(),
});

export const postVista = slbSocialSchema.table('post_vista', {
  idPostVista: bigserial('id_post_vista', { mode: 'number' }).primaryKey(),
  idPost: bigint('id_post', { mode: 'number' }).notNull(),
  idUsuario: bigint('id_usuario', { mode: 'number' }).notNull(),
  fechaPrimeraVista: timestamp('fecha_primera_vista').notNull().defaultNow(),
  fechaUltimaVista: timestamp('fecha_ultima_vista').notNull().defaultNow(),
});

export const postGuardado = slbSocialSchema.table(
  'post_guardado',
  {
    idPost: bigint('id_post', { mode: 'number' }).notNull(),
    idUsuario: bigint('id_usuario', { mode: 'number' }).notNull(),
    fechaGuardado: timestamp('fecha_guardado').notNull().defaultNow(),
  },
  (table) => ({
    pk: primaryKey({ columns: [table.idPost, table.idUsuario] }),
  })
);

export const postFijado = slbSocialSchema.table('post_fijado', {
  idPostFijado: bigserial('id_post_fijado', { mode: 'number' }).primaryKey(),
  idPost: bigint('id_post', { mode: 'number' }).notNull(),
  scopeTipo: varchar('scope_tipo', { length: 20 }).notNull(), // GLOBAL, EQUIPO, ACTIVIDAD
  scopeId: bigint('scope_id', { mode: 'number' }),
  fechaFijado: timestamp('fecha_fijado').notNull().defaultNow(),
  idUsuarioFijador: bigint('id_usuario_fijador', { mode: 'number' }).notNull(),
  fechaExpiracion: timestamp('fecha_expiracion'),
});

export const postReporte = slbSocialSchema.table('post_reporte', {
  idPostReporte: bigserial('id_post_reporte', { mode: 'number' }).primaryKey(),
  idPost: bigint('id_post', { mode: 'number' }),
  idComentario: bigint('id_comentario', { mode: 'number' }),
  idUsuarioReporta: bigint('id_usuario_reporta', { mode: 'number' }).notNull(),
  motivo: varchar('motivo', { length: 50 }).notNull(), // SPAM, OFENSIVO, ACOSO, FALSO, OTRO
  detalle: varchar('detalle', { length: 500 }),
  fechaReporte: timestamp('fecha_reporte').notNull().defaultNow(),
  estado: varchar('estado', { length: 20 }).notNull().default('PENDIENTE'),
  idUsuarioRevisor: bigint('id_usuario_revisor', { mode: 'number' }),
  fechaRevision: timestamp('fecha_revision'),
  accionTomada: varchar('accion_tomada', { length: 50 }),
});

export const comentario = slbSocialSchema.table('comentario', {
  idComentario: bigserial('id_comentario', { mode: 'number' }).primaryKey(),
  idPost: bigint('id_post', { mode: 'number' }).notNull(),
  idComentarioPadre: bigint('id_comentario_padre', { mode: 'number' }),
  idUsuarioAutor: bigint('id_usuario_autor', { mode: 'number' }).notNull(),
  contenido: varchar('contenido', { length: 1000 }).notNull(),
  editado: boolean('editado').notNull().default(false),
  fechaEdicion: timestamp('fecha_edicion'),
  fechaComentario: timestamp('fecha_comentario').notNull().defaultNow(),
  activo: boolean('activo').notNull().default(true),
});

export const comentarioAdjunto = slbSocialSchema.table('comentario_adjunto', {
  idComentarioAdjunto: bigserial('id_comentario_adjunto', { mode: 'number' }).primaryKey(),
  idComentario: bigint('id_comentario', { mode: 'number' }).notNull(),
  url: varchar('url', { length: 500 }).notNull(),
  orden: integer('orden'),
  mime: varchar('mime', { length: 50 }),
  sizeBytes: bigint('size_bytes', { mode: 'number' }),
});

export const comentarioHashtag = slbSocialSchema.table('comentario_hashtag', {
  idComentarioHashtag: bigserial('id_comentario_hashtag', { mode: 'number' }).primaryKey(),
  idComentario: bigint('id_comentario', { mode: 'number' }).notNull(),
  tag: varchar('tag', { length: 50 }).notNull(),
});

export const mencion = slbSocialSchema.table('mencion', {
  idMencion: bigserial('id_mencion', { mode: 'number' }).primaryKey(),
  tipoContenido: varchar('tipo_contenido', { length: 20 }).notNull(), // POST, COMENTARIO
  idContenido: bigint('id_contenido', { mode: 'number' }).notNull(),
  tipoMencion: varchar('tipo_mencion', { length: 20 }).notNull(),     // USUARIO, ACTIVIDAD, EQUIPO
  refId: bigint('ref_id', { mode: 'number' }).notNull(),
  offsetInicio: integer('offset_inicio'),
  offsetFin: integer('offset_fin'),
});

export const reaccionPost = slbSocialSchema.table(
  'reaccion_post',
  {
    idPost: bigint('id_post', { mode: 'number' }).notNull(),
    idUsuario: bigint('id_usuario', { mode: 'number' }).notNull(),
    tipo: varchar('tipo', { length: 20 }).notNull(), // LIKE, LOVE, HAHA, WOW, SAD, APLAUSO, ENOJADO
    fechaReaccion: timestamp('fecha_reaccion').notNull().defaultNow(),
  },
  (table) => ({
    pk: primaryKey({ columns: [table.idPost, table.idUsuario] }),
  })
);

export const reaccionComentario = slbSocialSchema.table(
  'reaccion_comentario',
  {
    idComentario: bigint('id_comentario', { mode: 'number' }).notNull(),
    idUsuario: bigint('id_usuario', { mode: 'number' }).notNull(),
    tipo: varchar('tipo', { length: 20 }).notNull(),
    fechaReaccion: timestamp('fecha_reaccion').notNull().defaultNow(),
  },
  (table) => ({
    pk: primaryKey({ columns: [table.idComentario, table.idUsuario] }),
  })
);
