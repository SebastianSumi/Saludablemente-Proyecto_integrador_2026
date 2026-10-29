import { describe, expect, it } from 'vitest';
import {
  homePorRol,
  normalizarRol,
  normalizarRoles,
  tieneAlgunRol,
} from './roles';

describe('roles', () => {
  it('normaliza nombres del backend y alias legacy', () => {
    expect(normalizarRol('Administrador')).toBe('Administrador');
    expect(normalizarRol('ADMIN')).toBe('Administrador');
    expect(normalizarRol('Supervisor')).toBe('Coordinador');
    expect(normalizarRol('Coordinador')).toBe('Coordinador');
    expect(normalizarRol('Evaluador')).toBe('Evaluador');
    expect(normalizarRol('Colaborador')).toBe('Colaborador');
    expect(normalizarRol('desconocido')).toBeNull();
  });

  it('tieneAlgunRol es case-insensitive y acepta alias', () => {
    expect(tieneAlgunRol(['ADMIN'], ['Administrador'])).toBe(true);
    expect(tieneAlgunRol(['Supervisor'], ['Coordinador'])).toBe(true);
    expect(tieneAlgunRol(['Colaborador'], ['Administrador'])).toBe(false);
    expect(tieneAlgunRol(['Colaborador'], [])).toBe(true);
  });

  it('normalizarRoles elimina duplicados', () => {
    expect(normalizarRoles(['ADMIN', 'Administrador', 'admin'])).toEqual([
      'Administrador',
    ]);
  });

  it('homePorRol prioriza Administrador', () => {
    expect(homePorRol(['Colaborador'])).toBe('/colaborador');
    expect(homePorRol(['ADMIN'])).toBe('/admin');
    expect(homePorRol(['Evaluador'])).toBe('/evaluador');
  });
});
