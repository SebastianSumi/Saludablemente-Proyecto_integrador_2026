import { Component, DestroyRef, inject, input, output, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { TuiButton } from '@taiga-ui/core';
import { switchMap, finalize } from 'rxjs';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { SocialService } from './social-service';
import { Page, Reacciones, ReaccionDetalle, TipoReaccion } from './social.model';
import { errorSocial } from './social.utils';
import { SocialPager } from './social-pager';
export const REACCIONES: readonly { tipo: TipoReaccion; emoji: string; nombre: string }[] = [
  { tipo: 'LIKE', emoji: '👍', nombre: 'Me gusta' },
  { tipo: 'LOVE', emoji: '❤️', nombre: 'Me encanta' },
  { tipo: 'HAHA', emoji: '😄', nombre: 'Me divierte' },
  { tipo: 'WOW', emoji: '😮', nombre: 'Me sorprende' },
  { tipo: 'SAD', emoji: '😢', nombre: 'Me entristece' },
  { tipo: 'APLAUSO', emoji: '👏', nombre: 'Aplausos' },
  { tipo: 'ENOJADO', emoji: '😠', nombre: 'Me enoja' },
];
@Component({
  selector: 'social-reactions',
  imports: [TuiButton, FormsModule, RouterLink, DatePipe, SocialPager],
  styleUrl: './social.css',
  template: ` <div class="actions" aria-label="Reacciones">
      @for (r of options; track r.tipo) {
        <button
          tuiButton
          type="button"
          appearance="flat"
          size="xs"
          [attr.aria-label]="r.nombre"
          [title]="r.nombre"
          [attr.aria-pressed]="value().reaccionDelUsuario === r.tipo"
          [disabled]="busy()"
          (click)="react(r.tipo)"
        >
          {{ r.emoji }}
          @if (value().porTipo[r.tipo]) {
            <span>{{ value().porTipo[r.tipo] }}</span>
          }
          @if (value().reaccionDelUsuario === r.tipo) {
            <span aria-hidden="true">✓</span>
          }
        </button>
      }
      <button tuiButton type="button" appearance="flat" size="xs" (click)="toggle()">
        {{ value().total }} reacciones · Ver personas
      </button>
    </div>
    @if (error()) {
      <p class="error" role="alert">{{ error() }}</p>
    }
    @if (open()) {
      <section class="panel">
        <h3>Personas que reaccionaron</h3>
        <label class="field"
          >Filtrar<select [(ngModel)]="tipo" (ngModelChange)="load(0)">
            <option [ngValue]="null">Todas</option>
            @for (r of options; track r.tipo) {
              <option [ngValue]="r.tipo">{{ r.nombre }}</option>
            }
          </select></label
        >
        @if (loading()) {
          <p role="status">Cargando…</p>
        } @else if (people(); as result) {
          @for (r of result.content; track r.idUsuario) {
            <p>
              <a [routerLink]="['/social/usuarios', r.idUsuario]">{{ r.autor.nombreCompleto }}</a> ·
              {{ label(r.tipo) }} <small>{{ r.fechaReaccion | date: 'short' }}</small>
            </p>
          } @empty {
            <p class="muted">Todavía no hay reacciones.</p>
          }
          <social-pager
            [page]="result.number"
            [total]="result.totalPages"
            (change)="load($event)"
          />
        }
      </section>
    }`,
})
export class SocialReactions {
  readonly id = input.required<number>();
  readonly comentario = input(false);
  readonly value = input.required<Reacciones>();
  readonly changed = output<Reacciones>();
  protected readonly options = REACCIONES;
  private readonly api = inject(SocialService);
  private readonly destroy = inject(DestroyRef);
  protected readonly busy = signal(false);
  protected readonly loading = signal(false);
  protected readonly open = signal(false);
  protected readonly error = signal('');
  protected readonly people = signal<Page<ReaccionDetalle> | null>(null);
  protected tipo: TipoReaccion | null = null;
  private request = 0;
  protected label(tipo: TipoReaccion) {
    return REACCIONES.find((x) => x.tipo === tipo)?.nombre;
  }
  protected react(tipo: TipoReaccion) {
    if (this.busy()) return;
    this.busy.set(true);
    this.error.set('');
    this.api
      .reaccionar(
        this.id(),
        this.value().reaccionDelUsuario === tipo ? null : tipo,
        this.comentario(),
      )
      .pipe(
        switchMap(() => this.api.reacciones(this.id(), this.comentario())),
        takeUntilDestroyed(this.destroy),
        finalize(() => this.busy.set(false)),
      )
      .subscribe({
        next: (r) => {
          this.changed.emit(r);
          if (this.open()) this.load(0);
        },
        error: (e) => this.error.set(errorSocial(e)),
      });
  }
  protected toggle() {
    this.open.set(!this.open());
    if (this.open()) this.load(0);
  }
  protected load(page: number) {
    const request = ++this.request;
    this.loading.set(true);
    this.error.set('');
    this.api
      .quienReacciono(this.id(), this.comentario(), this.tipo, page)
      .pipe(takeUntilDestroyed(this.destroy))
      .subscribe({
        next: (r) => {
          if (request === this.request) {
            this.people.set(r);
            this.loading.set(false);
          }
        },
        error: (e) => {
          if (request === this.request) {
            this.error.set(errorSocial(e));
            this.loading.set(false);
          }
        },
      });
  }
}
