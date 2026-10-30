import { Component, DestroyRef, inject, input, output, signal } from '@angular/core';
import { TuiButton } from '@taiga-ui/core';
import { concatMap, finalize, from } from 'rxjs';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { SocialService } from './social-service';
import { SocialTextEditor } from './social-text-editor';
import { adjuntoValido, errorSocial } from './social.utils';
@Component({
  selector: 'social-comment-editor',
  imports: [TuiButton, SocialTextEditor],
  styleUrl: './social.css',
  template: ` <form class="fields" (submit)="$event.preventDefault(); send()">
    <social-text-editor
      [value]="text()"
      (valueChange)="text.set($event)"
      [disabled]="busy() || uploading()"
      [label]="padre() ? 'Escribe una respuesta' : 'Escribe un comentario'"
    />
    <label class="field"
      >Adjuntar imágenes<input
        type="file"
        accept="image/jpeg,image/png,image/webp"
        multiple
        [disabled]="busy() || uploading() || urls().length >= 4"
        (change)="upload($event)" /></label
    ><small>Hasta 4 imágenes JPEG, PNG o WebP; máximo 5 MiB cada una.</small>
    @if (uploading()) {
      <p role="status">Subiendo adjuntos…</p>
    }
    <div class="attachments">
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
    @if (error()) {
      <p class="error" role="alert">{{ error() }}</p>
    }
    <div class="actions">
      <button tuiButton size="s" type="submit" [disabled]="busy() || uploading() || !text().trim()">
        {{ busy() ? 'Enviando…' : padre() ? 'Responder' : 'Comentar' }}
      </button>
      @if (padre()) {
        <button
          tuiButton
          appearance="flat"
          size="s"
          type="button"
          [disabled]="busy() || uploading()"
          (click)="cancel.emit()"
        >
          Cancelar
        </button>
      }
    </div>
  </form>`,
})
export class SocialCommentEditor {
  readonly post = input.required<number>();
  readonly padre = input<number | null>(null);
  readonly sent = output<void>();
  readonly cancel = output<void>();
  private readonly api = inject(SocialService);
  private readonly destroy = inject(DestroyRef);
  protected readonly text = signal('');
  protected readonly urls = signal<string[]>([]);
  protected readonly busy = signal(false);
  protected readonly uploading = signal(false);
  protected readonly error = signal('');
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
