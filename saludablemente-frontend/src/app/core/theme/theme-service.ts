import { Injectable, effect, inject, signal } from '@angular/core';
import { TUI_DARK_MODE } from '@taiga-ui/core';

const STORAGE_KEY = 'saludablemente-theme';

@Injectable({ providedIn: 'root' })
export class ThemeService {
  private readonly darkMode = inject(TUI_DARK_MODE);
  protected readonly oscuro = signal<boolean>(this.inicial());

  /** Señal reactiva del tema para que los computed (ej. gráficos) se reconstruyan al alternar. */
  readonly enOscuro = this.oscuro.asReadonly();

  constructor() {
    this.aplicar(this.oscuro());
    effect(() => this.aplicar(this.oscuro()));
  }

  esOscuro(): boolean {
    return this.oscuro();
  }

  alternar(): void {
    this.oscuro.update((v) => !v);
    try {
      localStorage.setItem(STORAGE_KEY, this.oscuro() ? 'dark' : 'light');
    } catch {
      /* almacenamiento no disponible */
    }
  }

  private inicial(): boolean {
    try {
      const guardado = localStorage.getItem(STORAGE_KEY);
      if (guardado === 'dark') return true;
      if (guardado === 'light') return false;
    } catch {
      /* ignorar */
    }
    return (
      typeof window !== 'undefined' &&
      typeof window.matchMedia === 'function' &&
      window.matchMedia('(prefers-color-scheme: dark)').matches
    );
  }

  private aplicar(oscuro: boolean): void {
    this.darkMode.set(oscuro);
    if (typeof document !== 'undefined') {
      document.body.setAttribute('tuiTheme', oscuro ? 'dark' : 'light');
    }
  }
}
