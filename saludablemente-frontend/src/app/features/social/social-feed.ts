import { Component, DestroyRef, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { TuiButton } from '@taiga-ui/core';
import {
  BehaviorSubject,
  catchError,
  combineLatest,
  debounceTime,
  forkJoin,
  map,
  of,
  Subject,
  switchMap,
} from 'rxjs';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { SocialService } from './social-service';
import { EquipoSocial, Muro, Scope, Sugerencia, Tendencia, TipoMencion } from './social.model';
import { errorSocial, rutaMencion } from './social.utils';
import { SocialPostCard } from './social-post-card';
import { SocialPostEditor } from './social-post-editor';
import { SocialPager } from './social-pager';
import { SocialTeam } from './social-team';
@Component({
  selector: 'app-social-feed',
  imports: [
    FormsModule,
    RouterLink,
    TuiButton,
    SocialPostCard,
    SocialPostEditor,
    SocialPager,
    SocialTeam,
  ],
  styleUrl: './social.css',
  template: ` <div class="columns">
    <div class="stack">
      @if (team(); as t) {
        <social-team [equipo]="t" />
      }
      <div class="actions">
        <h2>{{ heading() }}</h2>
        <button tuiButton size="s" type="button" (click)="composer.set(!composer())">
          {{ composer() ? 'Cerrar editor' : 'Nueva publicación' }}
        </button>
        @if (scope() !== 'GLOBAL' || tag()) {
          <a routerLink="/social">Volver al muro global</a>
        }
      </div>
      @if (composer()) {
        <social-post-editor
          (cancel)="composer.set(false)"
          (saved)="composer.set(false); refresh()"
        />
      }
      @if (scope() === 'GLOBAL' && !tag()) {
        <nav class="tabs" aria-label="Filtrar publicaciones">
          @for (t of tabs; track t.value) {
            <button
              type="button"
              [class.active]="tab() === t.value"
              [attr.aria-pressed]="tab() === t.value"
              (click)="selectTab(t.value)"
            >
              {{ t.label }}
            </button>
          }
        </nav>
      }
      @if (error()) {
        <div class="error" role="alert">
          {{ error() }}
          <button tuiButton type="button" appearance="flat" size="s" (click)="refresh()">
            Reintentar
          </button>
        </div>
      }
      @if (loading()) {
        <p class="loading" role="status">Cargando publicaciones…</p>
      } @else if (result(); as r) {
        @for (post of r.pinned; track post.idPost) {
          <social-post-card
            [post]="post"
            [pinned]="true"
            [scope]="scope()"
            [scopeId]="scopeId()"
            (changed)="refresh()"
            (deleted)="refresh()"
          />
        }
        @for (post of r.posts.content; track post.idPost) {
          <social-post-card
            [post]="post"
            [scope]="scope()"
            [scopeId]="scopeId()"
            (changed)="refresh()"
            (deleted)="refresh()"
          />
        }
        @if (!r.posts.content.length && !r.pinned.length) {
          <div class="empty">
            <h3>Este espacio está por comenzar</h3>
            <p>
              No hay publicaciones para este filtro. Comparte una experiencia o explora el muro
              global.
            </p>
          </div>
        }
        <social-pager
          [page]="r.posts.number"
          [total]="r.posts.totalPages"
          (change)="page($event)"
        />
      }
    </div>
    <aside class="stack sidebar">
      <section class="panel fields">
        <h3>Explora la comunidad</h3>
        <label
          >Encontrar<select
            [ngModel]="exploreType"
            (ngModelChange)="exploreType = $event; find(exploreQuery)"
          >
            <option value="EQUIPO">Equipos</option>
            <option value="ACTIVIDAD">Actividades</option>
          </select></label
        ><label
          >Nombre<input
            [ngModel]="exploreQuery"
            (ngModelChange)="find($event)"
            placeholder="Buscar…"
        /></label>
        @if (exploreError()) {
          <small role="alert">No se pudo realizar la búsqueda.</small>
        }
        @for (item of suggestions(); track item.id) {
          <a [routerLink]="ruta(item.tipo, item.id)">{{ item.nombre }}</a>
        }
      </section>
      <section class="panel">
        <h3>Conversaciones de la semana</h3>
        @if (trendError()) {
          <p role="alert">No se pudieron cargar las tendencias.</p>
          <button tuiButton type="button" size="s" appearance="flat" (click)="loadTrends()">
            Reintentar
          </button>
        } @else {
          @for (t of trends(); track t.tag) {
            <p>
              <a [routerLink]="['/social/hashtags', t.tag]">#{{ t.tag }}</a>
              <small>· {{ t.total }}</small>
            </p>
          } @empty {
            <p class="muted">Los hashtags de la comunidad aparecerán aquí.</p>
          }
        }
      </section>
      <section class="notice">
        <strong>Un espacio de bienestar</strong>
        <p>
          Comparte con respeto. Usa las menciones para invitar a conversar y los reportes para
          cuidar nuestra comunidad.
        </p>
      </section>
    </aside>
  </div>`,
})
export class SocialFeed {
  private readonly api = inject(SocialService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly destroy = inject(DestroyRef);
  private readonly reload = new BehaviorSubject(0);
  private readonly explore = new Subject<{ tipo: TipoMencion; q: string }>();
  protected readonly result = signal<Muro | null>(null);
  protected readonly team = signal<EquipoSocial | null>(null);
  protected readonly loading = signal(false);
  protected readonly error = signal('');
  protected readonly scope = signal<Scope>('GLOBAL');
  protected readonly scopeId = signal<number | null>(null);
  protected readonly tag = signal<string | null>(null);
  protected readonly tab = signal('PARA_TI');
  protected readonly heading = signal('El muro de nuestra comunidad');
  protected readonly composer = signal(false);
  protected readonly trends = signal<Tendencia[]>([]);
  protected readonly trendError = signal(false);
  protected readonly suggestions = signal<Sugerencia[]>([]);
  protected readonly exploreError = signal(false);
  protected readonly ruta = rutaMencion;
  protected exploreType: TipoMencion = 'EQUIPO';
  protected exploreQuery = '';
  protected readonly tabs = [
    { value: 'PARA_TI', label: 'Para ti' },
    { value: 'RECIENTES', label: 'Recientes' },
    { value: 'MIS_EQUIPOS', label: 'Mis equipos' },
    { value: 'SOLO_ACTIVIDADES', label: 'Actividades' },
    { value: 'SOLO_NOTICIAS', label: 'Noticias' },
  ];
  constructor() {
    combineLatest([this.route.paramMap, this.route.queryParamMap, this.route.data, this.reload])
      .pipe(
        debounceTime(0),
        switchMap(([params, query, data]) => {
          this.scope.set(data['scope'] ?? 'GLOBAL');
          const id = params.get('id') ? Number(params.get('id')) : null;
          this.scopeId.set(id);
          this.tag.set(params.get('tag'));
          this.tab.set(query.get('tab') ?? 'PARA_TI');
          this.heading.set(
            this.tag()
              ? `#${this.tag()}`
              : this.scope() === 'EQUIPO'
                ? 'El muro del equipo'
                : this.scope() === 'ACTIVIDAD'
                  ? 'El muro de la actividad'
                  : 'El muro de nuestra comunidad',
          );
          this.error.set('');
          this.result.set(null);
          this.loading.set(true);
          this.team.set(null);
          const page = Number(query.get('page') ?? 0);
          if (
            (id !== null && (!Number.isSafeInteger(id) || id < 1)) ||
            !Number.isSafeInteger(page) ||
            page < 0
          ) {
            this.error.set('El enlace no es válido.');
            this.loading.set(false);
            return of(null);
          }
          return forkJoin({
            muro: this.api.muro(page, this.tab(), this.scope(), id, this.tag()),
            team: this.scope() === 'EQUIPO' && id ? this.api.equipo(id) : of(null),
          }).pipe(
            catchError((e) => {
              this.error.set(errorSocial(e));
              return of(null);
            }),
          );
        }),
        takeUntilDestroyed(this.destroy),
      )
      .subscribe((result) => {
        if (result) {
          this.result.set(result.muro);
          this.team.set(result.team);
        }
        this.loading.set(false);
      });
    this.explore
      .pipe(
        debounceTime(250),
        switchMap((x) =>
          x.q.trim().length < 2
            ? of([])
            : this.api.sugerir(x.tipo, x.q).pipe(
                catchError(() => {
                  this.exploreError.set(true);
                  return of([]);
                }),
              ),
        ),
        takeUntilDestroyed(this.destroy),
      )
      .subscribe((x) => this.suggestions.set(x));
    this.loadTrends();
  }
  protected refresh() {
    this.reload.next(this.reload.value + 1);
  }
  protected page(page: number) {
    void this.router.navigate([], {
      relativeTo: this.route,
      queryParams: { page },
      queryParamsHandling: 'merge',
    });
  }
  protected selectTab(tab: string) {
    void this.router.navigate([], {
      relativeTo: this.route,
      queryParams: { tab, page: 0 },
      queryParamsHandling: 'merge',
    });
  }
  protected find(q: string) {
    this.exploreQuery = q;
    this.exploreError.set(false);
    this.suggestions.set([]);
    this.explore.next({ tipo: this.exploreType, q });
  }
  protected loadTrends() {
    this.trendError.set(false);
    this.api
      .tendencias()
      .pipe(takeUntilDestroyed(this.destroy))
      .subscribe({ next: (r) => this.trends.set(r), error: () => this.trendError.set(true) });
  }
}
