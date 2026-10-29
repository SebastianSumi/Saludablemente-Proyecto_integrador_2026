import {
  Directive,
  ElementRef,
  DestroyRef,
  afterNextRender,
  inject,
  input,
} from '@angular/core';

const SELECTOR_POR_DEFECTO = '.page-banner, .card';

/**
 * Reveal automático para toda una región (p. ej. el `main.content` del shell).
 *
 * En vez de poner `[appReveal]` en cada plantilla, escanea la región y aplica
 * `.app-reveal` / `.is-revealed` a los bloques objetivo con un único
 * IntersectionObserver, re-escaneando ante cambios de DOM (navegación, datos
 * cargados) con un MutationObserver (de-bounced por frame).
 *
 * Reglas:
 *  - objetivo por defecto: `.page-banner, .card` (configurable con el input);
 *  - ignora anidados (un `.card` dentro de otro) y elementos ya animados
 *    (`.app-reveal` manual, tarjetas de módulo con `modulo-entrar`);
 *  - respeta `[data-no-reveal]` y `prefers-reduced-motion`;
 *  - zoneless-safe: no escribe signals, solo clases CSS.
 *
 * Uso: <main class="content" appAutoReveal>...</main>
 */
@Directive({
  selector: '[appAutoReveal]',
  standalone: true,
})
export class AutoRevealDirective {
  /** Selector de los bloques a revelar (vacío = valor por defecto). */
  readonly appAutoReveal = input('');

  private readonly el = inject<ElementRef<HTMLElement>>(ElementRef);
  private readonly destroyRef = inject(DestroyRef);
  private observer?: IntersectionObserver;
  private mutation?: MutationObserver;
  private readonly vistos = new WeakSet<Element>();

  constructor() {
    afterNextRender(() => {
      if (typeof window === 'undefined') return;
      if (typeof IntersectionObserver === 'undefined') return;

      const reduce =
        typeof matchMedia === 'function' &&
        matchMedia('(prefers-reduced-motion: reduce)').matches;
      if (reduce) return;

      const raiz = this.el.nativeElement;
      const selector = this.appAutoReveal() || SELECTOR_POR_DEFECTO;

      this.observer = new IntersectionObserver(
        (entries) => {
          for (const entry of entries) {
            if (entry.isIntersecting) {
              (entry.target as HTMLElement).classList.add('is-revealed');
              this.observer?.unobserve(entry.target);
            }
          }
        },
        { threshold: 0.12, rootMargin: '0px 0px -8% 0px' },
      );

      const escanear = (): void => {
        const candidatos = raiz.querySelectorAll<HTMLElement>(selector);
        candidatos.forEach((el) => {
          if (this.vistos.has(el)) return;
          if (el.hasAttribute('data-no-reveal')) return;
          // Estas tarjetas ya tienen su propia animación de entrada
          // (`modulo-entrar`): no deben recibir además `.app-reveal` (opacity 0).
          if (
            el.classList.contains('modulo-card') ||
            el.classList.contains('modulo-hero') ||
            el.classList.contains('regla-card') ||
            el.classList.contains('hija') ||
            el.classList.contains('hija-grande')
          ) {
            return;
          }
          // Ya animado a mano o ya anidado dentro de otro objetivo: no tocar.
          if (
            el.classList.contains('app-reveal') ||
            el.parentElement?.closest(selector)
          ) {
            this.vistos.add(el);
            return;
          }
          this.vistos.add(el);

          const padre = el.parentElement;
          const hermanos = padre
            ? Array.from(padre.children).filter((c) => c.matches(selector))
            : [el];
          const pos = Math.min(Math.max(0, hermanos.indexOf(el)), 8);
          if (pos > 0) el.style.setProperty('--reveal-delay', `${pos * 60}ms`);

          el.classList.add('app-reveal');
          this.observer?.observe(el);
        });
      };

      // Microtask (no rAF): corre antes del paint, así el contenido recién
      // insertado (p. ej. tarjetas al cargar datos) no parpadea antes de animar.
      let programado = false;
      const programar = (): void => {
        if (programado) return;
        programado = true;
        queueMicrotask(() => {
          programado = false;
          escanear();
        });
      };

      escanear();

      if (typeof MutationObserver !== 'undefined') {
        this.mutation = new MutationObserver(programar);
        this.mutation.observe(raiz, { childList: true, subtree: true });
      }

      this.destroyRef.onDestroy(() => {
        this.observer?.disconnect();
        this.mutation?.disconnect();
      });
    });
  }
}
