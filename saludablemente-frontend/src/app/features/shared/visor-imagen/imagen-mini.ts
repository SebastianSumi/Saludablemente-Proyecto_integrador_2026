import { Component, computed, inject, input, signal } from '@angular/core';
import { TuiIcon } from '@taiga-ui/core';
import { VisorImagenService } from './visor-service';
import type { VisorImagenItem } from './visor-model';

export type ImagenMiniForma = 'cuadrada' | 'ancha' | 'redonda';

/**
 * Miniatura clicable que abre el visor global (`VisorImagenService`).
 * Uso: cualquier módulo pasa `src` (o `galeria` + `indice`) y listo.
 * Si la imagen falla, muestra un respaldo con icono en vez de romperse.
 */
@Component({
  selector: 'app-imagen-mini',
  standalone: true,
  imports: [TuiIcon],
  templateUrl: './imagen-mini.html',
  styleUrl: './imagen-mini.css',
})
export class ImagenMini {
  private readonly visor = inject(VisorImagenService);

  /** URL ya resuelta de la miniatura (obligatoria). */
  readonly src = input<string | undefined>(undefined);
  readonly titulo = input<string | undefined>(undefined);
  readonly subtitulo = input<string | undefined>(undefined);
  /** Galería completa para navegar dentro del visor; por defecto solo esta imagen. */
  readonly galeria = input<VisorImagenItem[] | undefined>(undefined);
  readonly indice = input(0);
  readonly forma = input<ImagenMiniForma>('ancha');
  /** Alto fijo en px para la forma ancha; las demás son cuadradas. */
  readonly altoPx = input(180);
  readonly conLupa = input(true);

  protected readonly conError = signal(false);

  protected readonly lista = computed<VisorImagenItem[]>(() => {
    const g = (this.galeria() ?? []).filter((i) => !!i?.src);
    if (g.length > 0) return g;
    const src = this.src();
    if (!src) return [];
    return [{ src, titulo: this.titulo(), subtitulo: this.subtitulo() }];
  });

  protected abrir(): void {
    const lista = this.lista();
    if (lista.length === 0) return;
    const galeria = this.galeria();
    const i = galeria?.length ? Math.min(Math.max(this.indice(), 0), lista.length - 1) : 0;
    this.visor.abrir(lista, i);
  }

  protected alError(): void {
    this.conError.set(true);
  }
}
