import { Component, DestroyRef, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { TuiButton } from '@taiga-ui/core';
import { catchError, of, switchMap } from 'rxjs';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { SocialService } from './social-service';
import { Busqueda, Page, TipoBusqueda } from './social.model';
import { errorSocial } from './social.utils';
import { SocialPostCard } from './social-post-card';
import { SocialPager } from './social-pager';
import { SocialText } from './social-text';
@Component({
  selector: 'app-social-search',
  imports: [FormsModule, RouterLink, TuiButton, SocialPostCard, SocialPager, SocialText],
  styleUrl: './social.css',
  template: ` <div class="stack">
    <form class="panel fields" (ngSubmit)="search()">
      <h2>Encuentra una conversación</h2>
      <div class="two">
        <label
          >Buscar<input
            name="q"
            [(ngModel)]="query"
            maxlength="200"
            placeholder="Escribe un nombre, una palabra o un hashtag" /></label
        ><label
          >En<select name="tipo" [(ngModel)]="tipo">
            <option value="POST">Publicaciones</option>
            <option value="COMENTARIO">Comentarios</option>
            <option value="AUTOR">Autores</option>
            <option value="HASHTAG">Hashtags</option>
          </select></label
        >
      </div>
      <div><button tuiButton size="s" type="submit" [disabled]="!query.trim()">Buscar</button></div>
    </form>
    @if (error()) {
      <p class="error" role="alert">
        {{ error() }}
        <button tuiButton size="s" type="button" appearance="flat" (click)="search()">
          Reintentar
        </button>
      </p>
    }
    @if (loading()) {
      <p role="status" class="loading">Buscando…</p>
    } @else if (result(); as r) {
      @switch (r.tipo) {
        @case ('POST') {
          @for (p of r.publicaciones.content; track p.idPost) {
            <social-post-card
              [post]="p"
              (changed)="search(currentPage())"
              (deleted)="search(currentPage())"
            />
          } @empty {
            <p class="empty">No encontramos publicaciones.</p>
          }
          <social-pager
            [page]="r.publicaciones.number"
            [total]="r.publicaciones.totalPages"
            (change)="search($event)"
          />
        }
        @case ('COMENTARIO') {
          @for (c of r.comentarios.content; track c.idComentario) {
            <article class="panel">
              <p>
                <a [routerLink]="['/social/usuarios', c.autor.idUsuario]">{{
                  c.autor.nombreCompleto
                }}</a>
              </p>
              <social-text [text]="c.contenido" [mentions]="c.menciones" /><a
                [routerLink]="['/social/posts', c.idPost]"
                [fragment]="'comentario-' + c.idComentario"
                >Ver en la publicación</a
              >
            </article>
          } @empty {
            <p class="empty">No encontramos comentarios.</p>
          }
          <social-pager
            [page]="r.comentarios.number"
            [total]="r.comentarios.totalPages"
            (change)="search($event)"
          />
        }
        @case ('AUTOR') {
          <section class="panel">
            @for (a of r.autores; track a.idUsuario) {
              <p>
                <a [routerLink]="['/social/usuarios', a.idUsuario]">{{ a.nombreCompleto }}</a
                ><small> · {{ a.nombreEquipo }}</small>
              </p>
            } @empty {
              <p class="empty">No encontramos autores.</p>
            }
            <small
              >La búsqueda de autores muestra hasta 20 coincidencias. Precisa el nombre para afinar
              los resultados.</small
            >
          </section>
        }
        @case ('HASHTAG') {
          <section class="panel chips">
            @for (t of r.hashtags; track t) {
              <a class="chip" [routerLink]="['/social/hashtags', t]">#{{ t }}</a>
            } @empty {
              <p class="empty">No encontramos hashtags.</p>
            }
          </section>
          @if (currentPage() > 0 || r.hashtags.length === 20) {
            <nav class="pager" aria-label="Páginas de hashtags">
              <button
                tuiButton
                size="s"
                type="button"
                appearance="secondary"
                [disabled]="currentPage() === 0"
                (click)="search(currentPage() - 1)"
              >
                Anterior</button
              ><span>Página {{ currentPage() + 1 }}</span
              ><button
                tuiButton
                size="s"
                type="button"
                appearance="secondary"
                [disabled]="r.hashtags.length < 20"
                (click)="search(currentPage() + 1)"
              >
                Siguiente
              </button>
            </nav>
          }
        }
      }
    } @else if (!loading() && !error()) {
      <p class="empty">Busca experiencias, personas o temas de tu comunidad.</p>
    }
  </div>`,
})
export class SocialSearch {
  private readonly api = inject(SocialService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly destroy = inject(DestroyRef);
  protected query = '';
  protected tipo: TipoBusqueda = 'POST';
  protected readonly currentPage = signal(0);
  protected readonly result = signal<Busqueda | null>(null);
  protected readonly loading = signal(false);
  protected readonly error = signal('');
  constructor() {
    this.route.queryParamMap
      .pipe(
        switchMap((params) => {
          this.query = params.get('q') ?? '';
          const type = params.get('tipo') ?? 'POST';
          const page = Number(params.get('page') ?? 0);
          this.result.set(null);
          this.error.set('');
          if (
            !['POST', 'COMENTARIO', 'AUTOR', 'HASHTAG'].includes(type) ||
            !Number.isSafeInteger(page) ||
            page < 0
          ) {
            this.error.set('El enlace de búsqueda no es válido.');
            return of(null);
          }
          this.tipo = type as TipoBusqueda;
          this.currentPage.set(page);
          this.loading.set(!!this.query.trim());
          if (!this.query.trim()) return of(null);
          return this.api.buscar(this.query, this.tipo, page).pipe(
            catchError((e) => {
              this.error.set(errorSocial(e));
              return of(null);
            }),
          );
        }),
        takeUntilDestroyed(this.destroy),
      )
      .subscribe((x) => {
        this.result.set(x);
        this.loading.set(false);
      });
  }
  protected search(page = 0) {
    void this.router
      .navigate([], {
        relativeTo: this.route,
        queryParams: { q: this.query.trim(), tipo: this.tipo, page },
      })
      .then((changed) => {
        if (!changed && this.query.trim()) {
          this.loading.set(true);
          this.api
            .buscar(this.query, this.tipo, page)
            .pipe(takeUntilDestroyed(this.destroy))
            .subscribe({
              next: (r) => {
                this.result.set(r);
                this.loading.set(false);
                this.error.set('');
              },
              error: (e) => {
                this.error.set(errorSocial(e));
                this.loading.set(false);
              },
            });
        }
      });
  }
}
