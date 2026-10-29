import { pgSchema, bigserial, varchar, text, timestamp, bigint, integer, smallint, numeric } from 'drizzle-orm/pg-core';

export const slbAlertasSchema = pgSchema('slb_alertas');

export const alertaClinica = slbAlertasSchema.table('alerta_clinica', {
  idAlerta: bigserial('id_alerta', { mode: 'number' }).primaryKey(),
  idPersona: bigint('id_persona', { mode: 'number' }).notNull(),
  nivelSeveridad: varchar('nivel_severidad', { length: 20 }).notNull(),
  estado: varchar('estado', { length: 20 }).notNull().default('PENDIENTE'),
  fechaGeneracion: timestamp('fecha_generacion').notNull().defaultNow(),
  fechaAtencion: timestamp('fecha_atencion'),
  observacion: text('observacion'),
});

export const alertaClinicaDetalle = slbAlertasSchema.table('alerta_clinica_detalle', {
  idDetalle: bigserial('id_detalle', { mode: 'number' }).primaryKey(),
  idAlerta: bigint('id_alerta', { mode: 'number' }).notNull(),
  parametro: varchar('parametro', { length: 50 }).notNull(),
  valorRegistrado: numeric('valor_registrado', { precision: 10, scale: 2 }).notNull(),
  rangoReferencia: varchar('rango_referencia', { length: 100 }),
});

export const alertaEtiqueta = slbAlertasSchema.table('alerta_etiqueta', {
  idAlertaEtiqueta: bigserial('id_alerta_etiqueta', { mode: 'number' }).primaryKey(),
  idAlerta: bigint('id_alerta', { mode: 'number' }).notNull(),
  idEtiqueta: bigint('id_etiqueta', { mode: 'number' }).notNull(),
});

export const etiquetaClinica = slbAlertasSchema.table('etiqueta_clinica', {
  idEtiqueta: bigserial('id_etiqueta', { mode: 'number' }).primaryKey(),
  nombre: varchar('nombre', { length: 50 }).notNull().unique(),
  color: varchar('color', { length: 7 }),
  descripcion: varchar('descripcion', { length: 255 }),
});

export const etiquetaParametro = slbAlertasSchema.table('etiqueta_parametro', {
  idEtiquetaParametro: bigserial('id_etiqueta_parametro', { mode: 'number' }).primaryKey(),
  idEtiqueta: bigint('id_etiqueta', { mode: 'number' }).notNull(),
  idParametro: bigint('id_parametro', { mode: 'number' }).notNull(),
});

export const parametroEvaluacion = slbAlertasSchema.table('parametro_evaluacion', {
  idParametro: bigserial('id_parametro', { mode: 'number' }).primaryKey(),
  codigo: varchar('codigo', { length: 50 }).notNull().unique(),
  nombre: varchar('nombre', { length: 100 }).notNull(),
  unidadMedida: varchar('unidad_medida', { length: 20 }),
  categoria: varchar('categoria', { length: 30 }),
});

export const usoCardAlerta = slbAlertasSchema.table('uso_card_alerta', {
  idUso: bigserial('id_uso', { mode: 'number' }).primaryKey(),
  idUsuario: bigint('id_usuario', { mode: 'number' }).notNull(),
  ruta: varchar('ruta', { length: 100 }).notNull(),
  contador: integer('contador').notNull().default(0),
  fechaUltimoUso: timestamp('fecha_ultimo_uso').notNull().defaultNow(),
});

// Helper para tablas de reglas clínicas estándar
const defineReglaTable = (nombre: string) =>
  slbAlertasSchema.table(nombre, {
    id: bigserial('id', { mode: 'number' }).primaryKey(),
    edadMin: smallint('edad_min').notNull(),
    edadMax: smallint('edad_max').notNull(),
    sexo: varchar('sexo', { length: 1 }),
    minimo: numeric('minimo', { precision: 10, scale: 2 }),
    maximo: numeric('maximo', { precision: 10, scale: 2 }),
    clasificacion: varchar('clasificacion', { length: 50 }).notNull(),
    nivelAlerta: varchar('nivel_alerta', { length: 20 }).notNull(),
  });

export const reglaGlucosa = defineReglaTable('regla_glucosa');
export const reglaColesterol = defineReglaTable('regla_colesterol');
export const reglaTrigliceridos = defineReglaTable('regla_trigliceridos');
export const reglaPresionArterial = defineReglaTable('regla_presion_arterial');
export const reglaImc = defineReglaTable('regla_imc');
export const reglaPorcentajeGrasa = defineReglaTable('regla_porcentaje_grasa');
export const reglaGrasaVisceral = defineReglaTable('regla_grasa_visceral');
export const reglaMasaMuscular = defineReglaTable('regla_masa_muscular');
export const reglaPerimetroAbdominal = defineReglaTable('regla_perimetro_abdominal');
export const reglaSaltoSinImpulso = defineReglaTable('regla_salto_sin_impulso');
export const reglaSaltaSoga = defineReglaTable('regla_salta_soga');
export const reglaPlanchas = defineReglaTable('regla_planchas');
export const reglaAbdominales = defineReglaTable('regla_abdominales');
export const reglaCarrera400m = defineReglaTable('regla_carrera_400m');
