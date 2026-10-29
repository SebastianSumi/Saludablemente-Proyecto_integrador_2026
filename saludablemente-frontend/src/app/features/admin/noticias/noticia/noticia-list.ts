import { Component, OnInit, inject, signal } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { RouterLink } from '@angular/router';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { TuiButton, TuiDropdown, TuiFilterByInputPipe, TuiIcon, tuiItemsHandlersProvider } from '@taiga-ui/core';
import { TuiBadge, TuiChevron, TuiDataListWrapper, TuiComboBox } from '@taiga-ui/kit';
import { TuiTable } from '@taiga-ui/addon-table';
import { NoticiaService } from '../noticia-service';
import { CategoriaNoticiaService } from '../categoria-service';
import { Categoria, EstadoNoticia, Noticia } from '../models';
import { forkJoin, of } from 'rxjs';
import { catchError } from 'rxjs/operators';

interface Opcion {
  id: string;
  label: string;
}

@Component({
  selector: 'app-noticia-list',
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
    TuiTable,
  ],
  templateUrl: './noticia-list.html',
  styleUrl: './noticia-list.css',
  providers: [
    tuiItemsHandlersProvider({
      stringify: signal(
        (item: unknown): string => {
          const o = item as { nombre?: string; label?: string } | null;
          return o?.nombre ?? o?.label ?? (typeof item === 'string' ? (item as string) : '');
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
export class NoticiaList implements OnInit {
  private readonly service = inject(NoticiaService);
  private readonly categoriasSrv = inject(CategoriaNoticiaService);

  protected readonly items = signal<Noticia[]>([]);
  protected readonly categorias = signal<Categoria[]>([]);
  protected readonly total = signal(0);
  protected readonly totalPages = signal(1);
  protected readonly page = signal(0);
  protected readonly loading = signal(false);
  protected readonly error = signal<string | null>(null);

  protected readonly estadoOpts: Opcion[] = [
    { id: '', label: 'Todos' },
    { id: 'BORRADOR', label: 'Borrador' },
    { id: 'PUBLICADO', label: 'Publicado' },
    { id: 'ARCHIVADO', label: 'Archivado' },
  ];

  protected readonly estadoCtrl = new FormControl<Opcion | null>(null);
  protected readonly categoriaCtrl = new FormControl<Categoria | null>(null);

  ngOnInit(): void {
    this.categoriasSrv.listarTodas().pipe(catchError(() => of([]))).subscribe((c) => {
      this.categorias.set(c);
    });
    this.estadoCtrl.valueChanges.subscribe(() => {
      this.page.set(0);
      this.cargar();
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
    this.service
      .listar({
        estado: (this.estadoCtrl.value?.id || undefined) as EstadoNoticia | undefined,
        idCategoria: this.categoriaCtrl.value?.id,
        page: this.page(),
        size: 20,
      })
      .subscribe({
        next: (p) => {
          this.items.set(p.content);
          this.total.set(p.totalElements);
          this.totalPages.set(Math.max(1, p.totalPages));
          this.loading.set(false);
        },
        error: (err: HttpErrorResponse) => {
          this.error.set(
            err.status === 0 ? 'No se pudo conectar con el servidor.' : 'No se pudo cargar las noticias.'
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

  protected aparienciaEstado(e?: string): string {
    switch (e) {
      case 'PUBLICADO':
        return 'positive';
      case 'ARCHIVADO':
        return 'neutral';
      default:
        return 'warning';
    }
  }
}
