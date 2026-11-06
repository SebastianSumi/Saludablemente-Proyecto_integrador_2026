export type RolApp = 'Administrador' | 'Coordinador' | 'Evaluador' | 'Colaborador';

export const ROLES_APP: readonly RolApp[] = [
  'Administrador',
  'Coordinador',
  'Evaluador',
  'Colaborador',
];

const ALIAS: Record<string, RolApp> = {
  admin: 'Administrador',
  administrador: 'Administrador',
  root: 'Administrador',
  coordinador: 'Coordinador',
  supervisor: 'Coordinador',
  evaluador: 'Evaluador',
  colaborador: 'Colaborador',
};

export function normalizarRol(raw: string): RolApp | null {
  if (!raw) return null;
  const key = raw.trim().toLowerCase();
  return ALIAS[key] ?? null;
}

export function normalizarRoles(raw: string[]): RolApp[] {
  const out: RolApp[] = [];
  for (const r of raw ?? []) {
    const n = normalizarRol(r);
    if (n && !out.includes(n)) out.push(n);
  }
  return out;
}

export function tieneAlgunRol(actuales: string[], esperados: string[]): boolean {
  const normActuales = normalizarRoles(actuales);
  const normEsperados = normalizarRoles(esperados);
  if (normEsperados.length === 0) return true;
  return normEsperados.some((r) => normActuales.includes(r));
}

export function homePorRol(roles: string[]): string {
  const n = normalizarRoles(roles);
  if (n.includes('Administrador')) return '/admin';
  if (n.includes('Evaluador')) return '/evaluador';
  if (n.includes('Coordinador')) return '/colaborador';
  return '/colaborador';
}
