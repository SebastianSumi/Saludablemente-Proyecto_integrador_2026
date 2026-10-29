import {
  Directive,
  ElementRef,
  DestroyRef,
  afterNextRender,
  inject,
} from '@angular/core';

/**
 * Animación ligada al scroll: escribe `--p` (0 → 1) en el host según cuánto
 * se ha desplazado el elemento hacia arriba respecto a su posición inicial
 * (0 = aún sin desplazar, 1 = se desplazó una altura de viewport). El CSS del
 * componente decide cómo usarlo (parallax, zoom, fade…).
 *
 * Recalcula con requestAnimationFrame (passive, zoneless-safe).
 *
 * Uso: <section class="inicio-hero" appScrollLinked>...</section>
 */
@Directive({
  selector: '[appScrollLinked]',
  standalone: true,
})
export class ScrollLinkedDirective {
  private readonly el = inject<ElementRef<HTMLElement>>(ElementRef);
  private readonly destroyRef = inject(DestroyRef);
  private frame = 0;

  constructor() {
    afterNextRender(() => {
      if (typeof window === 'undefined') return;

      const reduceMotion =
        typeof matchMedia === 'function' &&
        matchMedia('(prefers-reduced-motion: reduce)').matches;
      if (reduceMotion) return;

      const host = this.el.nativeElement;
      const topInicial = host.getBoundingClientRect().top;

      const actualizar = (): void => {
        this.frame = 0;
        const rect = host.getBoundingClientRect();
        const vh = window.innerHeight || 1;
        const recorrido = topInicial - rect.top;
        const progreso = Math.min(1, Math.max(0, recorrido / vh));
        host.style.setProperty('--p', progreso.toFixed(4));
      };

      const onScroll = (): void => {
        if (this.frame) return;
        this.frame = requestAnimationFrame(actualizar);
      };

      actualizar();
      window.addEventListener('scroll', onScroll, { passive: true });
      window.addEventListener('resize', onScroll, { passive: true });

      this.destroyRef.onDestroy(() => {
        window.removeEventListener('scroll', onScroll);
        window.removeEventListener('resize', onScroll);
        if (this.frame) cancelAnimationFrame(this.frame);
      });
    });
  }
}
