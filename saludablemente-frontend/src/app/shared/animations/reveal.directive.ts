import {
  Directive,
  ElementRef,
  DestroyRef,
  afterNextRender,
  inject,
  input,
} from '@angular/core';

/**
 * Revela un elemento cuando entra al viewport (funciona en todos los navegadores).
 *
 * En vez de depender de `animation-timeline` (solo Chrome/Edge/Safari recientes),
 * usa IntersectionObserver para añadir la clase `.is-revealed`; el movimiento lo
 * pinta el CSS global `.app-reveal` (ver src/styles.css).
 *
 * Uso:
 *   <section [appReveal]>...</section>
 *   <a [appReveal]="i">...</a>        <!-- i: índice para escalonar -->
 *   <article appReveal [distancia]="'32px'" [unaVez]="false">...</article>
 */
@Directive({
  selector: '[appReveal]',
  standalone: true,
  host: { class: 'app-reveal' },
})
export class RevealDirective {
  /** Índice opcional para escalonar la entrada (se multiplica por 60ms). */
  readonly appReveal = input<number | string | null>(null);
  /** Distancia vertical inicial del desplazamiento (vacío = usa el CSS). */
  readonly distancia = input('');
  /** Escala inicial (pop-in estilo Netflix). 1 = sin zoom. */
  readonly escala = input(1);
  /** Si es true, solo anima la primera vez que entra. */
  readonly unaVez = input(true);
  /** Umbral de intersección (0-1). */
  readonly umbral = input(0.15);

  private readonly el = inject<ElementRef<HTMLElement>>(ElementRef);
  private readonly destroyRef = inject(DestroyRef);
  private observer?: IntersectionObserver;

  constructor() {
    afterNextRender(() => {
      const host = this.el.nativeElement;
      const dist = this.distancia();
      if (dist) host.style.setProperty('--reveal-distance', dist);
      if (this.escala() !== 1) {
        host.style.setProperty('--reveal-scale', `${this.escala()}`);
      }

      const raw = this.appReveal();
      const indice =
        typeof raw === 'number' ? raw : raw ? Number(raw) : Number.NaN;
      if (!Number.isNaN(indice)) {
        host.style.setProperty('--reveal-delay', `${indice * 60}ms`);
      }

      const reduceMotion =
        typeof matchMedia === 'function' &&
        matchMedia('(prefers-reduced-motion: reduce)').matches;

      if (reduceMotion || typeof IntersectionObserver === 'undefined') {
        host.classList.add('is-revealed');
        return;
      }

      this.observer = new IntersectionObserver(
        (entries) => {
          for (const entry of entries) {
            if (entry.isIntersecting) {
              host.classList.add('is-revealed');
              if (this.unaVez()) {
                this.observer?.disconnect();
              }
            } else if (!this.unaVez()) {
              host.classList.remove('is-revealed');
            }
          }
        },
        { threshold: this.umbral(), rootMargin: '0px 0px -8% 0px' },
      );
      this.observer.observe(host);

      this.destroyRef.onDestroy(() => this.observer?.disconnect());
    });
  }
}
