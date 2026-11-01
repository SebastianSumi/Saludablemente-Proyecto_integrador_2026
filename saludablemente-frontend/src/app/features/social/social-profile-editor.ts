import {
  Component,
  OnInit,
  DestroyRef,
  inject,
  input,
  output,
  signal,
  computed,
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { TuiButton } from '@taiga-ui/core';
import { finalize } from 'rxjs';
import { Avatar, urlAvatarFoto } from '../../shared/components/avatar/avatar';
import { SocialIcon } from './social-icon';
import { SocialService } from './social-service';
import { SocialCurrentAuthor } from './social-current-author';
import { PerfilCabecera } from './social.model';
import { adjuntoValido, errorSocial } from './social.utils';
@Component({
  selector: 'social-profile-editor',
  imports: [TuiButton, Avatar, SocialIcon],
  styleUrls: ['./social.css', './social-profile.css', './social-profile-publicaciones.css'],
  template: `<form class="fields" (submit)="$event.preventDefault(); save()">
    @if (!onlyPresentation()) {
      <div class="profile-edit-cover">
        @if (current().portadaUrl) {
          <img [src]="current().portadaUrl" alt="Tu portada actual" />
        }
        <button
          tuiButton
          type="button"
          size="s"
          appearance="primary"
          [disabled]="busy()"
          (click)="cover.click()"
        >
          <social-icon name="image" />Cambiar portada
        </button>
      </div>
      @if (!coverOnly()) {
        <div class="identity">
          <app-avatar
            [foto]="avatar(current().autor.fotoUrl)"
            [iniciales]="current().autor.nombreCompleto.slice(0, 1)"
            size="l"
          />
          <button
            tuiButton
            type="button"
            size="s"
            appearance="secondary"
            [disabled]="busy()"
            (click)="photo.click()"
          >
            <social-icon name="camera" />Cambiar foto
          </button>
        </div>
      }
      <input
        #photo
        hidden
        type="file"
        accept="image/jpeg,image/png,image/webp"
        [disabled]="busy()"
        (change)="upload($event, false)"
      />
      <input
        #cover
        hidden
        type="file"
        accept="image/jpeg,image/png,image/webp"
        [disabled]="busy()"
        (change)="upload($event, true)"
      />
      <small
        >JPEG, PNG o WebP. Foto hasta 2 MiB; portada hasta 5 MiB. Las imágenes se guardan al
        subirlas.</small
      >
    }
    @if (!coverOnly()) {
      <label class="field"
        >Presentación<textarea
          aria-label="Presentación"
          rows="3"
          maxlength="300"
          [value]="text()"
          (input)="text.set($any($event.target).value)"
          [disabled]="busy()"
          placeholder="Cuenta algo sobre ti y lo que te inspira…"
        ></textarea>
      </label>
      <small>{{ text().length }} / 300</small>
      <p class="muted">Nombre, rol y equipo se gestionan en Personal.</p>
    }
    @if (error()) {
      <p class="error" role="alert">{{ error() }}</p>
    }
    @if (notice()) {
      <p class="notice" role="status">{{ notice() }}</p>
    }
    <div class="actions">
      @if (!coverOnly()) {
        <button tuiButton type="submit" size="s" [disabled]="busy()">
          <social-icon name="check" />Guardar cambios
        </button>
      }
      <button
        tuiButton
        appearance="flat"
        type="button"
        size="s"
        [disabled]="busy()"
        (click)="cancel.emit()"
      >
        Cerrar
      </button>
    </div>
  </form>`,
})
export class SocialProfileEditor implements OnInit {
  readonly perfil = input.required<PerfilCabecera>();
  readonly onlyPresentation = input(false);
  readonly coverOnly = input(false);
  readonly updated = output<PerfilCabecera>();
  readonly saved = output<void>();
  readonly cancel = output<void>();
  readonly pending = output<boolean>();
  private readonly api = inject(SocialService);
  private readonly destroy = inject(DestroyRef);
  private readonly author = inject(SocialCurrentAuthor);
  private readonly patch = signal<PerfilCabecera | null>(null);
  protected readonly current = computed(() => this.patch() || this.perfil());
  protected readonly text = signal('');
  protected readonly busy = signal(false);
  protected readonly error = signal('');
  protected readonly notice = signal('');
  protected readonly avatar = urlAvatarFoto;
  ngOnInit() {
    this.text.set(this.perfil().presentacion || '');
  }
  private working(busy: boolean) {
    this.busy.set(busy);
    this.pending.emit(busy);
  }
  protected upload(event: Event, portada: boolean) {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    input.value = '';
    if (!file || this.busy()) return;
    if (!adjuntoValido(file) || (!portada && file.size > 2 * 1024 * 1024)) {
      this.error.set(
        portada
          ? 'Selecciona JPEG, PNG o WebP de hasta 5 MiB.'
          : 'Selecciona JPEG, PNG o WebP de hasta 2 MiB.',
      );
      return;
    }
    this.working(true);
    this.error.set('');
    this.notice.set('');
    this.api
      .subirFotoPerfil(file, portada)
      .pipe(
        takeUntilDestroyed(this.destroy),
        finalize(() => this.working(false)),
      )
      .subscribe({
        next: (p) => {
          this.patch.set(p);
          this.updated.emit(p);
          this.author.refresh();
          this.notice.set(portada ? 'Portada actualizada.' : 'Foto actualizada.');
        },
        error: (e) => this.error.set(errorSocial(e)),
      });
  }
  protected save() {
    if (this.busy() || this.text().length > 300) return;
    this.working(true);
    this.error.set('');
    this.api
      .editarMiPresentacion(this.text().trim())
      .pipe(
        takeUntilDestroyed(this.destroy),
        finalize(() => this.working(false)),
      )
      .subscribe({
        next: (p) => {
          this.updated.emit(p);
          this.saved.emit();
        },
        error: (e) => this.error.set(errorSocial(e)),
      });
  }
}
