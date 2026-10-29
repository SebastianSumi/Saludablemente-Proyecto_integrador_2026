import { Component, computed, inject, signal } from '@angular/core';
import { DatePipe, TitleCasePipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { TuiButton, TuiIcon } from '@taiga-ui/core';
import { TuiDialogContext } from '@taiga-ui/core/portals/dialog';
import { injectContext } from '@taiga-ui/polymorpheus';
import { type Actividad, urlImagenActividad } from '../models';

export interface QuickViewDatos {
  /** Ficha directa de una actividad. */
  actividad?: Actividad;
  /** Día en formato yyyy-MM-dd para los modos día/vacío. */
  fecha?: string;
  /** Actividades de ese día (modo día). */
  delDia?: Actividad[];
}

type Modo = 'ficha' | 'dia' | 'vacio';

@Component({
  selector: 'app-actividad-quick-view',
  imports: [DatePipe, TitleCasePipe, RouterLink, TuiButton, TuiIcon],
  templateUrl: './actividad-quick-view.html',
  styleUrl: './actividad-quick-view.css',
})
export class ActividadQuickView {
  private readonly context =
    injectContext<TuiDialogContext<void, QuickViewDatos>>();

  protected readonly actividad = signal<Actividad | null>(
    this.context.data?.actividad ?? null,
  );
  protected readonly fecha = signal<string>(
    this.context.data?.actividad?.fechaActividad?.slice(0, 10) ??
      this.context.data?.fecha ??
      '',
  );
  protected readonly delDia = signal<Actividad[]>(
    this.context.data?.delDia ?? [],
  );
  protected readonly modo = signal<Modo>(
    this.context.data?.actividad
      ? 'ficha'
      : (this.context.data?.delDia?.length ?? 0) > 0
        ? 'dia'
        : 'vacio',
  );

  protected readonly fechaLarga = computed(() => {
    const iso = this.fecha();
    if (!iso) return '';
    const d = new Date(iso + 'T12:00:00');
    return d.toLocaleDateString('es-PE', {
      weekday: 'long',
      day: 'numeric',
      month: 'long',
      year: 'numeric',
    });
  });

  protected resolverUrl(url?: string | null): string | undefined {
    return urlImagenActividad(url);
  }

  protected verFicha(a: Actividad): void {
    this.actividad.set(a);
    this.fecha.set(a.fechaActividad.slice(0, 10));
    this.modo.set('ficha');
  }

  protected volverAlDia(): void {
    this.modo.set('dia');
  }

  protected cerrar(): void {
    this.context.completeWith(undefined);
  }

  protected claseEstado(a: Actividad): string {
    switch (a.estado) {
      case 'CANCELADA':
        return 'pill-error';
      case 'FINALIZADA':
      case 'CERRADA':
        return 'pill-success';
      case 'EN_CURSO':
        return 'pill-warning';
      case 'PROGRAMADA':
        return this.esFutura(a) ? 'pill-primary' : 'pill-secondary';
      default:
        return 'pill-neutral';
    }
  }

  protected etiquetaEstado(a: Actividad): string {
    switch (a.estado) {
      case 'PROGRAMADA':
        return this.esFutura(a) ? 'Planificada' : 'Programada';
      case 'EN_CURSO':
        return 'En curso';
      case 'FINALIZADA':
      case 'CERRADA':
        return 'Finalizada';
      case 'CANCELADA':
        return 'Cancelada';
      default:
        return a.estado;
    }
  }

  private esFutura(a: Actividad): boolean {
    const hoy = new Date();
    const iso = `${hoy.getFullYear()}-${String(hoy.getMonth() + 1).padStart(2, '0')}-${String(hoy.getDate()).padStart(2, '0')}`;
    return a.fechaActividad.slice(0, 10) > iso;
  }

  protected enlaceMapa(a: Actividad): string {
    if (a.latitudOficial != null && a.longitudOficial != null) {
      return `https://www.google.com/maps/search/?api=1&query=${a.latitudOficial},${a.longitudOficial}`;
    }
    return `https://www.google.com/maps/search/?api=1&query=${encodeURIComponent(a.lugar)}`;
  }

  protected duracion(a: Actividad): string | null {
    if (!a.horaInicio || !a.horaFin) return null;
    const aMin = this.aMinutos(a.horaInicio);
    const bMin = this.aMinutos(a.horaFin);
    if (aMin == null || bMin == null || bMin <= aMin) return null;
    const min = bMin - aMin;
    if (min < 60) return `${min} min`;
    const h = Math.floor(min / 60);
    const r = min % 60;
    return r === 0 ? `${h} h` : `${h} h ${r} min`;
  }

  private aMinutos(hora: string): number | null {
    const m = /^(\d{1,2}):(\d{2})/.exec(hora);
    if (!m) return null;
    return Number(m[1]) * 60 + Number(m[2]);
  }
}
