/**
 * Ilustraciones del módulo Alertas Clínicas.
 * Archivos WebP optimizados en `public/img/AlertasClinicas/`
 * (los nombres llevan espacios: se sirven con encodeURI).
 */

const BASE = '/img/AlertasClinicas';

function url(nombre: string): string {
  return encodeURI(`${BASE}/${nombre}.webp`);
}

export const IMG_ALERTAS = url('Alertas');
export const IMG_DASHBOARD_CLINICO = url('Dashboards');

const POR_REGLA: Record<string, string> = {
  'perimetro-abdominal': 'Perimetro Abdominal',
  'porcentaje-grasa': 'Porcentaje de grasa',
  'masa-muscular': 'masa muscular',
  imc: 'IMC Indice de masa corporal',
  'grasa-visceral': 'Grasa visceral',
  glucosa: 'Glucosa',
  colesterol: 'Colesterol',
  trigliceridos: 'Trigliceridos',
  'presion-arterial': 'Presion arterial',
};

/** Imagen de portada para una regla (slug de ruta). Desconocido → genérica. */
export function imagenRegla(slug: string | null | undefined): string {
  const nombre = (slug ?? '').trim().toLowerCase();
  const archivo = POR_REGLA[nombre];
  return archivo ? url(archivo) : IMG_ALERTAS;
}

/** Imagen genérica del módulo (bandeja, detalle, tablero). */
export function imagenPortada(): string {
  return IMG_ALERTAS;
}
