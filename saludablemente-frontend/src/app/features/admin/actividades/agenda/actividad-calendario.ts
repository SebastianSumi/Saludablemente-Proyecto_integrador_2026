import { Component, computed, inject, input, signal } from '@angular/core';
import { TuiButton, TuiDialogService, TuiIcon } from '@taiga-ui/core';
import { PolymorpheusComponent } from '@taiga-ui/polymorpheus';
import type { Actividad } from '../models';
import {
  ActividadQuickView,
  type QuickViewDatos,
} from './actividad-quick-view';

interface Celda {
  iso: string;
  dia: number;
  esMes: boolean;
  esHoy: boolean;
}

const DIAS = ['Lun', 'Mar', 'Mié', 'Jue', 'Vie', 'Sáb', 'Dom'];
const MESES = [
  'Enero', 'Febrero', 'Marzo', 'Abril', 'Mayo', 'Junio',
  'Julio', 'Agosto', 'Septiembre', 'Octubre', 'Noviembre', 'Diciembre',
];
const MAX_PILLS = 3;

@Component({
  selector: 'app-actividad-calendario',
  imports: [TuiButton, TuiIcon],
  templateUrl: './actividad-calendario.html',
  styleUrl: './actividad-calendario.css',
})
export class ActividadCalendario {
  private readonly dialogs = inject(TuiDialogService);

  readonly actividades = input<Actividad[]>([]);

  protected readonly DIAS = DIAS;
  protected readonly MAX_PILLS = MAX_PILLS;

  private readonly hoy = new Date();
  protected readonly hoyIso = signal(this.isoDe(this.hoy));
  protected readonly mes = signal({
    y: this.hoy.getFullYear(),
    m: this.hoy.getMonth(),
  });

  protected readonly nombreMes = computed(() => {
    const v = this.mes();
    return `${MESES[v.m]} ${v.y}`;
  });

  /** Mapa fecha (yyyy-MM-dd) → actividades ordenadas por hora. */
  protected readonly porFecha = computed(() => {
    const mapa = new Map<string, Actividad[]>();
    for (const a of this.actividades()) {
      const iso = (a.fechaActividad ?? '').slice(0, 10);
      if (!/^\d{4}-\d{2}-\d{2}$/.test(iso)) continue;
      const lista = mapa.get(iso) ?? [];
      lista.push(a);
      mapa.set(iso, lista);
    }
    for (const lista of mapa.values()) {
      lista.sort((x, y) =>
        (x.horaInicio ?? '').localeCompare(y.horaInicio ?? ''),
      );
    }
    return mapa;
  });

  /** 42 celdas Lun–Dom que cubren el mes visible. */
  protected readonly celdas = computed<Celda[]>(() => {
    const { y, m } = this.mes();
    const primero = new Date(y, m, 1);
    const desfase = (primero.getDay() + 6) % 7;
    const out: Celda[] = [];
    for (let i = 0; i < 42; i++) {
      const d = new Date(y, m, 1 - desfase + i);
      const iso = this.isoDe(d);
      out.push({
        iso,
        dia: d.getDate(),
        esMes: d.getMonth() === m,
        esHoy: iso === this.hoyIso(),
      });
    }
    return out;
  });

  protected itemsDe(iso: string): Actividad[] {
    return this.porFecha().get(iso) ?? [];
  }

  protected mesAnterior(): void {
    this.mes.update((v) => ({
      y: v.m === 0 ? v.y - 1 : v.y,
      m: v.m === 0 ? 11 : v.m - 1,
    }));
  }

  protected mesSiguiente(): void {
    this.mes.update((v) => ({
      y: v.m === 11 ? v.y + 1 : v.y,
      m: v.m === 11 ? 0 : v.m + 1,
    }));
  }

  protected irAHoy(): void {
    this.mes.set({ y: this.hoy.getFullYear(), m: this.hoy.getMonth() });
  }

  /**
   * Click en insignia: abre la lista del día (las insignias se mantienen
   * en la celda). El mini detalle aparece al presionar una actividad.
   */
  protected abrirFicha(event: Event, a: Actividad): void {
    event.stopPropagation();
    const iso = a.fechaActividad.slice(0, 10);
    this.abrir({ fecha: iso, delDia: this.itemsDe(iso) });
  }

  /** Click en día (número o zona vacía) → lista del día o vacío. */
  protected abrirDia(iso: string): void {
    const items = this.itemsDe(iso);
    this.abrir(
      items.length > 0
        ? { fecha: iso, delDia: items }
        : { fecha: iso, delDia: [] },
    );
  }

  private abrir(datos: QuickViewDatos): void {
    this.dialogs
      .open<void>(new PolymorpheusComponent(ActividadQuickView), {
        label: 'Actividad',
        data: datos,
      })
      .subscribe();
  }

  protected claseEvento(a: Actividad): string {
    switch (a.estado) {
      case 'CANCELADA':
        return 'ev-cancelada';
      case 'FINALIZADA':
      case 'CERRADA':
        return 'ev-finalizada';
      case 'EN_CURSO':
        return 'ev-curso';
      case 'PROGRAMADA':
        return a.fechaActividad.slice(0, 10) > this.hoyIso()
          ? 'ev-planificada'
          : 'ev-programada';
      default:
        return 'ev-neutra';
    }
  }

  private isoDe(d: Date): string {
    return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
  }
}
