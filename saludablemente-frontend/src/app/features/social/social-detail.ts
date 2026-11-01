import { SocialModal } from './social-modal';
import { DatePipe } from '@angular/common';
import { Avatar, urlAvatarFoto } from '../../shared/components/avatar/avatar';
import { SocialGallery } from './social-gallery';
import { afterEveryRender, Component, computed, DestroyRef, inject, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { TuiButton } from '@taiga-ui/core';
import {
  BehaviorSubject,
  catchError,
  combineLatest,
  debounceTime,
  forkJoin,
  of,
  switchMap,
} from 'rxjs';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { SocialService } from './social-service';
import { Page, Comentario, PostDetalle } from './social.model';
import { errorSocial, imagenesPublicacion, resumenDetalle } from './social.utils';
import { SocialPostCard } from './social-post-card';
import { SocialComment } from './social-comment';
import { SocialCommentEditor } from './social-comment-editor';
import { SocialPager } from './social-pager';
import { SocialText } from './social-text';
@Component({
  selector: 'app-social-detail',
  imports: [
    Avatar,
    DatePipe,
    SocialGallery,
    SocialModal,
    RouterLink,
    TuiButton,
    SocialPostCard,
    SocialComment,
    SocialCommentEditor,
    SocialPager,
    SocialText,
  ],
  styleUrls: ['./social.css', './social-detail.css'],
  template: ` <div class="stack">
    <a routerLink="/social">← Volver al muro</a>
    @if (error()) {
      <div class="error" role="alert">
        {{ error() }}
        <button tuiButton appearance="flat" size="s" type="button" (click)="refresh()">
          Reintentar
        </button>
      </div>
    }
    @if (loading()) {
      <p class="loading" role="status">Cargando publicación…</p>
    } @else if (post(); as p) {
      @if (card(); as c) {
        <social-post-card
          [post]="c"
          [detail]="true"
          (changed)="refresh()"
          (deleted)="post.set(null); error.set('Esta publicación ya no está disponible.')"
        />
      }
      @if (p.menciones.length) {
        <section class="panel">
          <h3>Menciones</h3>
          <social-text [mentions]="p.menciones" />
        </section>
      }
      @if (p.alcanceTotal !== null) {
        <p class="notice">
          Alcance: <strong>{{ p.alcanceTotal }}</strong> vistas contabilizadas. Este dato solo es
          visible para el autor y administradores.
        </p>
      }
      @if (commentOpen()) {
        <social-modal
          title="Comentar"
          [wide]="true"
          [busy]="commentBusy() || pendingThreads().size > 0"
          (closed)="closeComments()"
        >
          <div class="discussion-layout">
          <aside class="discussion-post" aria-label="Publicación que estás comentando">
            <header class="identity">
              <app-avatar [foto]="avatar(p.autor.fotoUrl)"
                [iniciales]="p.autor.nombreCompleto.slice(0, 1)" />
              <div><a [routerLink]="['/social/usuarios', p.autor.idUsuario]">
                <strong>{{ p.autor.nombreCompleto }}</strong>
              </a><small>{{ p.fechaPublicacion | date: 'dd/MM/yyyy · HH:mm' }}</small></div>
            </header>
            <h2>{{ p.titulo || 'Publicación' }}</h2>
            <social-text [text]="p.resumen" [mentions]="p.menciones" />
            @if (p.cuerpo) {
              <section class="post-body">
                <social-text [text]="p.cuerpo" [mentions]="p.menciones" />
              </section>
            }
            @if (discussionImages().length) {
              <div class="discussion-media">
                <social-gallery [urls]="discussionImages()" [title]="p.titulo || 'Publicación'" />
              </div>
            }
          </aside>
          <section class="conversation" aria-label="Comentarios de la publicación">
            <div class="comments-scroll" tabindex="0" aria-label="Lista de comentarios">
              @if (focused(); as selected) {
                @if (!inPage(selected.idComentario)) {
                  <section class="notice">
                    <h3>Comentario seleccionado</h3>
                    @if (selected.idComentarioPadre) {
                      <a
                        [routerLink]="['/social/posts', selected.idPost]"
                        [fragment]="'comentario-' + selected.idComentarioPadre"
                        >Ver comentario raíz</a
                      >
                    }
                    <social-comment
                      [comentario]="selected"
                      [replyInComposer]="true"
                      (replyRequested)="replyTo($event)"
                      (pending)="setThreadPending(selected.idComentario, $event)"
                      (changed)="loadComments(commentPage()); loadFocused()"
                    />
                  </section>
                }
              }
              @if (focusError()) {
                <p class="notice" role="status">{{ focusError() }}</p>
              }
              @if (commentError()) {
                <p class="error" role="alert">
                  {{ commentError() }}
                  <button
                    tuiButton
                    type="button"
                    appearance="flat"
                    size="s"
                    (click)="loadComments(commentPage())"
                  >
                    Reintentar
                  </button>
                </p>
              }
              @if (commentLoading()) {
                <p role="status">Cargando comentarios…</p>
              } @else if (comments(); as list) {
                @for (c of list.content; track c.idComentario) {
                  <social-comment
                    [comentario]="c"
                    [replyInComposer]="true"
                    (replyRequested)="replyTo($event)"
                    (pending)="setThreadPending(c.idComentario, $event)"
                    (changed)="loadComments(list.number)"
                  />
                } @empty {
                  <p class="empty">Sé la primera persona en comentar.</p>
                }
                <social-pager
                  [page]="list.number"
                  [total]="list.totalPages"
                  (change)="loadComments($event)"
                />
              }
            </div>
            <footer class="conversation-composer">
              @if (replyTarget(); as target) {
                <p class="reply-context">
                  Respondiendo a <strong>{{ target.autor.nombreCompleto }}</strong>
                </p>
              }
              @for (target of [replyTarget()]; track target?.idComentario ?? 0) {
                <social-comment-editor
                  [post]="p.idPost"
                  [padre]="target?.idComentarioPadre ?? target?.idComentario ?? null"
                  [initialText]="
                    target?.idComentarioPadre && target?.autor?.username
                      ? '@' + target!.autor.username + ' '
                      : ''
                  "
                  (pending)="commentBusy.set($event)"
                  (cancel)="replyTarget.set(null)"
                  (sent)="replyTarget.set(null); loadComments(commentPage())"
                />
              }
            </footer>
          </section>
          </div>
        </social-modal>
      }
    }
  </div>`,
})
export class SocialDetail {
  protected readonly avatar = urlAvatarFoto;
  protected readonly discussionImages = computed(() =>
    this.card() ? imagenesPublicacion(this.card()!) : [],
  );
  protected readonly replyTarget = signal<Comentario | null>(null);
  protected replyTo(comment: Comentario) {
    if (this.commentBusy()) return;
    this.replyTarget.set(comment);
    requestAnimationFrame(() =>
      document.querySelector<HTMLTextAreaElement>('.conversation-composer textarea')?.focus(),
    );
  }
  protected readonly pendingThreads = signal<Set<number>>(new Set());
  protected setThreadPending(id: number, busy: boolean) {
    this.pendingThreads.update((ids) => {
      const next = new Set(ids);
      busy ? next.add(id) : next.delete(id);
      return next;
    });
  }
  protected readonly commentOpen = signal(false);
  protected readonly commentBusy = signal(false);
  private readonly router = inject(Router);
  private readonly api = inject(SocialService);
  private readonly route = inject(ActivatedRoute);
  private readonly destroy = inject(DestroyRef);
  private readonly reload = new BehaviorSubject(0);
  private commentRequest = 0;
  protected readonly focused = signal<Comentario | null>(null);
  protected readonly focusError = signal('');
  private focusRequest = 0;
  private scrolled = '';
  protected readonly post = signal<PostDetalle | null>(null);
  protected readonly card = computed(() => (this.post() ? resumenDetalle(this.post()!) : null));
  protected readonly comments = signal<Page<Comentario> | null>(null);
  protected readonly commentPage = signal(0);
  protected readonly loading = signal(false);
  protected readonly error = signal('');
  protected readonly commentLoading = signal(false);
  protected readonly commentError = signal('');
  constructor() {
    this.route.queryParamMap
      .pipe(debounceTime(0), takeUntilDestroyed(this.destroy))
      .subscribe((params) => {
        if (params.get('comentar') === '1') this.commentOpen.set(true);
      });
    afterEveryRender(() => {
      const fragment = this.route.snapshot.fragment;
      if (!fragment || this.scrolled === fragment) return;
      const element = document.getElementById(fragment);
      if (element) {
        element.scrollIntoView?.({ block: 'center' });
        this.scrolled = fragment;
      }
    });
    this.route.fragment
      .pipe(debounceTime(0), takeUntilDestroyed(this.destroy))
      .subscribe((fragment) => {
        if (fragment?.startsWith('comentario-')) this.commentOpen.set(true);
        this.scrolled = '';
        this.loadFocused();
      });
    combineLatest([this.route.paramMap, this.reload])
      .pipe(
        debounceTime(0),
        switchMap(([params]) => {
          this.post.set(null);
          this.comments.set(null);
          this.loading.set(true);
          this.error.set('');
          this.commentRequest++;
          this.focusRequest++;
          this.focused.set(null);
          const id = Number(params.get('id'));
          if (!Number.isSafeInteger(id) || id < 1) {
            this.error.set('El enlace no es válido.');
            return of(null);
          }
          return forkJoin({ post: this.api.detalle(id), comments: this.api.comentarios(id) }).pipe(
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
          this.post.set(x.post);
          this.comments.set(x.comments);
          this.commentPage.set(0);
          this.loadFocused();
        }
        this.loading.set(false);
      });
  }
  protected closeComments() {
    if (this.commentBusy() || this.pendingThreads().size) return;
    this.replyTarget.set(null);
    this.commentOpen.set(false);
    void this.router.navigate([], {
      relativeTo: this.route,
      queryParams: { comentar: null },
      queryParamsHandling: 'merge',
      fragment: undefined,
      replaceUrl: true,
    });
  }
  protected refresh() {
    this.reload.next(this.reload.value + 1);
  }
  protected loadComments(page: number) {
    const p = this.post();
    if (!p) return;
    const request = ++this.commentRequest;
    this.commentLoading.set(true);
    this.commentError.set('');
    this.api
      .comentarios(p.idPost, page)
      .pipe(takeUntilDestroyed(this.destroy))
      .subscribe({
        next: (r) => {
          if (request !== this.commentRequest) return;
          if (r.content.length === 0 && page > 0) {
            this.loadComments(page - 1);
            return;
          }
          this.comments.set(r);
          this.commentPage.set(page);
          this.api
            .detalle(p.idPost)
            .pipe(takeUntilDestroyed(this.destroy))
            .subscribe({
              next: (d) => {
                if (request === this.commentRequest) this.post.set(d);
              },
              error: () => {},
            });
          this.commentLoading.set(false);
        },
        error: (e) => {
          if (request === this.commentRequest) {
            this.commentError.set(errorSocial(e));
            this.commentLoading.set(false);
          }
        },
      });
  }
  protected inPage(id: number) {
    return (
      this.comments()?.content.some(
        (c) => c.idComentario === id || c.respuestas.some((r) => r.idComentario === id),
      ) ?? false
    );
  }
  protected loadFocused() {
    const post = this.post();
    const fragment = this.route.snapshot.fragment;
    this.focused.set(null);
    this.focusError.set('');
    const request = ++this.focusRequest;
    if (!post || !fragment || !/^comentario-\d+$/.test(fragment)) return;
    const id = Number(fragment.slice(11));
    this.api
      .comentario(post.idPost, id)
      .pipe(takeUntilDestroyed(this.destroy))
      .subscribe({
        next: (c) => {
          if (request === this.focusRequest) this.focused.set(c);
        },
        error: (e) => {
          if (request === this.focusRequest) this.focusError.set(errorSocial(e));
        },
      });
  }
}
