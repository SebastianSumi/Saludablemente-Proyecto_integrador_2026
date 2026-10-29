import { Component, OnInit, inject, signal } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { ActivatedRoute, Router } from '@angular/router';
import { FormBuilder, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import {
  TuiButton,
  TuiCheckbox,
  TuiDropdown, TuiFilterByInputPipe,
  TuiError,
  TuiIcon,
  TuiInput,
  TuiLabel,
  TuiTextfield,
  tuiItemsHandlersProvider,
} from '@taiga-ui/core';
import { TuiButtonLoading, TuiChevron, TuiDataListWrapper, TuiComboBox, TuiTextarea } from '@taiga-ui/kit';
import { forkJoin, of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { NoticiaService } from '../noticia-service';
import { CategoriaNoticiaService } from '../categoria-service';
import { AreaService } from '../../catalogo/area/area-service';
import { TeamService } from '../../teams/team-service';
import { PerfilService } from '../../perfil/perfil-service';
import { ConfirmService } from '../../../../core/services/confirm-service';
import { Categoria } from '../models';
import { Area } from '../../catalogo/area/area.model';
import { Team } from '../../teams/team.model';

@Component({
  selector: 'app-noticia-form',
  imports: [
    FormsModule,
    ReactiveFormsModule,
    TuiButton,
    TuiButtonLoading,
    TuiCheckbox,
    TuiChevron,
    TuiDataListWrapper,
    TuiDropdown, TuiFilterByInputPipe,
    TuiError,
    TuiIcon,
    TuiInput,
    TuiLabel,
    TuiComboBox,
    TuiTextarea,
    TuiTextfield,
  ],
  templateUrl: './noticia-form.html',
  styleUrl: './noticia-form.css',
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
export class NoticiaForm implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly service = inject(NoticiaService);
  private readonly categoriasSrv = inject(CategoriaNoticiaService);
  private readonly areasSrv = inject(AreaService);
  private readonly equiposSrv = inject(TeamService);
  private readonly perfilSrv = inject(PerfilService);
  private readonly confirmar = inject(ConfirmService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  protected readonly id = signal<number | null>(null);
  protected readonly categorias = signal<Categoria[]>([]);
  protected readonly areas = signal<Area[]>([]);
  protected readonly equipos = signal<Team[]>([]);
  protected readonly teamsElegidos = signal<number[]>([]);
  protected readonly error = signal<string | null>(null);
  protected readonly loading = signal(false);
  protected readonly errorCarga = signal(false);
  protected readonly guardando = signal(false);
  protected idAutor = 0;

  protected readonly form = this.fb.nonNullable.group({
    titulo: ['', [Validators.required, Validators.maxLength(150)]],
    subtitulo: ['', [Validators.maxLength(200)]],
    contenido: ['', [Validators.required]],
    categoria: [null as Categoria | null, [Validators.required]],
    area: [null as Area | null],
  });

  ngOnInit(): void {
    this.perfilSrv.obtener().pipe(catchError(() => of(null))).subscribe((p) => {
      if (p) this.idAutor = p.idUsuario;
    });
    this.loading.set(true);
    forkJoin({
      cats: this.categoriasSrv.listarTodas().pipe(catchError(() => of([]))),
      areas: this.areasSrv.listar(true).pipe(catchError(() => of([]))),
      equipos: this.equiposSrv.listar(true).pipe(catchError(() => of([]))),
    }).subscribe(({ cats, areas, equipos }) => {
      this.categorias.set(cats);
      this.areas.set(areas);
      this.equipos.set(equipos);
      const idParam = this.route.snapshot.paramMap.get('id');
      if (!idParam) {
        this.loading.set(false);
        return;
      }
      const id = Number(idParam);
      this.id.set(id);
      this.service.obtener(id).subscribe({
        next: (n) => {
          this.form.patchValue({
            titulo: n.titulo,
            subtitulo: n.subtitulo ?? '',
            contenido: n.contenido,
            categoria: cats.find((c) => c.id === n.categoria?.id) ?? null,
            area: areas.find((a) => a.id === n.area?.id) ?? null,
          });
          this.teamsElegidos.set((n.teams ?? []).map((t) => t.id));
          this.loading.set(false);
        },
        error: () => {
          this.errorCarga.set(true);
          this.error.set('No se pudo cargar la noticia.');
          this.loading.set(false);
        },
      });
    });
  }

  protected toggleTeam(id: number, marcado: boolean): void {
    this.teamsElegidos.update((ids) =>
      marcado ? [...new Set([...ids, id])] : ids.filter((x) => x !== id)
    );
  }

  guardar(): void {
    if (this.loading() || this.errorCarga() || this.guardando()) return;
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      this.error.set('Completa título, contenido y categoría.');
      return;
    }
    if (!this.idAutor) {
      this.error.set('No se pudo identificar al autor. Recarga la página.');
      return;
    }
    const v = this.form.getRawValue();
    const payload = {
      titulo: (v.titulo ?? '').trim(),
      subtitulo: (v.subtitulo ?? '').trim() || undefined,
      contenido: v.contenido ?? '',
      idCategoria: (v.categoria as Categoria).id,
      idArea: (v.area as Area | null)?.id ?? undefined,
      idsTeams: this.teamsElegidos().length > 0 ? this.teamsElegidos() : undefined,
      idUsuarioAutor: this.idAutor,
    };
    const id = this.id();
    this.confirmar
      .confirmar(id ? '¿Guardar los cambios de la noticia?' : '¿Crear la noticia como borrador?', 'Guardar')
      .subscribe((ok) => {
        if (!ok) return;
        this.guardando.set(true);
        this.error.set(null);
        const peticion = id
          ? this.service.actualizar(id, payload)
          : this.service.crear(payload);
        peticion.subscribe({
          next: (n) => this.router.navigate(['/noticias/admin', n.id]),
          error: (err: HttpErrorResponse) => {
            if (err.status === 400) {
              this.error.set(err.error?.message ?? 'Datos inválidos.');
            } else {
              this.error.set('No se pudo guardar la noticia.');
            }
            this.guardando.set(false);
          },
        });
      });
  }

  cancelar(): void {
    const id = this.id();
    this.router.navigate(id ? ['/noticias/admin', id] : ['/noticias/admin']);
  }
}
