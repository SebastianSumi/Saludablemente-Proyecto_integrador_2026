import { pgSchema, bigserial, varchar, boolean, timestamp, bigint, primaryKey } from 'drizzle-orm/pg-core';

export const slbSeguridadSchema = pgSchema('slb_seguridad');

export const rol = slbSeguridadSchema.table('rol', {
  idRol: bigserial('id_rol', { mode: 'number' }).primaryKey(),
  nombre: varchar('nombre', { length: 40 }).notNull().unique(),
  descripcion: varchar('descripcion', { length: 150 }),
});

export const usuario = slbSeguridadSchema.table('usuario', {
  idUsuario: bigserial('id_usuario', { mode: 'number' }).primaryKey(),
  email: varchar('email', { length: 120 }).notNull().unique(),
  username: varchar('username', { length: 60 }).notNull().unique(),
  passwordHash: varchar('password_hash', { length: 255 }).notNull(),
  activo: boolean('activo').notNull().default(true),
  fechaCreacion: timestamp('fecha_creacion').notNull().defaultNow(),

  // Snapshot de identidad (anticorrupción hacia Personal)
  idPersonaSnapshot: bigint('id_persona', { mode: 'number' }).notNull().unique(),
  documentoIdentidadSnapshot: varchar('documento_identidad', { length: 15 }).notNull(),
  nombresSnapshot: varchar('nombres', { length: 100 }).notNull(),
  apellidoPaternoSnapshot: varchar('apellido_paterno', { length: 100 }).notNull(),
  apellidoMaternoSnapshot: varchar('apellido_materno', { length: 100 }),
  areaTrabajoSnapshot: varchar('area_trabajo', { length: 100 }),
  equipoSnapshot: varchar('equipo_snapshot', { length: 100 }),
  activoPersonaSnapshot: boolean('activo_persona').notNull(),
  snapshotFecha: timestamp('snapshot_fecha').notNull(),
});

export const usuarioRol = slbSeguridadSchema.table(
  'usuario_rol',
  {
    idUsuario: bigint('id_usuario', { mode: 'number' }).notNull().references(() => usuario.idUsuario),
    idRol: bigint('id_rol', { mode: 'number' }).notNull().references(() => rol.idRol),
  },
  (table) => ({
    pk: primaryKey({ columns: [table.idUsuario, table.idRol] }),
  })
);
