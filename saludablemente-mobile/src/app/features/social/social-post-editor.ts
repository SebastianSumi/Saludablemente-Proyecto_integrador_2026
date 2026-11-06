import { SocialIcon } from './social-icon';
import {
  effect,
  Component,
  DestroyRef,
  inject,
  input,
  OnInit,
  output,
  signal,
} from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { TuiButton } from '@taiga-ui/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { finalize } from 'rxjs';
import { TeamService } from '../admin/teams/team-service';
import { AuthService } from '../../core/auth/auth-service';
import { Team } from '../admin/teams/team.model';
import { SocialService } from './social-service';
import { SocialTextEditor } from './social-text-editor';
import { PostDetalle } from './social.model';
import { errorSocial, imagenesValidas, urlsImagenes } from './social.utils';
@Component({
  selector: 'social-post-editor',
  imports: [SocialIcon, ReactiveFormsModule, TuiButton, SocialTextEditor],
  styleUrl: './social.css',
  template: ` <section class="fields">
    @if (loading()) {
      <p role="status">Cargando publicación…</p>
    }
    @if (error()) {
      <p class="error" role="alert">{{ error() }}</p>
    }
    @if (!loading() && ready()) {
      <form [formGroup]="form" (ngSubmit)="save()" class="fields">
        @if (!share()) {
          <label
            >Título<input
              formControlName="titulo"
              maxlength="150"
              placeholder="¿Qué te gustaría compartir?"
          /></label>
        }
        <div class="field">
          <span>{{ share() ? 'Texto del share' : 'Resumen' }}</span
          ><social-text-editor
            [mentions]="mentions()"
            [value]="form.controls.resumen.value"
            (valueChange)="form.controls.resumen.setValue($event)"
            [max]="500"
            label="Resumen de la publicación"
            [disabled]="busy()"
          />
        </div>
        @if (!share() && admin()) {
          <div class="field">
            <span>Contenido de la publicación</span>
            <textarea
              formControlName="cuerpo"
              rows="8"
              maxlength="10000"
              placeholder="Texto amplio de la publicación (solo administradores)"
            ></textarea>
            <small>Puedes usar #hashtags y @menciones. Máximo 10 000 caracteres.</small>
          </div>
        }
        @if (!share()) {
          <label
            >Imágenes · una URL por línea<textarea              formControlName="imagenes"
              rows="2"
              placeholder="https://…"
            ></textarea></label
          ><small
            >Usa enlaces HTTP o HTTPS de imágenes. Puedes publicar solo una imagen o incluir título
            y resumen.</small
          >
        }
        @if (!postId()) {
          <label
            >Publicar como<select formControlName="equipo">
              <option [ngValue]="null">A título personal</option>
              @if (team(); as t) {
                <option [ngValue]="t.id">{{ t.nombre }}</option>
              }
            </select></label
          >
          @if (teamError()) {
            <small role="status"
              >No pudimos consultar tu equipo. Puedes publicar personalmente o reintentar.</small
            ><button tuiButton type="button" appearance="flat" size="s" (click)="loadTeam()">
              Consultar mi equipo
            </button>
          }
        }
        <div class="actions">
          <button tuiButton type="submit" size="s" [disabled]="busy() || form.invalid">
            <social-icon name="send" />{{
              busy() ? 'Guardando…' : postId() ? 'Guardar cambios' : 'Publicar'
            }}</button
          ><button
            tuiButton
            type="button"
            appearance="flat"
            size="s"
            [disabled]="busy()"
            (click)="cancel.emit()"
          >
            <social-icon name="x" />Cancelar
          </button>
        </div>
      </form>
    }
  </section>`,
})
export class SocialPostEditor implements OnInit {
  readonly postId = input<number | null>(null);
  readonly saved = output<PostDetalle>();
  readonly cancel = output<void>();
  readonly pending = output<boolean>();
  private readonly api = inject(SocialService);
  private readonly teams = inject(TeamService);
  private readonly auth = inject(AuthService);
  private readonly destroy = inject(DestroyRef);
  protected readonly team = signal<Team | null>(null);
  protected readonly teamError = signal(false);
  protected readonly loading = signal(false);
  protected readonly ready = signal(true);
  protected readonly busy = signal(false);
  protected readonly error = signal('');
  private readonly pendingEffect = effect(() => this.pending.emit(this.busy()));
  protected readonly share = signal(false);
  protected admin() {
    return this.auth.tieneRol('Administrador');
  }
  protected readonly mentions = signal<import('./social.model').Mencion[]>([]);
  protected readonly form = new FormGroup({
    titulo: new FormControl('', { nonNullable: true, validators: [Validators.maxLength(150)] }),
    resumen: new FormControl('', { nonNullable: true, validators: [Validators.maxLength(500)] }),
    cuerpo: new FormControl('', { nonNullable: true, validators: [Validators.maxLength(10000)] }),
    imagenes: new FormControl('', { nonNullable: true }),
    equipo: new FormControl<number | null>(null),
  });
  ngOnInit() {
    const id = this.postId();
    if (!id) {
      this.loadTeam();
      return;
    }
    this.loading.set(true);
    this.ready.set(false);
    this.api
      .detalle(id)
      .pipe(
        takeUntilDestroyed(this.destroy),
        finalize(() => this.loading.set(false)),
      )
      .subscribe({
        next: (p) => {
          if (!p.puedoEditar) {
            this.error.set('No puedes editar esta publicación.');
            return;
          }
          this.share.set(p.tipoContenido === 'SHARE');
          this.mentions.set(p.menciones);
          this.form.patchValue({
            titulo: p.titulo ?? '',
            resumen: p.resumen ?? '',
            cuerpo: p.cuerpo ?? '',
            imagenes: p.imagenes.join('\n'),
          });
          this.ready.set(true);
        },
        error: (e) => this.error.set(errorSocial(e)),
      });
  }
  protected loadTeam() {
    this.teamError.set(false);
    this.teams
      .miEquipo()
      .pipe(takeUntilDestroyed(this.destroy))
      .subscribe({
        next: (x) => this.team.set(x.activo ? x : null),
        error: (e) => this.teamError.set(e.status !== 404),
      });
  }
  protected save() {
    if (this.busy() || this.form.invalid) return;
    const v = this.form.getRawValue();
    const urls = urlsImagenes(v.imagenes);
    if (!this.share() && !urls.length && (!v.titulo.trim() || !v.resumen.trim())) {
      this.error.set('Incluye título y resumen, o al menos una imagen.');
      return;
    }
    if (!imagenesValidas(urls)) {
      this.error.set('Cada imagen debe tener una URL HTTP o HTTPS válida de hasta 500 caracteres.');
      return;
    }
    this.error.set('');
    this.busy.set(true);
    const body = {
      titulo: v.titulo.trim() || null,
      resumen: v.resumen.trim() || null,
      cuerpo: this.admin() ? (v.cuerpo.trim() || null) : null,
      imagenesUrls: urls,
    };
    const id = this.postId();
    const operation = id
      ? this.api.editar(id, body)
      : this.api.crear({ ...body, idEquipoAutor: v.equipo });
    operation
      .pipe(
        takeUntilDestroyed(this.destroy),
        finalize(() => this.busy.set(false)),
      )
      .subscribe({ next: (p) => this.saved.emit(p), error: (e) => this.error.set(errorSocial(e)) });
  }
}
