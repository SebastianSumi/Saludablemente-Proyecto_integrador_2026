import { HttpErrorResponse } from '@angular/common/http';
import { PostDetalle, PostResumen, TipoMencion } from './social.model';
export function errorSocial(error: unknown): string {
  if (error instanceof HttpErrorResponse) {
    if (error.status === 0)
      return 'No se pudo conectar. Comprueba tu conexión e inténtalo de nuevo.';
    if (error.status === 404)
      return 'El contenido no está disponible. Puede haberse eliminado u ocultado.';
    if (error.status === 403) return 'No tienes permiso para realizar esta acción.';
    if (error.status === 401) return 'Tu sesión ha caducado. Vuelve a iniciar sesión.';
    const body = error.error;
    if (body && typeof body === 'object') {
      const message = body.message ?? body.mensaje ?? body.detail;
      if (typeof message === 'string') return message;
    }
    if (error.status === 409)
      return 'La operación entra en conflicto con el estado actual. Actualiza e inténtalo de nuevo.';
  }
  return 'No se pudo completar la operación. Inténtalo de nuevo.';
}
export function rutaMencion(tipo: TipoMencion, id: number): string {
  return tipo === 'USUARIO'
    ? `/social/usuarios/${id}`
    : tipo === 'EQUIPO'
      ? `/social/equipos/${id}`
      : `/social/actividades/${id}`;
}
export function resumenDetalle(p: PostDetalle): PostResumen {
  return {
    ...p,
    reaccionesPorTipo: p.reacciones.porTipo,
    totalReacciones: p.reacciones.total,
    reaccionDelUsuario: p.reacciones.reaccionDelUsuario,
    postOriginalId: p.postOriginal?.idPost ?? null,
  };
}
export function urlsImagenes(text: string): string[] {
  return text
    .split(/\r?\n/)
    .map((x) => x.trim())
    .filter(Boolean);
}
export function imagenesValidas(urls: string[]): boolean {
  return urls.every((value) => {
    try {
      const url = new URL(value);
      return value.length <= 500 && ['https:', 'http:'].includes(url.protocol) && !!url.hostname;
    } catch {
      return false;
    }
  });
}
export function adjuntoValido(file: File): boolean {
  return (
    file.size > 0 &&
    file.size <= 5 * 1024 * 1024 &&
    ['image/jpeg', 'image/png', 'image/webp'].includes(file.type) &&
    /\.(jpe?g|png|webp)$/i.test(file.name)
  );
}
