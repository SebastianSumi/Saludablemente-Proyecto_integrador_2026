import type {
  Asistencia,
  EstadoAsistencia,
  MarcaAsistencia,
} from '../../features/admin/asistencias/models';

export type VistaMarcas = 'tarjetas' | 'compacta' | 'kanban';

/** Fila del wizard con estado visible (borrador superpuesto). */
export interface FilaMarca {
  idPersona: number;
  nombre: string;
  asistencia: Asistencia | null;
  visible: EstadoAsistencia | null;
  modificado: boolean;
}

export interface MetaEstado {
  icono: string;
  pill: string;
  etiqueta: string;
}

/** Meta visual por estado visible (null = pendiente). */
export function metaEstado(v: EstadoAsistencia | null): MetaEstado {
  if (v == null) return { icono: '@tui.hourglass', pill: 'pill-soft-neutral', etiqueta: 'Pendiente' };
  if (v === 'ASISTIO') return { icono: '@tui.user-check', pill: 'pill-soft-success', etiqueta: 'Presente' };
  if (v === 'TARDE') return { icono: '@tui.clock', pill: 'pill-soft-warning', etiqueta: 'Tarde' };
  if (v === 'AUSENTE') return { icono: '@tui.user-x', pill: 'pill-soft-error', etiqueta: 'Falta' };
  if (v === 'EXONERADO')
    return { icono: '@tui.file-check', pill: 'pill-soft-primary', etiqueta: 'Exonerado' };
  return { icono: '@tui.triangle-alert', pill: 'pill-soft-warning', etiqueta: v };
}

/** Bucle: pendiente→presente→tarde→falta→presente (otros estados no ciclan). */
export function siguienteMarcaDe(visible: EstadoAsistencia | null): MarcaAsistencia | null {
  if (visible == null) return 'PRESENTE';
  if (visible === 'ASISTIO') return 'TARDE';
  if (visible === 'TARDE') return 'FALTA';
  if (visible === 'AUSENTE') return 'PRESENTE';
  return null;
}

export function etiquetaMarca(m: MarcaAsistencia): string {
  return m === 'PRESENTE' ? 'Presente' : m === 'TARDE' ? 'Tarde' : 'Falta';
}

/** Icono de la próxima acción del bucle. */
export function iconoAccion(visible: EstadoAsistencia | null): string {
  const s = siguienteMarcaDe(visible);
  if (s == null) return '@tui.minus';
  if (visible == null) return '@tui.check';
  if (s === 'PRESENTE') return '@tui.user-check';
  if (s === 'TARDE') return '@tui.clock';
  return '@tui.user-x';
}

export const VISTAS_MARCAS: { id: VistaMarcas; etiqueta: string; icono: string }[] = [
  { id: 'compacta', etiqueta: 'Compacta', icono: '@tui.list' },
  { id: 'tarjetas', etiqueta: 'Tarjetas', icono: '@tui.layout-grid' },
  { id: 'kanban', etiqueta: 'Kanban', icono: '@tui.kanban' },
];

const PREF_VISTA = 'wizard-vista';

export function leerVistaGuardada(): VistaMarcas {
  try {
    const v = localStorage.getItem(PREF_VISTA);
    if (v === 'tarjetas' || v === 'compacta' || v === 'kanban') return v;
  } catch {
    // ignorar
  }
  return 'compacta';
}

export function guardarVista(v: VistaMarcas): void {
  try {
    localStorage.setItem(PREF_VISTA, v);
  } catch {
    // ignorar
  }
}
