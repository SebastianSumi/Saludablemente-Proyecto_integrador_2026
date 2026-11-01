import { SocialIcon } from './social-icon';
import { effect, Component, DestroyRef, inject, input, output, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { TuiButton } from '@taiga-ui/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { finalize } from 'rxjs';
import { SocialService } from './social-service';
import { MotivoReporte } from './social.model';
import { errorSocial } from './social.utils';
@Component({
  selector: 'social-report',
  imports: [SocialIcon, FormsModule, TuiButton],
  styleUrl: './social.css',
  template: ` <form class="fields" (ngSubmit)="send()">
    <label
      >Motivo<select [(ngModel)]="motivo" name="motivo">
        <option value="SPAM">Spam</option>
        <option value="OFENSIVO">Contenido ofensivo</option>
        <option value="ACOSO">Acoso</option>
        <option value="FALSO">Información falsa</option>
        <option value="OTRO">Otro</option>
      </select></label
    ><label
      >Detalles opcionales<textarea
        [(ngModel)]="detalle"
        name="detalle"
        maxlength="500"
        rows="3"
      ></textarea>
    </label>
    @if (error()) {
      <p class="error" role="alert">{{ error() }}</p>
    }
    <div class="actions">
      <button tuiButton size="s" type="submit" [disabled]="busy()">
        {{ busy() ? 'Enviando…' : 'Enviar reporte' }}</button
      ><button
        tuiButton
        type="button"
        size="s"
        appearance="flat"
        [disabled]="busy()"
        (click)="cancel.emit()"
      >
        <social-icon name="x" />Cancelar
      </button>
    </div>
  </form>`,
})
export class SocialReport {
  readonly post = input.required<number>();
  readonly comentario = input<number>();
  readonly sent = output<void>();
  readonly cancel = output<void>();
  readonly pending = output<boolean>();
  private readonly api = inject(SocialService);
  private readonly destroy = inject(DestroyRef);
  protected motivo: MotivoReporte = 'SPAM';
  protected detalle = '';
  protected readonly busy = signal(false);
  protected readonly error = signal('');
  private readonly pendingEffect = effect(() => this.pending.emit(this.busy()));
  protected send() {
    if (this.busy()) return;
    this.busy.set(true);
    this.error.set('');
    this.api
      .reportar(this.post(), this.motivo, this.detalle.trim() || null, this.comentario())
      .pipe(
        takeUntilDestroyed(this.destroy),
        finalize(() => this.busy.set(false)),
      )
      .subscribe({ next: () => this.sent.emit(), error: (e) => this.error.set(errorSocial(e)) });
  }
}
