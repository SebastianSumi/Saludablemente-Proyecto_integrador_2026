import { Component, computed, input } from '@angular/core';
import { PostResumen } from './social.model';
import { SocialIcon } from './social-icon';

@Component({
  selector: 'social-activity-meta',
  imports: [SocialIcon],
  template: ` <section class="activity" aria-label="Información de la actividad">
    <header>
      <span class="label">Detalles de la actividad</span>
      @if (post().estado) {
        <span class="status" [attr.data-state]="post().estado"
          ><span class="status-dot"></span>{{ status() }}</span
        >
      }
    </header>
    <dl>
      @if (date()) {
        <div class="item">
          <span class="symbol"><social-icon name="calendar" /></span>
          <div>
            <dt>Fecha</dt>
            <dd>{{ date() }}</dd>
          </div>
        </div>
      }
      @if (post().horaInicio || post().horaFin) {
        <div class="item">
          <span class="symbol"><social-icon name="clock" /></span>
          <div>
            <dt>Horario</dt>
            <dd>
              {{ post().horaInicio?.slice(0, 5) }}
              @if (post().horaInicio && post().horaFin) {
                <span aria-hidden="true"> – </span>
              }
              {{ post().horaFin?.slice(0, 5) }}
            </dd>
          </div>
        </div>
      }
      @if (post().lugar) {
        <div class="item">
          <span class="symbol"><social-icon name="map-pin" /></span>
          <div>
            <dt>Lugar</dt>
            <dd>{{ post().lugar }}</dd>
          </div>
        </div>
      }
      @if (post().coordinadorNombre) {
        <div class="item">
          <span class="symbol"><social-icon name="user" /></span>
          <div>
            <dt>Coordina</dt>
            <dd>{{ post().coordinadorNombre }}</dd>
          </div>
        </div>
      }
    </dl>
  </section>`,
  styles: [
    `
      :host {
        display: block;
        margin: 18px 0;
      }
      .activity {
        border: 1px solid var(--sal-line);
        border-radius: 16px;
        background: var(--sal-hover);
        padding: 16px;
      }
      header {
        display: flex;
        gap: 12px;
        align-items: center;
        justify-content: space-between;
        flex-wrap: wrap;
      }
      .label {
        font-size: 12px;
        font-weight: 600;
        color: var(--sal-ink-soft);
      }
      .status {
        display: inline-flex;
        align-items: center;
        gap: 6px;
        padding: 5px 10px;
        border-radius: 99px;
        background: color-mix(in srgb, var(--sal-primary) 12%, var(--sal-card));
        color: var(--sal-action);
        font-size: 12px;
        font-weight: 600;
      }
      .status-dot {
        width: 6px;
        height: 6px;
        background: currentColor;
        border-radius: 50%;
      }
      .status[data-state='CANCELADA'] {
        color: var(--tui-status-negative);
      }
      .status[data-state='FINALIZADA'] {
        color: var(--tui-status-positive);
      }
      .status[data-state='EN_CURSO'] {
        color: var(--sal-accent);
      }
      dl {
        display: grid;
        grid-template-columns: 1fr 1fr;
        gap: 18px 16px;
        margin: 18px 0 0;
      }
      .item {
        display: flex;
        gap: 10px;
        min-width: 0;
      }
      .symbol {
        display: flex;
        align-items: center;
        justify-content: center;
        width: 34px;
        height: 34px;
        border: 1px solid var(--sal-line);
        border-radius: 10px;
        background: var(--sal-card);
        color: var(--sal-action);
        flex-shrink: 0;
      }
      dt {
        font-size: 11px;
        color: var(--sal-ink-soft);
        margin-bottom: 3px;
      }
      dd {
        margin: 0;
        font-size: 13px;
        font-weight: 500;
        line-height: 1.5;
        overflow-wrap: anywhere;
      }
      @media (max-width: 480px) {
        dl {
          grid-template-columns: 1fr;
          gap: 14px;
        }
        .activity {
          padding: 14px;
        }
      }
    `,
  ],
})
export class SocialActivityMeta {
  readonly post = input.required<PostResumen>();
  protected readonly status = computed(
    () =>
      ({
        PROGRAMADA: 'Programada',
        EN_CURSO: 'En curso',
        FINALIZADA: 'Finalizada',
        CANCELADA: 'Cancelada',
        REPROGRAMADA: 'Reprogramada',
      })[this.post().estado ?? ''] ?? this.post().estado?.replaceAll('_', ' ').toLowerCase(),
  );
  protected readonly date = computed(() => {
    const value = this.post().fechaEvento;
    if (!value) return '';
    const parts = value.split('-').map(Number);
    if (parts.length !== 3 || parts.some((x) => !Number.isFinite(x))) return '';
    return new Intl.DateTimeFormat('es-PE', {
      day: 'numeric',
      month: 'short',
      year: 'numeric',
    }).format(new Date(parts[0], parts[1] - 1, parts[2]));
  });
}
