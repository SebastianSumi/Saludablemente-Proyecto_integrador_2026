import {
  Directive,
  ElementRef,
  DestroyRef,
  afterNextRender,
  inject,
  input,
} from '@angular/core';

/**
 * Parallax ligado al scroll, independiente del navegador.
 *
 * Escucha `scroll`/`resize` con requestAnimationFrame (passive) y escribe la
 * variable CSS `--parallax-y` en el host; el desplazamiento lo aplica el CSS
 * global `.app-parallax` (ver src/styles.css). No usa signals por frame, por lo
 * que es seguro en una app zoneless.
 *
 * Uso:
 *   <span class="hero-orb" [appParallax]="0.12"></span>
 */
@Directive({
  selector: '[appParallax]',
  standalone: true,
  host: { class: 'app-parallax' },
})
export class ParallaxDirective {
  /** Factor de desplazamiento (fracción de la altura del viewport). */
  readonly appParallax = input(0.15);

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
        const rect = host.getBoundingClientRect();
        const vh = window.innerHeight || 1;
        const centro = rect.top + rect.height / 2;
        const progreso = (centro - vh / 2) / vh;
        const desplazamiento = -progreso * this.appParallax() * vh;
        host.style.setProperty(
          '--parallax-y',
          `${desplazamiento.toFixed(2)}px`,
        );
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
