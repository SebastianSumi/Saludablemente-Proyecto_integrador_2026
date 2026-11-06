import { Component, computed, HostListener, input, signal } from '@angular/core';
import { TuiButton } from '@taiga-ui/core';
import { SocialIcon } from './social-icon';
import { SocialModal } from './social-modal';

@Component({
  selector: 'social-gallery',
  imports: [TuiButton, SocialIcon, SocialModal],
  styleUrl: './social-gallery.css',
  template: `
    @if (images().length) {
      <div
        class="photo-grid"
        [attr.data-count]="visible().length"
        [attr.data-orientation]="orientation()"
        aria-label="Imágenes de la publicación"
      >
        @for (url of visible(); track url; let i = $index) {
          <button
            class="photo-tile"
            type="button"
            (click)="open(i)"
            [attr.aria-label]="'Ver imagen ' + (i + 1) + ' de ' + images().length"
          >
            @if (!failed().has(url)) {
              <img
                [src]="url"
                [alt]="title() + ' · imagen ' + (i + 1)"
                loading="lazy"
                (load)="loaded(url, $event)"
                (error)="fail(url)"
              />
            } @else {
              <span class="photo-failed"><social-icon name="image-off" />Imagen no disponible</span>
            }
            @if (i === 4 && extra() > 0) {
              <span class="photo-extra" [attr.aria-label]="extra() + ' imágenes adicionales'"
                >+{{ extra() }}</span
              >
            }
          </button>
        }
      </div>
    }
    @if (selected() !== null && current(); as url) {
      <social-modal
        [title]="title() || 'Imágenes de la publicación'"
        [wide]="true"
        (closed)="close()"
      >
        <section class="photo-viewer" aria-label="Visor de imágenes">
          <div class="viewer-tools">
            <span role="status" aria-live="polite"
              >Imagen {{ index() + 1 }} de {{ images().length }}</span
            >
            <div class="zoom-tools">
              <button
                tuiButton
                appearance="flat"
                size="s"
                type="button"
                aria-label="Alejar imagen"
                [disabled]="zoom() <= 1"
                (click)="changeZoom(-0.5)"
              >
                <social-icon name="zoom-out" />
              </button>
              <button
                tuiButton
                appearance="flat"
                size="s"
                type="button"
                aria-label="Ajustar imagen"
                (click)="zoom.set(1)"
              >
                {{ zoom() * 100 }} %
              </button>
              <button
                tuiButton
                appearance="flat"
                size="s"
                type="button"
                aria-label="Acercar imagen"
                [disabled]="zoom() >= 3"
                (click)="changeZoom(0.5)"
              >
                <social-icon name="zoom-in" />
              </button>
              <a
                tuiButton
                appearance="flat"
                size="s"
                [href]="url"
                target="_blank"
                rel="noopener noreferrer"
                aria-label="Abrir imagen original"
                ><social-icon name="external-link"
              /></a>
            </div>
          </div>
          <div class="viewer-stage">
            <div
              class="image-canvas"
              (touchstart)="touchStart($event)"
              (touchend)="touchEnd($event)"
            >
              @if (!failed().has(url)) {
                <img
                  [src]="url"
                  [alt]="title() + ' · imagen ' + (index() + 1)"
                  [style.width.%]="zoom() * 100"
                  [style.height.%]="zoom() * 100"
                  (error)="fail(url)"
                />
              } @else {
                <span class="photo-failed"
                  ><social-icon name="image-off" />No se pudo cargar esta imagen.</span
                >
              }
            </div>
            @if (images().length > 1) {
              <button
                class="viewer-arrow previous"
                type="button"
                aria-label="Imagen anterior"
                (click)="move(-1)"
              >
                <social-icon name="chevron-left" />
              </button>
              <button
                class="viewer-arrow next"
                type="button"
                aria-label="Imagen siguiente"
                (click)="move(1)"
              >
                <social-icon name="chevron-right" />
              </button>
            }
          </div>
          @if (images().length > 1) {
            <nav class="photo-thumbs" aria-label="Todas las imágenes">
              @for (thumb of images(); track thumb; let i = $index) {
                <button
                  type="button"
                  [class.active]="index() === i"
                  [attr.aria-pressed]="index() === i"
                  [attr.aria-label]="'Ir a imagen ' + (i + 1)"
                  (click)="select(i)"
                >
                  <img [src]="thumb" alt="" loading="lazy" /><span>{{ i + 1 }}</span>
                </button>
              }
            </nav>
          }
        </section>
      </social-modal>
    }
  `,
})
export class SocialGallery {
  readonly urls = input<string[]>([]);
  readonly title = input('Imágenes de la publicación');
  protected readonly images = computed(() => [
    ...new Set(
      this.urls()
        .map((url) => url.trim())
        .filter(Boolean),
    ),
  ]);
  protected readonly visible = computed(() => this.images().slice(0, 5));
  protected readonly extra = computed(() => Math.max(0, this.images().length - 5));
  protected readonly sizes = signal<Record<string, { width: number; height: number }>>({});
  protected readonly failed = signal<Set<string>>(new Set());
  protected readonly selected = signal<number | null>(null);
  protected readonly index = computed(() =>
    Math.min(this.selected() ?? 0, Math.max(0, this.images().length - 1)),
  );
  protected readonly current = computed(() => this.images()[this.index()] ?? null);
  protected readonly zoom = signal(1);
  protected readonly orientation = computed(() => {
    const size = this.sizes()[this.images()[0]];
    return size && size.height > size.width * 1.15
      ? 'portrait'
      : size && size.width > size.height * 1.15
        ? 'landscape'
        : 'square';
  });
  private touchX: number | null = null;
  protected loaded(url: string, event: Event) {
    const image = event.target as HTMLImageElement;
    this.sizes.update((s) => ({
      ...s,
      [url]: { width: image.naturalWidth, height: image.naturalHeight },
    }));
  }
  protected fail(url: string) {
    this.failed.update((s) => new Set([...s, url]));
  }
  protected open(index: number) {
    this.select(index);
  }
  protected select(index: number) {
    this.selected.set(index);
    this.zoom.set(1);
  }
  protected close() {
    this.selected.set(null);
    this.zoom.set(1);
  }
  protected move(delta: number) {
    if (this.images().length)
      this.select((this.index() + delta + this.images().length) % this.images().length);
  }
  protected changeZoom(delta: number) {
    this.zoom.update((z) => Math.max(1, Math.min(3, z + delta)));
  }
  protected touchStart(e: TouchEvent) {
    this.touchX = e.touches[0]?.clientX ?? null;
  }
  protected touchEnd(e: TouchEvent) {
    const end = e.changedTouches[0]?.clientX;
    if (
      this.zoom() === 1 &&
      this.touchX !== null &&
      end !== undefined &&
      Math.abs(end - this.touchX) > 50
    )
      this.move(end < this.touchX ? 1 : -1);
    this.touchX = null;
  }
  @HostListener('document:keydown', ['$event'])
  protected navigate(e: KeyboardEvent) {
    if (this.selected() !== null && e.key === 'Escape') {
      e.preventDefault();
      this.close();
      return;
    }
    if (
      this.selected() === null ||
      (e.target instanceof HTMLElement &&
        e.target.matches('input,textarea,select,[contenteditable=true]'))
    )
      return;
    if (e.key === 'ArrowRight' || e.key === 'ArrowLeft') {
      e.preventDefault();
      this.move(e.key === 'ArrowRight' ? 1 : -1);
    }
  }
}
