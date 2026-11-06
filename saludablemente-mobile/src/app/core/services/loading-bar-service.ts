import { Injectable, inject, signal } from '@angular/core';
import {
  NavigationCancel,
  NavigationEnd,
  NavigationError,
  NavigationStart,
  Router,
} from '@angular/router';

/** Intervalo de avance simulado y techo antes de completar (NProgress-like). */
const PASO_MS = 220;
const TECHO = 0.9;
/** Tiempo que la barra permanece en 100% antes de desvanecerse. */
const CIERRE_MS = 240;

/**
 * Barra de carga global (zoneless-safe).
 *
 * Suma cargas concurrentes de navegación de ruta y de peticiones HTTP; la barra
 * sube sola hasta el 90% y salta a 100% cuando no queda ninguna carga activa.
 * El progreso se actualiza por intervalo (~220ms), no por frame, para no
 * disparar change detection en cada tick.
 */
@Injectable({ providedIn: 'root' })
export class LoadingBarService {
  private readonly router = inject(Router);

  private pendientes = 0;
  private activo = false;
  private timer: ReturnType<typeof setInterval> | null = null;
  private cierre: ReturnType<typeof setTimeout> | null = null;

  readonly visible = signal(false);
  readonly progreso = signal(0);

  constructor() {
    if (typeof window === 'undefined') return;

    this.router.events.subscribe((e) => {
      if (e instanceof NavigationStart) {
        this.iniciar();
      } else if (
        e instanceof NavigationEnd ||
        e instanceof NavigationCancel ||
        e instanceof NavigationError
      ) {
        this.terminar();
      }
    });
  }

  /** Registra una carga nueva (ruta o petición HTTP). */
  iniciar(): void {
    this.pendientes++;
    if (this.activo) return;

    this.activo = true;
    this.limpiarCierre();
    this.visible.set(true);
    this.progreso.set(0.08);

    this.timer = setInterval(() => {
      const p = this.progreso();
      if (p >= TECHO) return;
      const avance = (TECHO - p) * 0.09 + 0.01;
      this.progreso.set(Math.min(TECHO, p + avance));
    }, PASO_MS);
  }

  /** Cierra una carga registrada con {@link iniciar}. */
  terminar(): void {
    this.pendientes = Math.max(0, this.pendientes - 1);
    if (this.pendientes === 0) this.completar();
  }

  private completar(): void {
    this.detenerTimer();
    this.activo = false;
    this.progreso.set(1);

    this.limpiarCierre();
    this.cierre = setTimeout(() => {
      this.visible.set(false);
      this.cierre = setTimeout(() => this.progreso.set(0), PASO_MS);
    }, CIERRE_MS);
  }

  private detenerTimer(): void {
    if (this.timer !== null) {
      clearInterval(this.timer);
      this.timer = null;
    }
  }

  private limpiarCierre(): void {
    if (this.cierre !== null) {
      clearTimeout(this.cierre);
      this.cierre = null;
    }
  }
}
