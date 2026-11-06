import { Component, DestroyRef, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { TuiButton } from '@taiga-ui/core';
import {
  BehaviorSubject,
  catchError,
  combineLatest,
  debounceTime,
  distinctUntilChanged,
  forkJoin,
  map,
  Observable,
  of,
  switchMap,
} from 'rxjs';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { Avatar, urlAvatarFoto } from '../../shared/components/avatar/avatar';
import { SocialService } from './social-service';
import { Autor, Comentario, Page, PerfilCabecera, PostResumen, TabContenido } from './social.model';
import { errorSocial, imagenesPublicacion } from './social.utils';
import { SocialPostCard } from './social-post-card';
import { SocialPager } from './social-pager';
import { SocialText } from './social-text';
import { SocialIcon } from './social-icon';
import { SocialModal } from './social-modal';
import { SocialProfileEditor } from './social-profile-editor';
import { mentionDraft } from './social-mention-draft';
interface PerfilVista {
  autor: Autor | null;
  posts: Page<PostResumen> | null;
  comments: Page<Comentario> | null;
}
@Component({
  selector: 'app-social-profile',
  imports: [
    DatePipe,
    RouterLink,
    TuiButton,
    Avatar,
    SocialPostCard,
    SocialPager,
    SocialText,
    SocialIcon,
    SocialModal,
    SocialProfileEditor,
  ],
  styleUrls: ['./social.css', './social-profile.css', './social-profile-publicaciones.css', './social-profile-comments.css'],
  template: `<div class="stack profile-page">
    <section class="profile-header">
      <div class="profile-cover">
        @if (header()?.portadaUrl; as url) {
          <img [src]="url" alt="Portada del perfil" (error)="cover.hidden = true" #cover />
        }
        @if (header()?.propietario) {
          <button
            class="cover-edit"
            tuiButton
            type="button"
            size="s"
            appearance="secondary"
            (click)="coverOnly.set(true); onlyPresentation.set(false); editing.set(true)"
          >
            <social-icon name="camera" />Editar portada
          </button>
        }
      </div>
      <div class="profile-identity">
        <app-avatar
          [foto]="avatar((header()?.autor || view()?.autor)?.fotoUrl)"
          [iniciales]="initials((header()?.autor || view()?.autor)?.nombreCompleto || '')"
          size="xxl"
        />
        <div class="profile-name">
          <h1>
            {{
              (header()?.autor || view()?.autor)?.nombreCompleto ||
                (mine() ? 'Mi perfil' : 'Perfil de la comunidad')
            }}
          </h1>
          <p>{{ (header()?.autor || view()?.autor)?.rolPrincipal }}</p>
          @if (header()?.equipos?.length) {
            <div class="chips">
              @for (team of header()!.equipos; track team.idEquipo) {
                <a [routerLink]="['/social/equipos', team.idEquipo]"
                  ><social-icon name="users" />{{ team.nombreEquipo }}</a
                >
              }
            </div>
          }
        </div>
        @if (header()?.propietario) {
          <button
            tuiButton
            type="button"
            size="s"
            appearance="secondary"
            (click)="coverOnly.set(false); onlyPresentation.set(false); editing.set(true)"
          >
            <social-icon name="pencil" />Editar perfil
          </button>
        }
      </div>
      <nav class="tabs profile-tabs" aria-label="Contenido del perfil">
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
    </section>
    @if (error()) {
      <div class="error" role="alert">
        {{ error()
        }}<button tuiButton appearance="flat" type="button" size="s" (click)="refresh()">
          Reintentar
        </button>
      </div>
    }
    <div class="profile-layout">
      <aside class="profile-sidebar">
        <section class="panel profile-info">
          <div class="section-heading">
            <h2>Datos personales</h2>
            @if (header()?.propietario) {
              <button
                tuiButton
                type="button"
                size="s"
                appearance="flat"
                aria-label="Editar presentación"
                (click)="coverOnly.set(false); onlyPresentation.set(true); editing.set(true)"
              >
                <social-icon name="pencil" />
              </button>
            }
          </div>
          @if (header()?.presentacion; as bio) {
            <p class="profile-bio">{{ bio }}</p>
          }
          @if (header()?.autor || view()?.autor; as a) {
            <dl>
              <div>
                <dt><social-icon name="user" />Nombre</dt>
                <dd>{{ a.nombreCompleto }}</dd>
              </div>
              @if (a.rolPrincipal) {
                <div>
                  <dt><social-icon name="briefcase" />Rol</dt>
                  <dd>{{ a.rolPrincipal }}</dd>
                </div>
              }
              @if (header()?.equipos?.length) {
                <div>
                  <dt><social-icon name="users" />Equipo</dt>
                  <dd>
                    @for (team of header()!.equipos; track team.idEquipo) {
                      <span>{{ team.nombreEquipo }}</span>
                    }
                  </dd>
                </div>
              }
            </dl>
          }
        </section>
        <section class="panel profile-friends">
          <div class="section-heading">
            <h2>Amigos</h2>
            <small>{{ friends()?.totalElements || 0 }}</small>
          </div>
          <p class="muted">Compañeros de equipo</p>
          @if (friendsLoading()) {
            <p role="status">Cargando compañeros…</p>
          }
          @if (friendsError()) {
            <p class="error" role="alert">{{ friendsError() }}</p>
            <button tuiButton type="button" size="s" appearance="flat" (click)="loadFriends(0)">
              Reintentar
            </button>
          }
          <div class="friend-mosaic">
            @for (person of friends()?.content || []; track person.idUsuario) {
              <article class="friend-tile">
                <a
                  [routerLink]="['/social/usuarios', person.idUsuario]"
                  [attr.aria-label]="'Ver perfil de ' + person.nombreCompleto"
                >
                  <app-avatar
                    [foto]="avatar(person.fotoUrl)"
                    [iniciales]="initials(person.nombreCompleto)"
                    size="xl"
                    shape="square"
                  /><span>{{ person.nombreCompleto }}</span></a
                >
              </article>
            } @empty {
              @if (!friendsLoading() && !friendsError()) {
                <p class="muted">Sin compañeros de equipo.</p>
              }
            }
          </div>
          @if (friends(); as page) {
            <social-pager
              [page]="page.number"
              [total]="page.totalPages"
              (change)="loadFriends($event)"
            />
          }
        </section>
      </aside>
      <section class="stack profile-content" [attr.aria-label]="tab() === 'COMENTARIOS' ? 'Comentarios del perfil' : 'Publicaciones del perfil'">
        <div class="panel publication-views">
          <div>
            <h2>{{ tab() === 'COMENTARIOS' ? (mine() ? 'Mis comentarios' : 'Comentarios') : 'Publicaciones' }}</h2>
            @if (tab() === 'COMENTARIOS') {
              <p class="comments-intro">{{ mine() ? 'Tus aportes a las conversaciones de la comunidad.' : 'Sus aportes a las conversaciones de la comunidad.' }}</p>
            }
          </div>
          @if (tab() === 'COMENTARIOS' && view()?.comments; as comments) {
            <span class="comments-count">{{ comments.totalElements }} {{ comments.totalElements === 1 ? 'comentario' : 'comentarios' }}</span>
          }
          @if (tab() !== 'COMENTARIOS') {
            <div class="view-switch" role="group" aria-label="Vista de publicaciones">
              <button
                tuiButton
                appearance="flat"
                size="s"
                type="button"
                aria-label="Vista de lista"
                [attr.aria-pressed]="layout() === 'lista'"
                (click)="setLayout('lista')"
              >
                <social-icon name="list" /><span>Lista</span>
              </button>
              <button
                tuiButton
                appearance="flat"
                size="s"
                type="button"
                aria-label="Vista de cuadrícula"
                [attr.aria-pressed]="layout() === 'cuadricula'"
                (click)="setLayout('cuadricula')"
              >
                <social-icon name="grid-2x2" /><span>Cuadrícula</span>
              </button>
            </div>
          }
        </div>
        @if (mine() && tab() === 'GUARDADOS') {
          <p class="notice">Solo tú puedes ver tus guardados.</p>
        }
        @if (loading()) {
          <p class="loading" role="status">Cargando contenido…</p>
        }
        @if (view(); as v) {
          @if (v.posts; as posts) {
            @if (layout() === 'cuadricula') {
              <div class="profile-post-grid">
                @for (p of posts.content; track p.idPost) {
                  <a
                    class="profile-post-preview"
                    [routerLink]="['/social/posts', p.idPost]"
                    [queryParams]="{ comentar: 1 }"
                  >
                    <div class="preview-media">
                      @if (cover(p); as url) {
                        @if (!failedPreviews().has(url)) {
                          <img
                            [src]="url"
                            alt="Imagen de la publicación"
                            loading="lazy"
                            (error)="previewFailed(url)"
                          />
                        } @else {
                          <span class="preview-placeholder"
                            ><social-icon name="image-off" /><span>Imagen no disponible</span></span
                          >
                        }
                      } @else {
                        <span class="preview-placeholder"
                          ><social-icon name="file-text" /><span
                            >Publicación de la comunidad</span
                          ></span
                        >
                      }
                      <span class="preview-kind"
                        ><social-icon
                          [name]="
                            p.tipoContenido === 'SHARE'
                              ? 'share-2'
                              : p.tipoContenido === 'ACTIVIDAD'
                                ? 'calendar-days'
                                : 'file-text'
                          "
                        />{{ previewType(p) }}</span
                      >
                      @if (previewImageCount(p) > 1) {
                        <span class="preview-image-count"
                          ><social-icon name="images" />{{ previewImageCount(p) }}</span
                        >
                      }
                    </div>
                    <div class="preview-copy">
                      <h3>{{ p.titulo || p.postOriginal?.titulo || 'Publicación compartida' }}</h3>
                      <p class="preview-summary">
                        {{ previewSummary(p) || 'Abre la publicación para ver la conversación.' }}
                      </p>
                      <div class="preview-meta">
                        <app-avatar
                          [foto]="avatar(p.autor.fotoUrl)"
                          [iniciales]="initials(p.autor.nombreCompleto)"
                          size="s"
                        />
                        <div class="preview-author">
                          <strong>{{ p.autor.nombreCompleto }}</strong
                          ><time [attr.datetime]="p.fechaPublicacion"
                            >{{ p.fechaPublicacion | date: 'dd MMM yyyy' }} ·
                            {{ p.fechaPublicacion | date: 'HH:mm' }}</time
                          >
                        </div>
                      </div>
                      <div class="preview-stats">
                        <span [attr.aria-label]="p.totalReacciones + ' reacciones'"
                          ><social-icon name="thumbs-up" />{{ p.totalReacciones }}</span
                        >
                        <span [attr.aria-label]="p.totalComentarios + ' comentarios'"
                          ><social-icon name="message-circle" />{{ p.totalComentarios }}</span
                        >
                        <span [attr.aria-label]="p.totalShares + ' compartidos'"
                          ><social-icon name="share-2" />{{ p.totalShares }}</span
                        >
                        <social-icon class="preview-open" name="arrow-up-right" />
                      </div>
                    </div>
                  </a>
                } @empty {
                  <p class="empty">Sin publicaciones.</p>
                }
              </div>
            } @else {
              @for (p of posts.content; track p.idPost) {
                <social-post-card [post]="p" (changed)="refresh()" (deleted)="refresh()" />
              } @empty {
                <p class="empty">Sin publicaciones.</p>
              }
            }
            <social-pager
              [page]="posts.number"
              [total]="posts.totalPages"
              (change)="page($event)"
            />
          }
          @if (v.comments; as comments) {
            @for (c of comments.content; track c.idComentario) {
              <article class="profile-comment" [attr.aria-label]="'Comentario de ' + c.autor.nombreCompleto">
                <header class="profile-comment-header">
                  <app-avatar [foto]="avatar(c.autor.fotoUrl)" [iniciales]="initials(c.autor.nombreCompleto)" size="m" />
                  <div class="profile-comment-author">
                    <a [routerLink]="['/social/usuarios', c.autor.idUsuario]">{{ c.autor.nombreCompleto }}</a>
                    <time [attr.datetime]="c.fechaComentario">{{ c.fechaComentario | date: 'dd MMM yyyy' }} · {{ c.fechaComentario | date: 'HH:mm' }}@if (c.editado) { <span> · Editado</span> }</time>
                  </div>
                  <span class="profile-comment-kind"><social-icon [name]="c.idComentarioPadre ? 'reply' : 'message-circle'" />{{ c.idComentarioPadre ? 'Respuesta' : 'Comentario' }}</span>
                </header>
                <div class="profile-comment-body">
                  @if (c.eliminado) {
                    <p class="muted">Este comentario fue eliminado.</p>
                  } @else {
                    <social-text [text]="c.contenido" [mentions]="c.menciones" />
                    @if (c.adjuntosUrls.length) {
                      <div class="profile-comment-images" aria-label="Imágenes del comentario">
                        @for (url of c.adjuntosUrls; track $index) {
                          <a [href]="url" target="_blank" rel="noopener noreferrer" [attr.aria-label]="'Abrir imagen ' + ($index + 1) + ' en otra pestaña'">
                            @if (!failedPreviews().has(url)) {
                              <img [src]="url" [alt]="'Imagen adjunta ' + ($index + 1)" loading="lazy" (error)="previewFailed(url)" />
                            } @else {
                              <span class="comment-image-fallback"><social-icon name="image-off" />Imagen no disponible</span>
                            }
                          </a>
                        }
                      </div>
                    }
                  }
                </div>
                <footer class="profile-comment-footer">
                  <span class="profile-comment-context"><social-icon name="message-circle" />En una publicación de la comunidad</span>
                  <a class="profile-comment-open"
                  [routerLink]="['/social/posts', c.idPost]"
                  [fragment]="'comentario-' + c.idComentario"
                  >Ver conversación<social-icon name="arrow-up-right" /></a>
                </footer>
              </article>
            } @empty {
              <div class="profile-comments-empty">
                <span class="comments-empty-icon"><social-icon name="messages-square" /></span>
                <h3>{{ mine() ? 'Tu próxima conversación empieza aquí' : 'Todavía no hay comentarios' }}</h3>
                <p>{{ mine() ? 'Cuando comentes o respondas una publicación, encontrarás tus aportes en este espacio.' : 'Los comentarios de este perfil aparecerán aquí.' }}</p>
                @if (mine()) { <a tuiButton appearance="secondary" size="s" routerLink="/social">Explorar publicaciones<social-icon name="arrow-up-right" /></a> }
              </div>
            }
            <social-pager
              [page]="comments.number"
              [total]="comments.totalPages"
              (change)="page($event)"
            />
          }
        }
      </section>
    </div>
    @if (editing() && header()?.propietario) {
      <social-modal
        [title]="
          coverOnly()
            ? 'Editar portada'
            : onlyPresentation()
              ? 'Editar presentación'
              : 'Editar perfil'
        "
        [busy]="editBusy()"
        (closed)="editing.set(false)"
      >
        <social-profile-editor
          [perfil]="header()!"
          [onlyPresentation]="onlyPresentation()"
          [coverOnly]="coverOnly()"
          (pending)="editBusy.set($event)"
          (updated)="header.set($event)"
          (saved)="editing.set(false)"
          (cancel)="editing.set(false)"
        />
      </social-modal>
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
  protected readonly header = signal<PerfilCabecera | null>(null);
  protected readonly friends = signal<Page<Autor> | null>(null);
  protected readonly friendsLoading = signal(false);
  protected readonly friendsError = signal('');
  private friendRequest = 0;
  private profileKey = '';
  protected readonly mine = signal(false);
  protected readonly tab = signal<TabContenido>('PUBLICACIONES');
  protected readonly layout = signal<'lista' | 'cuadricula'>('lista');
  protected readonly failedPreviews = signal(new Set<string>());
  protected readonly loading = signal(false);
  protected readonly error = signal('');
  protected readonly avatar = urlAvatarFoto;
  protected readonly tabs = signal<{ value: TabContenido; label: string }[]>([]);
  protected readonly editing = signal(false);
  protected readonly editBusy = signal(false);
  protected readonly onlyPresentation = signal(false);
  protected readonly coverOnly = signal(false);
  constructor() {
    this.layout.set(
      this.route.snapshot.queryParamMap.get('vista') === 'cuadricula' ? 'cuadricula' : 'lista',
    );
    const query = this.route.queryParamMap.pipe(
      map((q) => ({ tab: q.get('tab') || 'PUBLICACIONES', page: Number(q.get('page') || 0) })),
      distinctUntilChanged((a, b) => a.tab === b.tab && a.page === b.page),
    );
    combineLatest([this.route.paramMap, query, this.route.data, this.reload])
      .pipe(
        debounceTime(0),
        switchMap(([params, q, data]) => {
          const mine = !!data['mine'];
          this.mine.set(mine);
          this.loading.set(true);
          this.error.set('');
          this.view.set(null);
          this.friendRequest++;
          this.tabs.set([
            { value: 'PUBLICACIONES', label: 'Todo' },
            { value: 'COMENTARIOS', label: 'Comentarios' },
            ...(mine
              ? [
                  { value: 'REACCIONES' as const, label: 'Mis reacciones' },
                  { value: 'GUARDADOS' as const, label: 'Mis guardados' },
                ]
              : []),
          ]);
          const id = Number(params.get('id'));
          const key = mine ? 'mine' : String(id);
          if (key !== this.profileKey) {
            this.profileKey = key;
            this.header.set(null);
            this.friends.set(null);
            this.editing.set(false);
          }
          this.friendsLoading.set(false);
          this.friendsError.set('');
          if (
            !this.tabs().some((t) => t.value === q.tab) ||
            !Number.isSafeInteger(q.page) ||
            q.page < 0 ||
            (!mine && (!Number.isSafeInteger(id) || id < 1))
          ) {
            this.error.set('La sección del perfil no es válida.');
            return of(null);
          }
          this.tab.set(q.tab as TabContenido);
          const request = mine
            ? this.api.miPerfilSocial().pipe(
                switchMap((h) =>
                  forkJoin({
                    header: of(h),
                    friends: this.api.companeros(h.autor.idUsuario!),
                    content: this.api.miContenido(this.tab(), q.page),
                  }),
                ),
                map((x) => ({
                  header: x.header,
                  friends: x.friends,
                  view: {
                    autor: x.header.autor,
                    posts:
                      q.tab === 'COMENTARIOS'
                        ? null
                        : q.tab === 'REACCIONES'
                          ? x.content.reacciones
                          : q.tab === 'GUARDADOS'
                            ? x.content.guardados
                            : x.content.publicaciones,
                    comments: q.tab === 'COMENTARIOS' ? x.content.comentarios : null,
                  },
                })),
              )
            : forkJoin({
                header: this.api.cabeceraPerfil(id),
                friends: this.api.companeros(id),
                content: this.api.perfil(id, q.page),
              }).pipe(
                map((x) => ({
                  header: x.header,
                  friends: x.friends,
                  view: {
                    autor: x.content.autor,
                    posts: q.tab === 'COMENTARIOS' ? null : x.content.publicaciones,
                    comments: q.tab === 'COMENTARIOS' ? x.content.comentarios : null,
                  },
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
      .subscribe((x) => {
        if (x) {
          this.header.set(x.header);
          this.friends.set(x.friends);
          this.view.set(x.view);
        } else {
          this.header.set(null);
          this.friends.set(null);
        }
        this.loading.set(false);
      });
  }
  protected initials(name: string) {
    return (
      name
        ?.trim()
        .split(/\s+/)
        .slice(0, 2)
        .map((x) => x[0])
        .join('')
        .toUpperCase() || '?'
    );
  }
  protected cover(post: PostResumen) {
    return (
      imagenesPublicacion(
        post.tipoContenido === 'SHARE' && post.postOriginal ? post.postOriginal : post,
      )[0] || null
    );
  }
  protected refresh() {
    this.reload.next(this.reload.value + 1);
  }
  protected select(tab: TabContenido) {
    void this.router.navigate([], {
      relativeTo: this.route,
      queryParams: { tab, page: 0 },
      queryParamsHandling: 'merge',
    });
  }
  protected page(page: number) {
    void this.router.navigate([], {
      relativeTo: this.route,
      queryParams: { page },
      queryParamsHandling: 'merge',
    });
  }
  protected setLayout(vista: 'lista' | 'cuadricula') {
    this.layout.set(vista);
  }
  protected previewFailed(url: string) {
    this.failedPreviews.update((urls) => new Set([...urls, url]));
  }
  protected previewType(post: PostResumen) {
    return post.tipoContenido === 'SHARE'
      ? 'Compartido'
      : post.tipoContenido === 'ACTIVIDAD'
        ? 'Actividad'
        : post.tipoContenido === 'NOTICIA'
          ? 'Noticia'
          : 'Publicación';
  }
  protected previewImageCount(post: PostResumen) {
    return imagenesPublicacion(
      post.tipoContenido === 'SHARE' && post.postOriginal ? post.postOriginal : post,
    ).length;
  }
  protected previewSummary(post: PostResumen) {
    return mentionDraft(post.resumen || post.postOriginal?.resumen || '').text;
  }
  protected loadFriends(page: number) {
    const id = this.header()?.autor.idUsuario;
    if (!id) return;
    const request = ++this.friendRequest;
    this.friendsLoading.set(true);
    this.friendsError.set('');
    this.api
      .companeros(id, page)
      .pipe(takeUntilDestroyed(this.destroy))
      .subscribe({
        next: (r) => {
          if (request === this.friendRequest) {
            this.friends.set(r);
            this.friendsLoading.set(false);
          }
        },
        error: (e) => {
          if (request === this.friendRequest) {
            this.friendsError.set(errorSocial(e));
            this.friendsLoading.set(false);
          }
        },
      });
  }
}
