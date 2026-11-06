import { BandejaNotificacion } from './models';

export type OrigenColor =
  | 'primary'
  | 'secondary'
  | 'success'
  | 'warning'
  | 'error'
  | 'violet';

export interface OrigenVisual {
  icono: string;
  color: OrigenColor;
  etiqueta: string;
}

const ORIGENES: Record<string, OrigenVisual> = {
  social: { icono: '@tui.messages-square', color: 'primary', etiqueta: 'Social' },
  alertaclinica: { icono: '@tui.bell', color: 'error', etiqueta: 'Alertas clínicas' },
  alertasclinicas: { icono: '@tui.bell', color: 'error', etiqueta: 'Alertas clínicas' },
  alerta: { icono: '@tui.bell', color: 'error', etiqueta: 'Alertas clínicas' },
  metas: { icono: '@tui.target', color: 'success', etiqueta: 'Metas' },
  asistencias: { icono: '@tui.clipboard-check', color: 'warning', etiqueta: 'Asistencias' },
  actividades: { icono: '@tui.calendar', color: 'primary', etiqueta: 'Actividades' },
  nutricional: { icono: '@tui.apple', color: 'success', etiqueta: 'Nutricional' },
  aptitud: { icono: '@tui.activity', color: 'violet', etiqueta: 'Aptitud física' },
  aptitudfisica: { icono: '@tui.activity', color: 'violet', etiqueta: 'Aptitud física' },
  noticias: { icono: '@tui.newspaper', color: 'secondary', etiqueta: 'Noticias' },
  personal: { icono: '@tui.users', color: 'primary', etiqueta: 'Personal' },
  seguridad: { icono: '@tui.shield', color: 'error', etiqueta: 'Seguridad' },
  catalogos: { icono: '@tui.folder', color: 'secondary', etiqueta: 'Catálogos' },
  recordatorios: { icono: '@tui.bell-ring', color: 'warning', etiqueta: 'Recordatorios' },
  sistema: { icono: '@tui.inbox', color: 'primary', etiqueta: 'Sistema' },
};

const CANALES: Record<string, string> = {
  EMAIL: 'Email',
  IN_APP: 'En la app',
  PUSH: 'Push',
  SMS: 'SMS',
};

export function claveModulo(origen?: string): string {
  return (origen ?? '').toLowerCase().replace(/[_-]/g, '').trim();
}

export function origenVisual(origen?: string): OrigenVisual {
  const clave = claveModulo(origen);
  return (
    ORIGENES[clave] ?? {
      icono: '@tui.bell',
      color: 'primary',
      etiqueta: origen?.trim() ? origen : 'General',
    }
  );
}

export function etiquetaCanal(canal?: string): string {
  if (!canal) return '';
  return CANALES[canal.toUpperCase()] ?? canal;
}

export function rutaOrigenNotificacion(
  n: Pick<BandejaNotificacion, 'origenModulo' | 'idOrigenReferencia'>,
  esAdmin: boolean,
): string | null {
  const clave = claveModulo(n.origenModulo);
  const ref = n.idOrigenReferencia;

  switch (clave) {
    case 'social':
      return ref ? `/social/posts/${ref}` : '/social';
    case 'alertaclinica':
    case 'alertasclinicas':
    case 'alerta':
      if (!ref) return esAdmin ? '/alertaclinica/alertas' : '/colaborador/mis-alertas';
      return esAdmin
        ? `/alertaclinica/alertas/${ref}`
        : `/colaborador/mis-alertas/${ref}`;
    case 'metas':
      if (!ref) return esAdmin ? '/metas' : '/colaborador/mis-metas';
      return esAdmin ? `/metas/${ref}` : `/colaborador/mis-metas/${ref}`;
    case 'asistencias':
      return esAdmin ? '/asistencias/registro' : '/colaborador/mis-asistencias';
    case 'actividades':
      return ref ? `/actividades/${ref}` : '/actividades';
    case 'nutricional':
      if (!ref) return esAdmin ? '/nutricional/evaluaciones' : '/colaborador/mis-evaluaciones';
      return esAdmin
        ? `/nutricional/evaluaciones/${ref}`
        : `/colaborador/evaluaciones/nutricional/${ref}`;
    case 'aptitud':
    case 'aptitudfisica':
      if (!ref) return esAdmin ? '/aptitud' : '/colaborador/mis-evaluaciones';
      return esAdmin
        ? `/aptitud/${ref}`
        : `/colaborador/evaluaciones/aptitud/${ref}`;
    case 'noticias':
      if (!ref) return esAdmin ? '/noticias/admin' : '/colaborador/noticias';
      return esAdmin
        ? `/noticias/admin/${ref}`
        : `/colaborador/noticias/${ref}`;
    case 'personal':
      return ref ? `/personal/personas/${ref}` : '/personal/personas';
    case 'seguridad':
      return '/seguridad/usuarios';
    default:
      return null;
  }
}
