import { AsyncPipe } from '@angular/common';
import { Avatar, urlAvatarFoto } from '../../shared/components/avatar/avatar';
import { SocialCurrentAuthor } from './social-current-author';
import { SocialIcon } from './social-icon';
import {
  effect,
  OnInit,
  OnDestroy,
  Component,
  DestroyRef,
  inject,
  input,
  output,
  signal,
} from '@angular/core';
import { TuiButton } from '@taiga-ui/core';
import { concatMap, finalize, from } from 'rxjs';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { SocialService } from './social-service';
import { SocialTextEditor } from './social-text-editor';
import { adjuntoValido, errorSocial } from './social.utils';
let nextComposerId = 0;
@Component({
  selector: 'social-comment-editor',
  imports: [AsyncPipe, Avatar, SocialIcon, TuiButton, SocialTextEditor],
  styleUrls: ['./social.css', './social-composer.css'],
  template: `<form class="composer" (submit)="$event.preventDefault(); send()">
    <div class="composer-main">
      <social-text-editor
        [compact]="true"
        [value]="text()"
        (valueChange)="text.set($event)"
        [disabled]="busy() || uploading()"
        [label]="padre() ? 'Escribe una respuesta' : 'Escribe un comentario'"
      >
        <div editor-submit class="composer-send">
          @if (padre()) {
            <button
              tuiButton
              appearance="flat"
              size="xs"
              type="button"
              [disabled]="busy() || uploading()"
              (click)="cancel.emit()"
            >
              <social-icon name="x" />Cancelar
            </button>
          }
          <button
            tuiButton
            size="xs"
            type="submit"
            [disabled]="busy() || uploading() || !text().trim()"
          >
            <social-icon name="send" />{{
              busy() ? 'Enviando…' : padre() ? 'Responder' : 'Comentar'
            }}
          </button>
        </div>
        <button
          editor-tools
          tuiButton
          appearance="flat"
          size="s"
          type="button"
          [disabled]="busy() || uploading() || urls().length >= 4"
          (click)="file.click()"
          [attr.aria-describedby]="helpId"
        >
          <social-icon name="paperclip" />Añadir archivo
        </button>
      </social-text-editor>
      <input
        #file
        hidden
        type="file"
        accept="image/jpeg,image/png,image/webp"
        multiple
        [disabled]="busy() || uploading() || urls().length >= 4"
        (change)="upload($event)"
      />
      <small class="attachment-hint" [id]="helpId"
        >Hasta 4 imágenes · JPEG, PNG o WebP · 5 MiB por archivo</small
      >
      @if (uploading()) {
        <p role="status">Subiendo adjuntos…</p>
      }
      @if (urls().length) {
        <div class="composer-attachments">
          @for (url of urls(); track url) {
            <div>
              <img [src]="url" alt="Adjunto seleccionado" /><button
                tuiButton
                appearance="flat"
                size="xs"
                type="button"
                [disabled]="busy() || uploading()"
                (click)="remove(url)"
              >
                Retirar
              </button>
            </div>
          }
        </div>
      }
      @if (error()) {
        <p class="error" role="alert">{{ error() }}</p>
      }
    </div>
    @let author = currentAuthor.autor$ | async;
    <app-avatar
      class="composer-avatar"
      [foto]="avatar(author?.fotoUrl)"
      [iniciales]="initials(author?.nombreCompleto)"
      size="s"
      [attr.title]="author?.nombreCompleto || 'Tu perfil'"
    />
  </form>`,
})
export class SocialCommentEditor implements OnInit, OnDestroy {
  ngOnDestroy() {
    this.pending.emit(false);
  }
  protected readonly helpId = `social-files-help-${++nextComposerId}`;
  protected readonly currentAuthor = inject(SocialCurrentAuthor);
  protected readonly avatar = urlAvatarFoto;
  protected initials(name?: string) {
    return (
      name
        ?.trim()
        .split(/\s+/)
        .slice(0, 2)
        .map((n) => n[0])
        .join('')
        .toUpperCase() || '?'
    );
  }
  readonly initialText = input('');
  ngOnInit() {
    this.text.set(this.initialText());
  }
  readonly post = input.required<number>();
  readonly padre = input<number | null>(null);
  readonly sent = output<void>();
  readonly cancel = output<void>();
  readonly pending = output<boolean>();
  private readonly api = inject(SocialService);
  private readonly destroy = inject(DestroyRef);
  protected readonly text = signal('');
  protected readonly urls = signal<string[]>([]);
  protected readonly busy = signal(false);
  protected readonly uploading = signal(false);
  protected readonly error = signal('');
  private readonly pendingEffect = effect(() => this.pending.emit(this.busy() || this.uploading()));
  protected upload(event: Event) {
    const input = event.target as HTMLInputElement;
    const files = Array.from(input.files ?? []);
    input.value = '';
    if (!files.length || this.uploading()) return;
    if (this.urls().length + files.length > 4) {
      this.error.set('Solo puedes adjuntar cuatro imágenes.');
      return;
    }
    if (files.some((f) => !adjuntoValido(f))) {
      this.error.set(
        'Selecciona imágenes JPEG, PNG o WebP de hasta 5 MiB, con su extensión correspondiente.',
      );
      return;
    }
    this.error.set('');
    this.uploading.set(true);
    from(files)
      .pipe(
        concatMap((file) => this.api.subirAdjunto(this.post(), file)),
        takeUntilDestroyed(this.destroy),
        finalize(() => this.uploading.set(false)),
      )
      .subscribe({
        next: (a) => this.urls.update((list) => [...list, a.url]),
        error: (e) => this.error.set(errorSocial(e)),
      });
  }
  protected remove(url: string) {
    this.urls.update((list) => list.filter((x) => x !== url));
  }
  protected send() {
    if (this.busy() || this.uploading() || !this.text().trim() || this.text().length > 1000) return;
    this.busy.set(true);
    this.error.set('');
    this.api
      .comentar(this.post(), {
        contenido: this.text().trim(),
        idComentarioPadre: this.padre(),
        adjuntosUrls: this.urls(),
      })
      .pipe(
        takeUntilDestroyed(this.destroy),
        finalize(() => this.busy.set(false)),
      )
      .subscribe({
        next: () => {
          this.text.set('');
          this.urls.set([]);
          this.sent.emit();
        },
        error: (e) => this.error.set(errorSocial(e)),
      });
  }
}
