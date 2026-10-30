import { Component, DestroyRef, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { TuiButton } from '@taiga-ui/core';
import {
  BehaviorSubject,
  catchError,
  combineLatest,
  debounceTime,
  map,
  Observable,
  of,
  switchMap,
} from 'rxjs';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { Avatar, urlAvatarFoto } from '../../shared/components/avatar/avatar';
import { SocialService } from './social-service';
import { Autor, Comentario, Page, PostResumen, TabContenido } from './social.model';
import { errorSocial } from './social.utils';
import { SocialPostCard } from './social-post-card';
import { SocialPager } from './social-pager';
import { SocialText } from './social-text';
interface PerfilVista {
  autor: Autor | null;
  posts: Page<PostResumen> | null;
  comments: Page<Comentario> | null;
}
@Component({
  selector: 'app-social-profile',
  imports: [DatePipe, RouterLink, TuiButton, Avatar, SocialPostCard, SocialPager, SocialText],
  styleUrl: './social.css',
  template: ` <div class="stack">
    <section class="panel">
      @if (view()?.autor; as a) {
        <div class="identity">
          <app-avatar
            [foto]="avatar(a.fotoUrl)"
            [iniciales]="initials(a.nombreCompleto)"
            size="l"
          />
          <div>
            <h2>{{ a.nombreCompleto }}</h2>
            <p class="muted">{{ a.rolPrincipal }} · {{ a.nombreEquipo }}</p>
          </div>
        </div>
      } @else {
        <h2>{{ mine() ? 'Mi contenido' : 'Perfil de la comunidad' }}</h2>
      }
      <nav class="tabs" aria-label="Contenido del perfil">
        @for (t of tabs(); track t.value) {
          <button
            type="button"
            [class.active]="tab() === t.value"
            [attr.aria-pressed]="tab() === t.value"
            (click)="select(t.value)"
          >
            {{ t.label }}
          </button>
        }
      </nav>
      @if (mine() && tab() === 'GUARDADOS') {
        <small>Solo tú puedes ver tus guardados.</small>
      }
    </section>
    @if (error()) {
      <p class="error" role="alert">
        {{ error() }}
        <button tuiButton type="button" size="s" appearance="flat" (click)="refresh()">
          Reintentar
        </button>
      </p>
    }
    @if (loading()) {
      <p class="loading" role="status">Cargando contenido…</p>
    } @else if (view(); as v) {
      @if (v.posts; as posts) {
        @for (p of posts.content; track p.idPost) {
          <social-post-card [post]="p" (changed)="refresh()" (deleted)="refresh()" />
        } @empty {
          <p class="empty">Todavía no hay publicaciones en esta sección.</p>
        }
        <social-pager [page]="posts.number" [total]="posts.totalPages" (change)="page($event)" />
      }
      @if (v.comments; as comments) {
        @for (c of comments.content; track c.idComentario) {
          <article class="panel">
            <small>{{ c.fechaComentario | date: 'medium' }}</small
            ><social-text [text]="c.contenido" [mentions]="c.menciones" /><a
              [routerLink]="['/social/posts', c.idPost]"
              [fragment]="'comentario-' + c.idComentario"
              >Ver comentario en la publicación</a
            >
          </article>
        } @empty {
          <p class="empty">Todavía no hay comentarios.</p>
        }
        <social-pager
          [page]="comments.number"
          [total]="comments.totalPages"
          (change)="page($event)"
        />
      }
    }
  </div>`,
})
export class SocialProfile {
  private readonly api = inject(SocialService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly destroy = inject(DestroyRef);
  private readonly reload = new BehaviorSubject(0);
  protected readonly view = signal<PerfilVista | null>(null);
  protected readonly mine = signal(false);
  protected readonly tab = signal<TabContenido>('PUBLICACIONES');
  protected readonly loading = signal(false);
  protected readonly error = signal('');
  protected readonly avatar = urlAvatarFoto;
  protected readonly tabs = signal<{ value: TabContenido; label: string }[]>([]);
  constructor() {
    combineLatest([this.route.paramMap, this.route.queryParamMap, this.route.data, this.reload])
      .pipe(
        debounceTime(0),
        switchMap(([params, q, data]) => {
          const mine = !!data['mine'];
          this.mine.set(mine);
          this.tabs.set([
            { value: 'PUBLICACIONES', label: mine ? 'Mis publicaciones' : 'Publicaciones' },
            { value: 'COMENTARIOS', label: mine ? 'Mis comentarios' : 'Comentarios' },
            ...(mine
              ? [
                  { value: 'REACCIONES' as const, label: 'Mis reacciones' },
                  { value: 'GUARDADOS' as const, label: 'Mis guardados' },
                ]
              : []),
          ]);
          const tab = q.get('tab') ?? 'PUBLICACIONES';
          const page = Number(q.get('page') ?? 0);
          this.view.set(null);
          this.error.set('');
          this.loading.set(true);
          if (
            !this.tabs().some((t) => t.value === tab) ||
            !Number.isSafeInteger(page) ||
            page < 0
          ) {
            this.error.set('La sección del perfil no es válida.');
            return of(null);
          }
          this.tab.set(tab as TabContenido);
          const request: Observable<PerfilVista> = mine
            ? this.api
                .miContenido(this.tab(), page)
                .pipe(
                  map((x) => ({
                    autor: null,
                    posts:
                      tab === 'COMENTARIOS'
                        ? null
                        : tab === 'REACCIONES'
                          ? x.reacciones
                          : tab === 'GUARDADOS'
                            ? x.guardados
                            : x.publicaciones,
                    comments: tab === 'COMENTARIOS' ? x.comentarios : null,
                  })),
                )
            : this.api
                .perfil(Number(params.get('id')), page)
                .pipe(
                  map((x) => ({
                    autor: x.autor,
                    posts: tab === 'COMENTARIOS' ? null : x.publicaciones,
                    comments: tab === 'COMENTARIOS' ? x.comentarios : null,
                  })),
                );
          return request.pipe(
            catchError((e) => {
              this.error.set(errorSocial(e));
              return of(null);
            }),
          );
        }),
        takeUntilDestroyed(this.destroy),
      )
      .subscribe((v) => {
        this.view.set(v);
        this.loading.set(false);
      });
  }
  protected initials(name: string) {
    return (
      name
        ?.split(' ')
        .slice(0, 2)
        .map((x) => x[0])
        .join('') || '?'
    );
  }
  protected refresh() {
    this.reload.next(this.reload.value + 1);
  }
  protected select(tab: TabContenido) {
    void this.router.navigate([], { relativeTo: this.route, queryParams: { tab, page: 0 } });
  }
  protected page(page: number) {
    void this.router.navigate([], {
      relativeTo: this.route,
      queryParams: { page },
      queryParamsHandling: 'merge',
    });
  }
}
