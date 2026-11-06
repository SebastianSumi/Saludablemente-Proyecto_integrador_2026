import { SocialIcon } from './social-icon';
import { SocialModal } from './social-modal';
import {
  Component,
  OnDestroy,
  computed,
  DestroyRef,
  effect,
  inject,
  input,
  output,
  signal,
} from '@angular/core';
import { DatePipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { TuiButton } from '@taiga-ui/core';
import { filter, finalize, switchMap } from 'rxjs';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { Avatar, urlAvatarFoto } from '../../shared/components/avatar/avatar';
import { AuthService } from '../../core/auth/auth-service';
import { ConfirmService } from '../../core/services/confirm-service';
import { Comentario, Reacciones } from './social.model';
import { SocialService } from './social-service';
import { SocialText } from './social-text';
import { SocialTextEditor } from './social-text-editor';
import { SocialReactions } from './social-reactions';
import { SocialReport } from './social-report';
import { SocialCommentEditor } from './social-comment-editor';
import { errorSocial } from './social.utils';
@Component({
  selector: 'social-comment',
  imports: [
    SocialIcon,
    SocialModal,
    DatePipe,
    RouterLink,
    TuiButton,
    Avatar,
    SocialText,
    SocialTextEditor,
    SocialReactions,
    SocialReport,
    SocialCommentEditor,
  ],
  styleUrls: ['./social.css', './social-comments.css'],
  template: ` <article class="comment" [attr.id]="'comentario-' + c().idComentario">
    <div class="comment-bubble">
      <header class="identity">
        <app-avatar
          [foto]="avatar(c().autor.fotoUrl)"
          [iniciales]="initials(c().autor.nombreCompleto)"
          size="s"
        />
        <div>
          <a [routerLink]="['/social/usuarios', c().autor.idUsuario]"
            ><strong>{{ c().autor.nombreCompleto }}</strong></a
          ><small
            >{{ c().fechaComentario | date: 'dd/MM/yyyy · HH:mm' }}
            @if (c().editado) {
              · Editado
            }
          </small>
        </div>
      </header>
      @if (editing()) {
        <social-modal title="Editar comentario" [busy]="busy()" (closed)="editing.set(false)">
          <form (submit)="$event.preventDefault(); save()">
            <social-text-editor
              [mentions]="c().menciones"
              [value]="text()"
              (valueChange)="text.set($event)"
              [disabled]="busy()"
              label="Editar comentario"
            />
            <div class="actions">
              <button tuiButton size="s" type="submit" [disabled]="busy() || !text().trim()">
                Guardar</button
              ><button
                tuiButton
                appearance="flat"
                size="s"
                type="button"
                [disabled]="busy()"
                (click)="editing.set(false)"
              >
                <social-icon name="x" />Cancelar
              </button>
            </div>
            @if (error()) {
              <p class="error" role="alert">{{ error() }}</p>
            }
          </form></social-modal
        >
      } @else {
        <div class="comment-text">
          <social-text [text]="c().contenido" [mentions]="c().menciones" />
        </div>
      }
    </div>
    @if (!c().eliminado) {
      <div class="attachments">
        @for (url of c().adjuntosUrls; track url) {
          <a [href]="url" target="_blank" rel="noopener noreferrer"
            ><img [src]="url" alt="Adjunto del comentario" loading="lazy"
          /></a>
        }
      </div>
      <div class="comment-tools">
        <social-reactions
          [id]="c().idComentario"
          [comentario]="true"
          [value]="c().reacciones"
          (changed)="reactionChanged($event)"
        />
        <div class="actions">
          <button tuiButton appearance="flat" size="xs" type="button" (click)="requestReply()">
            <social-icon name="reply" />Responder
          </button>
          @if (c().puedoEditar) {
            <button
              tuiButton
              appearance="flat"
              size="xs"
              type="button"
              (click)="text.set(c().contenido); editing.set(true)"
            >
              <social-icon name="pencil" />Editar
            </button>
          }
          @if (c().puedoEliminar) {
            <button
              tuiButton
              appearance="flat"
              size="xs"
              type="button"
              [disabled]="busy()"
              (click)="remove()"
            >
              <social-icon name="trash-2" />Eliminar
            </button>
          }
          <button
            tuiButton
            appearance="flat"
            size="xs"
            type="button"
            (click)="report.set(!report())"
          >
            <social-icon name="flag" />Reportar
          </button>
          @if (admin()) {
            <button
              tuiButton
              appearance="flat"
              size="xs"
              type="button"
              [disabled]="busy()"
              (click)="hide()"
            >
              <social-icon name="eye-off" />Ocultar
            </button>
          }
        </div>
      </div>
    }
    @if (error()) {
      <p class="error" role="alert">{{ error() }}</p>
    }
    @if (notice()) {
      <p role="status">{{ notice() }}</p>
    }
    @if (report()) {
      <social-modal title="Reportar comentario" [busy]="childBusy()" (closed)="report.set(false)">
        <social-report
          (pending)="childBusy.set($event)"
          [post]="c().idPost"
          [comentario]="c().idComentario"
          (cancel)="report.set(false)"
          (sent)="report.set(false); notice.set('Reporte enviado.')"
      /></social-modal>
    }
    @if (reply()) {
      <div class="panel" aria-label="Responder comentario">
        <p>
          Responder a <strong>{{ c().autor.nombreCompleto }}</strong>
        </p>
        <social-comment-editor
          (pending)="childBusy.set($event)"
          [post]="c().idPost"
          [padre]="c().idComentarioPadre ?? c().idComentario"
          [initialText]="
            c().idComentarioPadre && c().autor.username ? '@' + c().autor.username + ' ' : ''
          "
          (cancel)="reply.set(false)"
          (sent)="reply.set(false); changed.emit()"
        />
      </div>
    }
    @for (child of c().respuestas; track child.idComentario) {
      <div class="reply">
        <social-comment
          [comentario]="child"
          [replyInComposer]="replyInComposer()"
          (replyRequested)="replyRequested.emit($event)"
          (pending)="setChildPending(child.idComentario, $event)"
          (changed)="changed.emit()"
        />
      </div>
    }
  </article>`,
})
export class SocialComment implements OnDestroy {
  ngOnDestroy() {
    this.pending.emit(false);
  }
  readonly comentario = input.required<Comentario>();
  readonly changed = output<void>();
  readonly replyInComposer = input(false);
  readonly replyRequested = output<Comentario>();
  protected requestReply() {
    if (this.replyInComposer()) this.replyRequested.emit(this.c());
    else this.reply.set(!this.reply());
  }
  readonly pending = output<boolean>();
  private readonly pendingChildren = signal<Set<number>>(new Set());
  protected setChildPending(id: number, busy: boolean) {
    this.pendingChildren.update((ids) => {
      const next = new Set(ids);
      busy ? next.add(id) : next.delete(id);
      return next;
    });
  }
  private readonly api = inject(SocialService);
  private readonly auth = inject(AuthService);
  private readonly confirm = inject(ConfirmService);
  private readonly destroy = inject(DestroyRef);
  private readonly patch = signal<Partial<Comentario>>({});
  protected readonly c = computed(() => ({ ...this.comentario(), ...this.patch() }));
  protected readonly avatar = urlAvatarFoto;
  protected readonly editing = signal(false);
  protected readonly childBusy = signal(false);
  protected readonly reply = signal(false);
  protected readonly report = signal(false);
  protected readonly busy = signal(false);
  protected readonly error = signal('');
  protected readonly notice = signal('');
  protected readonly text = signal('');
  constructor() {
    effect(() =>
      this.pending.emit(this.busy() || this.childBusy() || this.pendingChildren().size > 0),
    );
    effect(() => {
      this.comentario();
      this.patch.set({});
    });
  }
  protected admin() {
    return this.auth.tieneRol('Administrador');
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
  protected reactionChanged(r: Reacciones) {
    this.patch.update((p) => ({ ...p, reacciones: r }));
  }
  protected save() {
    if (this.busy() || !this.text().trim() || this.text().length > 1000) return;
    this.busy.set(true);
    this.error.set('');
    this.api
      .editarComentario(this.c().idPost, this.c().idComentario, this.text().trim())
      .pipe(
        takeUntilDestroyed(this.destroy),
        finalize(() => this.busy.set(false)),
      )
      .subscribe({
        next: (r) => {
          this.patch.set({ ...r, respuestas: this.c().respuestas });
          this.editing.set(false);
        },
        error: (e) => this.error.set(errorSocial(e)),
      });
  }
  protected remove() {
    this.confirm
      .confirmar('¿Eliminar este comentario y sus respuestas?', 'Eliminar')
      .pipe(
        filter(Boolean),
        switchMap(() => this.api.eliminarComentario(this.c().idPost, this.c().idComentario)),
        takeUntilDestroyed(this.destroy),
      )
      .subscribe({ next: () => this.changed.emit(), error: (e) => this.error.set(errorSocial(e)) });
  }
  protected hide() {
    this.confirm
      .confirmar('¿Ocultar este comentario?', 'Ocultar')
      .pipe(
        filter(Boolean),
        switchMap(() => this.api.moderar(this.c().idComentario, false, true)),
        takeUntilDestroyed(this.destroy),
      )
      .subscribe({ next: () => this.changed.emit(), error: (e) => this.error.set(errorSocial(e)) });
  }
}
