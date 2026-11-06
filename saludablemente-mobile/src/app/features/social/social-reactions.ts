import { Avatar, urlAvatarFoto } from '../../shared/components/avatar/avatar';
import { SocialIcon } from './social-icon';
import { SocialModal } from './social-modal';
import { Component, computed, DestroyRef, inject, input, output, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { TuiButton, TuiDropdown } from '@taiga-ui/core';
import { switchMap, finalize } from 'rxjs';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { SocialService } from './social-service';
import { Page, Reacciones, ReaccionDetalle, TipoReaccion } from './social.model';
import { errorSocial } from './social.utils';
import { SocialPager } from './social-pager';
export const REACCIONES: readonly { tipo: TipoReaccion; icon: string; nombre: string }[] = [
  { tipo: 'LIKE', icon: 'thumbs-up', nombre: 'Me gusta' },
  { tipo: 'LOVE', icon: 'heart', nombre: 'Me encanta' },
  { tipo: 'HAHA', icon: 'laugh', nombre: 'Me divierte' },
  { tipo: 'WOW', icon: 'surprised', nombre: 'Me sorprende' },
  { tipo: 'SAD', icon: 'frown', nombre: 'Me entristece' },
  { tipo: 'APLAUSO', icon: 'applause', nombre: 'Aplausos' },
  { tipo: 'ENOJADO', icon: 'angry', nombre: 'Me enoja' },
];
@Component({
  selector: 'social-reactions',
  imports: [
    Avatar,
    SocialIcon,
    TuiDropdown,
    SocialModal,
    TuiButton,
    RouterLink,
    DatePipe,
    SocialPager,
  ],
  styleUrls: ['./social.css', './social-reactions.css'],
  template: `<div class="reaction-bar">
      <div class="reaction-control" [attr.data-reaction]="value().reaccionDelUsuario">
        <button
          tuiButton
          type="button"
          appearance="flat"
          size="s"
          class="like-button"
          [disabled]="busy()"
          [attr.aria-busy]="busy()"
          [attr.aria-pressed]="!!value().reaccionDelUsuario"
          [attr.aria-label]="
            value().reaccionDelUsuario ? label(value().reaccionDelUsuario!) : 'Me gusta'
          "
          [title]="
            value().reaccionDelUsuario
              ? label(value().reaccionDelUsuario!) || 'Reaccionar'
              : 'Me gusta'
          "
          (click)="react(value().reaccionDelUsuario || 'LIKE')"
        >
          <social-icon
            [name]="icon(value().reaccionDelUsuario)"
            [filled]="!!value().reaccionDelUsuario"
          />
        </button>
        <button
          tuiButton
          type="button"
          appearance="flat"
          size="s"
          class="reaction-more"
          [disabled]="busy()"
          aria-label="Elegir reacción"
          [attr.aria-expanded]="pickerOpen()"
          [tuiDropdown]="reactionPicker"
          [tuiDropdownOpen]="pickerOpen()"
          (tuiDropdownOpenChange)="pickerOpen.set($event)"
          (click)="open.set(false)"
        >
          <social-icon name="chevron-down" /><span class="sr-only">Elegir reacción</span>
        </button>
      </div>
      <button
        tuiButton
        type="button"
        appearance="flat"
        size="s"
        class="reaction-count"
        (click)="toggle()"
        aria-label="Ver personas que reaccionaron"
      >
        {{ value().total }}<span class="sr-only"> reacciones · Ver personas</span>
      </button>
    </div>
    <ng-template #reactionPicker
      ><div
        class="reaction-picker reaction-options"
        role="toolbar"
        aria-label="Elegir reacción"
        (keydown)="navigatePicker($event)"
      >
        @for (r of options; track r.tipo) {
          <button
            tuiButton
            type="button"
            appearance="flat"
            size="m"
            [attr.aria-label]="r.nombre"
            [title]="r.nombre"
            [attr.aria-pressed]="value().reaccionDelUsuario === r.tipo"
            [disabled]="busy()"
            (click)="react(r.tipo)"
            [attr.data-reaction]="r.tipo"
          >
            <social-icon [name]="r.icon" [filled]="value().reaccionDelUsuario === r.tipo" /><span
              class="sr-only"
              >{{ r.nombre }}</span
            >
            @if (value().porTipo[r.tipo]) {
              <small class="reaction-amount">{{ value().porTipo[r.tipo] }}</small>
            }
          </button>
        }
        <small class="picker-hint">Una reacción por publicación</small>
      </div></ng-template
    >
    @if (error()) {
      <p class="error" role="alert">{{ error() }}</p>
    }
    @if (open()) {
      <social-modal title="Personas que reaccionaron" (closed)="open.set(false)">
        <section class="reaction-dialog">
          @if (error()) {
            <p class="error" role="alert">{{ error() }}</p>
          }

          <nav class="reaction-tabs" aria-label="Filtrar personas por reacción">
            <button
              type="button"
              [class.active]="tipo === null"
              [attr.aria-pressed]="tipo === null"
              (click)="selectType(null)"
            >
              Todos ({{ value().total }})
            </button>
            @for (r of visibleTypes(); track r.tipo) {
              <button
                type="button"
                [attr.data-reaction]="r.tipo"
                [class.active]="tipo === r.tipo"
                [attr.aria-pressed]="tipo === r.tipo"
                [attr.aria-label]="r.nombre + ' (' + value().porTipo[r.tipo] + ')'"
                [title]="r.nombre"
                (click)="selectType(r.tipo)"
              >
                <social-icon [name]="r.icon" [filled]="true" /><span
                  >({{ value().porTipo[r.tipo] }})</span
                >
              </button>
            }
          </nav>
          @if (loading()) {
            <p role="status">Cargando…</p>
          } @else if (people(); as result) {
            <div class="reaction-people" role="list">
              @for (r of result.content; track r.idUsuario) {
                <article class="reaction-person" role="listitem">
                  <social-icon
                    class="person-reaction"
                    [attr.data-reaction]="r.tipo"
                    [name]="icon(r.tipo)"
                    [filled]="true"
                    [attr.title]="label(r.tipo)"
                  />
                  <app-avatar
                    [foto]="avatar(r.autor.fotoUrl)"
                    [iniciales]="initials(r.autor.nombreCompleto)"
                    size="m"
                  />
                  <div class="person-summary">
                    <a [routerLink]="['/social/usuarios', r.idUsuario]" (click)="open.set(false)">{{
                      r.autor.nombreCompleto
                    }}</a>
                    <time [attr.datetime]="r.fechaReaccion"
                      >{{ r.fechaReaccion | date: 'dd MMM yyyy'
                      }}<span>{{ r.fechaReaccion | date: 'HH:mm' }}</span></time
                    >
                  </div>
                </article>
              } @empty {
                <p class="muted">Todavía no hay reacciones.</p>
              }
            </div>
            <social-pager
              [page]="result.number"
              [total]="result.totalPages"
              (change)="load($event)"
            />
          }</section
      ></social-modal>
    }`,
})
export class SocialReactions {
  readonly id = input.required<number>();
  readonly comentario = input(false);
  readonly value = input.required<Reacciones>();
  readonly changed = output<Reacciones>();
  protected readonly options = REACCIONES;
  protected readonly avatar = urlAvatarFoto;
  protected readonly visibleTypes = computed(() =>
    REACCIONES.filter((r) => (this.value().porTipo[r.tipo] || 0) > 0),
  );
  protected initials(name: string) {
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
  protected selectType(tipo: TipoReaccion | null) {
    this.tipo = tipo;
    this.load(0);
  }
  private readonly api = inject(SocialService);
  private readonly destroy = inject(DestroyRef);
  protected readonly busy = signal(false);
  protected readonly loading = signal(false);
  protected readonly open = signal(false);
  protected readonly pickerOpen = signal(false);
  protected readonly error = signal('');
  protected readonly people = signal<Page<ReaccionDetalle> | null>(null);
  protected tipo: TipoReaccion | null = null;
  private request = 0;
  protected navigatePicker(event: KeyboardEvent) {
    const keys = ['ArrowRight', 'ArrowLeft', 'Home', 'End'];
    if (!keys.includes(event.key)) return;
    const root = event.currentTarget as HTMLElement;
    const buttons = [...root.querySelectorAll<HTMLButtonElement>('button:not(:disabled)')];
    if (!buttons.length) return;
    event.preventDefault();
    const index = buttons.indexOf(root.ownerDocument.activeElement as HTMLButtonElement);
    const next =
      event.key === 'Home'
        ? 0
        : event.key === 'End'
          ? buttons.length - 1
          : (index + (event.key === 'ArrowLeft' ? -1 : 1) + buttons.length) % buttons.length;
    buttons[next].focus();
  }
  protected icon(tipo: TipoReaccion | null) {
    return REACCIONES.find((x) => x.tipo === tipo)?.icon || 'thumbs-up';
  }
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
          this.pickerOpen.set(false);
          if (this.open()) {
            this.tipo = null;
            this.load(0);
          }
        },
        error: (e) => this.error.set(errorSocial(e)),
      });
  }
  protected toggle() {
    this.pickerOpen.set(false);
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
