import {
  Directive,
  ElementRef,
  DestroyRef,
  afterNextRender,
  inject,
  input,
} from '@angular/core';

const SELECTOR_POR_DEFECTO =
  '.page-banner img, .card-image, .banner-imagen, .regla-banner img';

/**
 * Parallax automático para los banners/imágenes de una región.
 *
 * Escanea la región, marca los objetivos con `.app-parallax` y actualiza su
 * `--parallax-y` en un único listener de scroll (passive, rAF, zoneless-safe).
 * Re-escanea ante cambios de DOM (navegación).
 *
 * Uso: <main class="content" appAutoParallax>...</main>
 */
@Directive({
  selector: '[appAutoParallax]',
  standalone: true,
})
export class AutoParallaxDirective {
  /** Selector de los elementos con parallax (vacío = valor por defecto). */
  readonly appAutoParallax = input('');
  /** Factor de desplazamiento (fracción de la altura del viewport). */
  readonly factor = input(0.1);

  private readonly el = inject<ElementRef<HTMLElement>>(ElementRef);
  private readonly destroyRef = inject(DestroyRef);
  private mutation?: MutationObserver;
  private readonly objetivos = new Set<HTMLElement>();
  private frame = 0;

  constructor() {
    afterNextRender(() => {
      if (typeof window === 'undefined') return;

      const reduce =
        typeof matchMedia === 'function' &&
        matchMedia('(prefers-reduced-motion: reduce)').matches;
      if (reduce) return;

      const raiz = this.el.nativeElement;
      const selector = this.appAutoParallax() || SELECTOR_POR_DEFECTO;

      const actualizar = (): void => {
        this.frame = 0;
        const vh = window.innerHeight || 1;
        this.objetivos.forEach((el) => {
          const rect = el.getBoundingClientRect();
          const centro = rect.top + rect.height / 2;
          const progreso = (centro - vh / 2) / vh;
          const dy = -progreso * this.factor() * vh;
          el.style.setProperty('--parallax-y', `${dy.toFixed(2)}px`);
        });
      };

      const onScroll = (): void => {
        if (this.frame) return;
        this.frame = requestAnimationFrame(actualizar);
      };

      const escanear = (): void => {
        raiz.querySelectorAll<HTMLElement>(selector).forEach((el) => {
          if (el.classList.contains('app-parallax')) return;
          if (el.parentElement?.closest(selector)) return;
          el.classList.add('app-parallax');
          this.objetivos.add(el);
        });
        actualizar();
      };

      let programado = 0;
      const programar = (): void => {
        if (programado) return;
        programado = requestAnimationFrame(() => {
          programado = 0;
          escanear();
        });
      };

      escanear();
      window.addEventListener('scroll', onScroll, { passive: true });
      window.addEventListener('resize', onScroll, { passive: true });

      if (typeof MutationObserver !== 'undefined') {
        this.mutation = new MutationObserver(programar);
        this.mutation.observe(raiz, { childList: true, subtree: true });
      }

      this.destroyRef.onDestroy(() => {
        window.removeEventListener('scroll', onScroll);
        window.removeEventListener('resize', onScroll);
        this.mutation?.disconnect();
        if (this.frame) cancelAnimationFrame(this.frame);
        if (programado) cancelAnimationFrame(programado);
      });
    });
  }
}
