/**
 * Ilustraciones del módulo Mis Evaluaciones.
 * Coloca las imágenes en `public/img/Evaluaciones/` con estos nombres:
 *   - Evaluaciones.webp  (portada del hero)
 *   - Nutricional.webp   (evaluaciones nutricionales)
 *   - Aptitud.webp       (evaluaciones de aptitud física)
 * Mientras el archivo no exista se verá la imagen rota; basta con soltar
 * el .webp con el nombre correcto y queda funcionando.
 */

const BASE = '/img/Evaluaciones';

function url(nombre: string): string {
  return encodeURI(`${BASE}/${nombre}.webp`);
}

export const IMG_EVALUACIONES = url('Evaluaciones');

/** Portada genérica del módulo (hero). */
export function imagenPortada(): string {
  return IMG_EVALUACIONES;
}

/** Imagen por tipo de evaluación. */
export function imagenTipo(tipo: 'nutricional' | 'aptitud'): string {
  return tipo === 'nutricional' ? url('Nutricional') : url('Aptitud');
}
