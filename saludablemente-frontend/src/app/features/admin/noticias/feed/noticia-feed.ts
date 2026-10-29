import { Component, OnInit, inject, signal } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { RouterLink } from '@angular/router';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { TuiButton, TuiDropdown, TuiFilterByInputPipe, TuiIcon, tuiItemsHandlersProvider } from '@taiga-ui/core';
import { TuiBadge, TuiChevron, TuiDataListWrapper, TuiComboBox } from '@taiga-ui/kit';
import { NoticiaPublicaService } from '../noticia-service';
import { CategoriaNoticiaService } from '../categoria-service';
import { ImagenMini } from '../../../shared/visor-imagen/imagen-mini';
import { Categoria, NoticiaFeed, urlImagenNoticia } from '../models';
import { forkJoin, of } from 'rxjs';
import { catchError } from 'rxjs/operators';

@Component({
  selector: 'app-noticia-feed',
  imports: [
    RouterLink,
    ReactiveFormsModule,
    TuiBadge,
    TuiButton,
    TuiChevron,
    TuiDataListWrapper,
    TuiDropdown, TuiFilterByInputPipe,
    TuiIcon,
    TuiComboBox,
    ImagenMini,
  ],
  templateUrl: './noticia-feed.html',
  styleUrl: './noticia-feed.css',
  providers: [
    tuiItemsHandlersProvider({
      stringify: signal(
        (item: unknown): string => {
          const o = item as { nombre?: string } | null;
          return o?.nombre ?? (typeof item === 'string' ? (item as string) : '');
        }
      ),
      identityMatcher: signal(
        (a: unknown, b: unknown): boolean =>
          ((a as { id?: unknown } | null)?.id ?? a ?? null) ===
          ((b as { id?: unknown } | null)?.id ?? b ?? null)
      ),
    }),
  ],
})
export class NoticiaFeedComponent implements OnInit {
  private readonly feedSrv = inject(NoticiaPublicaService);
  private readonly categoriasSrv = inject(CategoriaNoticiaService);

  protected readonly items = signal<NoticiaFeed[]>([]);
  protected readonly categorias = signal<Categoria[]>([]);
  protected readonly totalPages = signal(1);
  protected readonly page = signal(0);
  protected readonly loading = signal(false);
  protected readonly error = signal<string | null>(null);

  protected readonly categoriaCtrl = new FormControl<Categoria | null>(null);

  ngOnInit(): void {
    this.categoriasSrv.listarActivas().pipe(catchError(() => of([]))).subscribe((c) => {
      this.categorias.set(c);
    });
    this.categoriaCtrl.valueChanges.subscribe(() => {
      this.page.set(0);
      this.cargar();
    });
    this.cargar();
  }

  cargar(): void {
    this.loading.set(true);
    this.error.set(null);
    this.feedSrv
      .feed({ idCategoria: this.categoriaCtrl.value?.id, page: this.page(), size: 20 })
      .subscribe({
        next: (p) => {
          this.items.set(p.content);
          this.totalPages.set(Math.max(1, p.totalPages));
          this.loading.set(false);
        },
        error: (err: HttpErrorResponse) => {
          this.error.set(
            err.status === 0 ? 'No se pudo conectar con el servidor.' : 'No se pudo cargar el feed.'
          );
          this.loading.set(false);
        },
      });
  }

  paginaAnterior(): void {
    if (this.page() > 0) {
      this.page.update((v) => v - 1);
      this.cargar();
    }
  }

  paginaSiguiente(): void {
    if (this.page() + 1 < this.totalPages()) {
      this.page.update((v) => v + 1);
      this.cargar();
    }
  }

  protected colorCategoria(c: NoticiaFeed): string {
    return c.colorCategoria ?? 'var(--sal-accent)';
  }

  protected imagenDe(n: NoticiaFeed): string | undefined {
    return urlImagenNoticia(n.imagenUrl);
  }

  protected galeria(): { src: string; titulo: string; subtitulo?: string }[] {
    return this.items()
      .map((it) => ({
        src: this.imagenDe(it),
        titulo: it.titulo,
        subtitulo: it.subtitulo,
      }))
      .filter((it) => !!it.src) as { src: string; titulo: string; subtitulo?: string }[];
  }

  protected indiceDe(n: NoticiaFeed): number {
    return Math.max(
      0,
      this.galeria().findIndex((g) => g.src === this.imagenDe(n)),
    );
  }
}
