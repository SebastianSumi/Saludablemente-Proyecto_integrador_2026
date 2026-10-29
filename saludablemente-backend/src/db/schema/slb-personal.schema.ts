import { pgSchema, bigserial, varchar, date, bigint, boolean, timestamp } from 'drizzle-orm/pg-core';

export const slbPersonalSchema = pgSchema('slb_personal');

export const persona = slbPersonalSchema.table('persona', {
  idPersona: bigserial('id_persona', { mode: 'number' }).primaryKey(),
  nombres: varchar('nombres', { length: 100 }).notNull(),
  apellidoPaterno: varchar('apellido_paterno', { length: 100 }).notNull(),
  apellidoMaterno: varchar('apellido_materno', { length: 100 }),
  celular: varchar('celular', { length: 15 }).unique(),
  fechaNacimiento: date('fecha_nacimiento').notNull(),
  sexo: varchar('sexo', { length: 1 }).notNull(),
  email: varchar('email', { length: 120 }),
  idArea: bigint('id_area', { mode: 'number' }),
  tallaPolo: varchar('talla_polo', { length: 5 }),
  estadoCivil: varchar('estado_civil', { length: 20 }),
  nivelEducativo: varchar('nivel_educativo', { length: 30 }),
  idEquipo: bigint('id_equipo', { mode: 'number' }),
  activo: boolean('activo').notNull().default(true),
  documentoIdentidad: varchar('documento_identidad', { length: 15 }).notNull().unique(),
  avatarUrl: varchar('avatar_url', { length: 250 }),
});

export const credencialPrograma = slbPersonalSchema.table('credencial_programa', {
  idCredencial: bigserial('id_credencial', { mode: 'number' }).primaryKey(),
  idPersona: bigint('id_persona', { mode: 'number' }).notNull(),
  codigoQr: varchar('codigo_qr', { length: 100 }).notNull().unique(),
  fechaEmision: timestamp('fecha_emision').notNull(),
  activo: boolean('activo').notNull().default(true),
});

export const preferenciaComunicacion = slbPersonalSchema.table('preferencia_comunicacion', {
  idPreferencia: bigserial('id_preferencia', { mode: 'number' }).primaryKey(),
  idPersona: bigint('id_persona', { mode: 'number' }).notNull().unique(),
  canalPreferido: varchar('canal_preferido', { length: 20 }).notNull(),
  recibirNotificaciones: boolean('recibir_notificaciones').notNull().default(true),
});
