import {
  Directive,
  ElementRef,
  DestroyRef,
  afterNextRender,
  inject,
} from '@angular/core';

/**
 * Barra de progreso de lectura ligada al scroll de la página.
 *
 * Escribe `--scroll-progress` (0 → 1) en el host con requestAnimationFrame
 * (passive, zoneless-safe). El CSS global `.scroll-progress` la pinta.
 *
 * Uso: <div class="scroll-progress" appScrollProgress></div>
 */
@Directive({
  selector: '[appScrollProgress]',
  standalone: true,
})
export class ScrollProgressDirective {
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

      const actualizar = (): void => {
        this.frame = 0;
        const doc = document.documentElement;
        const max = doc.scrollHeight - window.innerHeight;
        const progreso =
          max > 0 ? Math.min(1, Math.max(0, window.scrollY / max)) : 0;
        host.style.setProperty('--scroll-progress', progreso.toFixed(4));
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
