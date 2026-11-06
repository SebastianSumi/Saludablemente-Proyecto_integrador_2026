import { Component, ElementRef, HostListener, OnDestroy, ViewChild, computed, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { TuiButton, TuiIcon } from '@taiga-ui/core';
import { TuiBadge } from '@taiga-ui/kit';
import { injectContext } from '@taiga-ui/polymorpheus';
import { TuiDialogContext } from '@taiga-ui/core/portals/dialog';
import { VisorImagenDatos } from './visor-model';

const ZOOM_MIN = 1;
const ZOOM_MAX = 4;

@Component({
  selector: 'app-visor-imagen',
  imports: [FormsModule, TuiBadge, TuiButton, TuiIcon],
  templateUrl: './visor-imagen.html',
  styleUrl: './visor-imagen.css',
})
export class VisorImagen implements OnDestroy {
  private readonly ctx =
    injectContext<TuiDialogContext<void, VisorImagenDatos>>();

  @ViewChild('lienzo') private lienzoRef?: ElementRef<HTMLElement>;

  protected readonly imagenes = signal(this.ctx.data.imagenes ?? []);
  protected readonly indice = signal(
    Math.min(
      Math.max(this.ctx.data.indice ?? 0, 0),
      Math.max(this.imagenes().length - 1, 0),
    ),
  );
  protected readonly zoom = signal(1);
  protected readonly rotacion = signal(0);
  protected readonly offset = signal({ x: 0, y: 0 });
  protected readonly cargando = signal(true);
  protected readonly conError = signal(false);
  protected readonly descargando = signal(false);
  protected readonly reproduciendo = signal(false);
  protected readonly pantallaCompleta = signal(false);

  private arrastrando = false;
  private inicio = { x: 0, y: 0 };
  private base = { x: 0, y: 0 };
  private deslizArranque: { x: number; y: number } | null = null;
  private pellizco: { dist: number; zoomBase: number } | null = null;
  private readonly punteros = new Map<number, { x: number; y: number }>();
  private timerSlide: ReturnType<typeof setInterval> | null = null;

  protected readonly actual = computed(() => this.imagenes()[this.indice()]);
  protected readonly total = computed(() => this.imagenes().length);
  protected readonly zoomPct = computed(() => `${Math.round(this.zoom() * 100)}%`);
  /** Pan+zoom en el marco; rotación en la img para no romper el encaje. */
  protected readonly marcoTransform = computed(() => {
    const o = this.offset();
    return `translate(${o.x}px, ${o.y}px) scale(${this.zoom()})`;
  });
  protected readonly fotoTransform = computed(
    () => `rotate(${this.rotacion()}deg)`,
  );

  protected irAl(i: number): void {
    if (this.total() < 1) return;
    this.indice.set(Math.min(Math.max(i, 0), this.total() - 1));
    this.restablecerVista();
  }

  protected anterior(): void {
    this.pausarSlide();
    this.irAl((this.indice() - 1 + this.total()) % this.total());
  }

  protected siguiente(): void {
    this.pausarSlide();
    this.irAl((this.indice() + 1) % this.total());
  }

  protected elegir(i: number): void {
    this.pausarSlide();
    this.irAl(i);
  }

  protected avanzarSlide(): void {
    this.irAl((this.indice() + 1) % this.total());
  }

  private limitar(v: { x: number; y: number }): { x: number; y: number } {
    const margen = 120 + 260 * (this.zoom() - 1);
    return {
      x: Math.max(-margen, Math.min(margen, v.x)),
      y: Math.max(-margen, Math.min(margen, v.y)),
    };
  }

  protected fijarZoom(z: number): void {
    const n = Math.min(ZOOM_MAX, Math.max(ZOOM_MIN, +Number(z).toFixed(2)));
    this.zoom.set(n);
    this.offset.update((o) => (n <= 1 ? { x: 0, y: 0 } : this.limitar(o)));
  }

  protected zoomIn(): void {
    this.fijarZoom(this.zoom() + 0.25);
  }

  protected zoomOut(): void {
    this.fijarZoom(this.zoom() - 0.25);
  }

  protected restablecer(): void {
    this.zoom.set(1);
    this.rotacion.set(0);
    this.offset.set({ x: 0, y: 0 });
  }

  private restablecerVista(): void {
    this.restablecer();
    this.cargando.set(true);
    this.conError.set(false);
  }

  protected rotar(): void {
    this.rotacion.update((r) => (r + 90) % 360);
  }

  protected alCargar(): void {
    this.cargando.set(false);
    this.conError.set(false);
  }

  protected alError(): void {
    this.cargando.set(false);
    this.conError.set(true);
  }

  protected rueda(e: WheelEvent): void {
    e.preventDefault();
    if (e.deltaY < 0) this.zoomIn();
    else if (e.deltaY > 0) this.zoomOut();
  }

  protected dobleClick(): void {
    this.zoom() > 1 ? this.fijarZoom(1) : this.fijarZoom(2);
  }

  /** Pan con 1 dedo cuando hay zoom; swipe para navegar sin zoom; pellizco con 2 dedos. */
  protected inicioPan(e: PointerEvent): void {
    if ((e.target as HTMLElement | null)?.closest?.('button')) return;
    this.punteros.set(e.pointerId, { x: e.clientX, y: e.clientY });
    if (this.punteros.size === 2) {
      const [a, b] = [...this.punteros.values()];
      this.pellizco = {
        dist: Math.hypot(a.x - b.x, a.y - b.y) || 1,
        zoomBase: this.zoom(),
      };
      this.arrastrando = false;
      this.deslizArranque = null;
      return;
    }
    if (this.zoom() > 1) {
      this.arrastrando = true;
      this.inicio = { x: e.clientX, y: e.clientY };
      this.base = { ...this.offset() };
    } else {
      this.deslizArranque = { x: e.clientX, y: e.clientY };
    }
    (e.currentTarget as HTMLElement)?.setPointerCapture?.(e.pointerId);
  }

  protected moverPan(e: PointerEvent): void {
    if (!this.punteros.has(e.pointerId)) return;
    this.punteros.set(e.pointerId, { x: e.clientX, y: e.clientY });
    if (this.punteros.size === 2 && this.pellizco) {
      const [a, b] = [...this.punteros.values()];
      const dist = Math.hypot(a.x - b.x, a.y - b.y) || 1;
      this.fijarZoom(this.pellizco.zoomBase * (dist / this.pellizco.dist));
      return;
    }
    if (!this.arrastrando) return;
    this.offset.set(
      this.limitar({
        x: this.base.x + (e.clientX - this.inicio.x),
        y: this.base.y + (e.clientY - this.inicio.y),
      }),
    );
  }

  protected finPan(e?: PointerEvent): void {
    if (e) this.punteros.delete(e.pointerId);
    else this.punteros.clear();
    if (this.punteros.size < 2) this.pellizco = null;
    // Swipe horizontal sin zoom = anterior/siguiente (táctil y escritorio)
    if (this.deslizArranque && e && this.zoom() <= 1 && this.total() > 1) {
      const dx = e.clientX - this.deslizArranque.x;
      const dy = e.clientY - this.deslizArranque.y;
      if (Math.abs(dx) > 70 && Math.abs(dx) > Math.abs(dy) * 1.5) {
        if (dx < 0) this.siguiente();
        else this.anterior();
      }
    }
    this.deslizArranque = null;
    this.arrastrando = false;
  }

  @HostListener('window:keydown', ['$event'])
  protected tecla(e: KeyboardEvent): void {
    if (e.key === 'ArrowRight') this.siguiente();
    else if (e.key === 'ArrowLeft') this.anterior();
    else if (e.key === '+' || e.key === '=') this.zoomIn();
    else if (e.key === '-') this.zoomOut();
    else if (e.key === '0') this.restablecer();
    else if (e.key === 'r' || e.key === 'R') this.rotar();
    else if (e.key === 'f' || e.key === 'F') this.alternarPantalla();
    else if (e.key === ' ') {
      e.preventDefault();
      this.alternarSlide();
    }
  }

  protected alternarSlide(): void {
    if (this.reproduciendo()) {
      this.pausarSlide();
      return;
    }
    if (this.total() < 2) return;
    this.reproduciendo.set(true);
    this.timerSlide = setInterval(() => this.avanzarSlide(), 2500);
  }

  protected pausarSlide(): void {
    this.reproduciendo.set(false);
    if (this.timerSlide) {
      clearInterval(this.timerSlide);
      this.timerSlide = null;
    }
  }

  protected async alternarPantalla(): Promise<void> {
    const el = this.lienzoRef?.nativeElement;
    try {
      if (document.fullscreenElement) {
        await document.exitFullscreen();
      } else if (el?.requestFullscreen) {
        await el.requestFullscreen();
      }
    } catch {
      this.abrirOriginal();
    }
  }

  @HostListener('document:fullscreenchange')
  protected alCambiarPantalla(): void {
    this.pantallaCompleta.set(!!document.fullscreenElement);
  }

  protected async descargar(): Promise<void> {
    const img = this.actual();
    if (!img || this.descargando()) return;
    this.descargando.set(true);
    try {
      const res = await fetch(img.src, { mode: 'cors' });
      if (!res.ok) throw new Error('fetch');
      const blob = await res.blob();
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download =
        (img.titulo ?? 'imagen').replace(/[^\w\-áéíóúñ]+/gi, '_') + '.jpg';
      document.body.appendChild(a);
      a.click();
      a.remove();
      setTimeout(() => URL.revokeObjectURL(url), 4000);
    } catch {
      window.open(img.src, '_blank', 'noopener');
    } finally {
      this.descargando.set(false);
    }
  }

  protected abrirOriginal(): void {
    const img = this.actual();
    if (img) window.open(img.src, '_blank', 'noopener');
  }

  protected cerrar(): void {
    this.pausarSlide();
    this.ctx.completeWith();
  }

  ngOnDestroy(): void {
    this.pausarSlide();
  }
}
