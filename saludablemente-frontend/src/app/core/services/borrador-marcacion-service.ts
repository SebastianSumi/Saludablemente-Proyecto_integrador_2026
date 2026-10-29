import { Injectable } from '@angular/core';
import type { MarcaAsistencia } from '../../features/admin/asistencias/models';

export type MapaBorrador = Record<number, MarcaAsistencia>;

interface CacheBorrador {
  marcas: MapaBorrador;
  savedAt: string;
}

const PREFIJO = 'wizard-marcacion:';
const TTL_MS = 24 * 3600 * 1000;

/**
 * Borrador de marcación en caché web (localStorage).
 * Nunca toca el backend: solo Guardar envía. Clave por actividad.
 */
@Injectable({ providedIn: 'root' })
export class BorradorMarcacionService {
  private clave(idActividad: number): string {
    return `${PREFIJO}${idActividad}`;
  }

  cargar(idActividad: number): MapaBorrador {
    try {
      const raw = localStorage.getItem(this.clave(idActividad));
      if (!raw) return {};
      const data = JSON.parse(raw) as CacheBorrador;
      if (!data?.marcas || typeof data.marcas !== 'object') return {};
      if (!data.savedAt || Date.now() - new Date(data.savedAt).getTime() > TTL_MS) {
        this.limpiar(idActividad);
        return {};
      }
      const limpio: MapaBorrador = {};
      for (const [k, v] of Object.entries(data.marcas)) {
        const id = Number(k);
        if (Number.isInteger(id) && (v === 'PRESENTE' || v === 'TARDE' || v === 'FALTA')) {
          limpio[id] = v;
        }
      }
      return limpio;
    } catch {
      return {};
    }
  }

  guardar(idActividad: number, marcas: MapaBorrador): void {
    try {
      if (Object.keys(marcas).length === 0) {
        this.limpiar(idActividad);
        return;
      }
      const data: CacheBorrador = { marcas, savedAt: new Date().toISOString() };
      localStorage.setItem(this.clave(idActividad), JSON.stringify(data));
    } catch {
      // caché llena o bloqueada: se sigue sin persistir
    }
  }

  limpiar(idActividad: number): void {
    try {
      localStorage.removeItem(this.clave(idActividad));
    } catch {
      // ignorar
    }
  }
}
