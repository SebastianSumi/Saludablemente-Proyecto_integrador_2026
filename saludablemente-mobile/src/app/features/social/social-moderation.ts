import { Component, DestroyRef, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { TuiButton } from '@taiga-ui/core';
import { BehaviorSubject, catchError, filter, finalize, of, switchMap, tap } from 'rxjs';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ConfirmService } from '../../core/services/confirm-service';
import { SocialService } from './social-service';
import { EstadoReporte, Page, Reporte } from './social.model';
import { errorSocial } from './social.utils';
import { SocialPager } from './social-pager';
@Component({
  selector: 'app-social-moderation',
  imports: [DatePipe, FormsModule, RouterLink, TuiButton, SocialPager],
  styleUrl: './social.css',
  template: ` <div class="stack">
    <header>
      <span class="eyebrow">Administración</span>
      <h2>Cuidamos la comunidad</h2>
      <p class="muted">Revisa los reportes y registra una decisión.</p>
    </header>
    <section class="panel fields">
      <label
        >Estado de los reportes<select [(ngModel)]="estado" (ngModelChange)="load(0)">
          <option value="PENDIENTE">Pendientes</option>
          <option value="ACEPTADO">Aceptados</option>
          <option value="RECHAZADO">Rechazados</option>
        </select></label
      >
    </section>
    @if (error()) {
      <p class="error" role="alert">
        {{ error() }}
        <button tuiButton type="button" appearance="flat" size="s" (click)="load(page())">
          Reintentar
        </button>
      </p>
    }
    @if (notice()) {
      <p class="notice" role="status">{{ notice() }}</p>
    }
    @if (loading()) {
      <p class="loading" role="status">Cargando reportes…</p>
    } @else if (result(); as list) {
      @for (r of list.content; track r.idReporte) {
        <article class="panel fields">
          <div class="identity">
            <div class="grow">
              <h3>{{ r.idComentario ? 'Comentario reportado' : 'Publicación reportada' }}</h3>
              <small
                >{{ r.fechaReporte | date: 'medium' }} · {{ r.reportante.nombreCompleto }}</small
              >
            </div>
            <span class="chip">{{ r.estado }}</span>
          </div>
          <p>
            <strong>{{ r.motivo }}</strong> · {{ r.detalle || 'Sin detalles adicionales.' }}
          </p>
          <a
            [routerLink]="['/social/posts', r.idPostContenido]"
            [fragment]="r.idComentario ? 'comentario-' + r.idComentario : undefined"
            >Consultar publicación</a
          >
          @if (r.estado === 'PENDIENTE') {
            <label
              >Observación de la revisión<textarea
                [ngModel]="observations[r.idReporte] || ''"
                (ngModelChange)="observations[r.idReporte] = $event"
                maxlength="500"
                rows="2"
              ></textarea>
            </label>
            <div class="actions">
              <button
                tuiButton
                size="s"
                type="button"
                [disabled]="busy()"
                (click)="resolve(r, 'ACEPTADO')"
              >
                Aceptar y ocultar</button
              ><button
                tuiButton
                appearance="secondary"
                size="s"
                type="button"
                [disabled]="busy()"
                (click)="resolve(r, 'RECHAZADO')"
              >
                Rechazar reporte
              </button>
            </div>
          } @else {
            <p class="muted">
              Revisado por {{ r.revisor?.nombreCompleto }} · {{ r.fechaRevision | date: 'medium'
              }}<br />{{ r.comentarioRevision || 'Sin observación.' }} · Acción:
              {{ r.accionTomada }}
            </p>
            @if (r.estado === 'ACEPTADO') {
              <button
                tuiButton
                size="s"
                type="button"
                appearance="secondary"
                [disabled]="busy()"
                (click)="restore(r.idComentario || r.idPost!, !!r.idComentario)"
              >
                Restaurar contenido
              </button>
            }
          }
        </article>
      } @empty {
        <p class="empty">No hay reportes en este estado.</p>
      }
      <social-pager [page]="list.number" [total]="list.totalPages" (change)="load($event)" />
    }
    <form class="panel fields" (ngSubmit)="restoreById()">
      <h3>Restaurar contenido oculto</h3>
      <p class="muted">
        Para contenido ocultado directamente, indica el identificador. Solo se restaura contenido
        retirado por moderación.
      </p>
      <div class="two">
        <label
          >Tipo<select name="tipo" [(ngModel)]="restoreType">
            <option value="POST">Publicación</option>
            <option value="COMENTARIO">Comentario</option>
          </select></label
        ><label
          >Identificador<input name="id" type="number" min="1" required [(ngModel)]="restoreId"
        /></label>
      </div>
      <div>
        <button
          tuiButton
          type="submit"
          size="s"
          appearance="secondary"
          [disabled]="busy() || !restoreId"
        >
          Restaurar
        </button>
      </div>
    </form>
  </div>`,
})
export class SocialModeration {
  private readonly api = inject(SocialService);
  private readonly confirm = inject(ConfirmService);
  private readonly destroy = inject(DestroyRef);
  private readonly queries = new BehaviorSubject({ estado: 'PENDIENTE' as EstadoReporte, page: 0 });
  protected estado: EstadoReporte = 'PENDIENTE';
  protected readonly page = signal(0);
  protected readonly result = signal<Page<Reporte> | null>(null);
  protected readonly loading = signal(false);
  protected readonly busy = signal(false);
  protected readonly error = signal('');
  protected readonly notice = signal('');
  protected observations: Record<number, string> = {};
  protected restoreId: number | null = null;
  protected restoreType = 'POST';
  constructor() {
    this.queries
      .pipe(
        tap(() => {
          this.loading.set(true);
          this.error.set('');
        }),
        switchMap((q) =>
          this.api.reportes(q.estado, q.page).pipe(
            catchError((e) => {
              this.error.set(errorSocial(e));
              return of(null);
            }),
          ),
        ),
        takeUntilDestroyed(this.destroy),
      )
      .subscribe((r) => {
        this.result.set(r);
        this.loading.set(false);
      });
  }
  protected load(page: number) {
    this.page.set(page);
    this.queries.next({ estado: this.estado, page });
  }
  protected resolve(r: Reporte, estado: 'ACEPTADO' | 'RECHAZADO') {
    if (this.busy()) return;
    this.confirm
      .confirmar(
        estado === 'ACEPTADO'
          ? '¿Aceptar el reporte y ocultar el contenido?'
          : '¿Rechazar este reporte?',
        estado === 'ACEPTADO' ? 'Aceptar y ocultar' : 'Rechazar',
      )
      .pipe(
        filter(Boolean),
        tap(() => {
          this.busy.set(true);
          this.error.set('');
        }),
        switchMap(() =>
          this.api.resolver(r.idReporte, estado, this.observations[r.idReporte]?.trim() || null),
        ),
        takeUntilDestroyed(this.destroy),
        finalize(() => this.busy.set(false)),
      )
      .subscribe({
        next: () => {
          this.notice.set('Decisión registrada.');
          delete this.observations[r.idReporte];
          this.load(this.page());
        },
        error: (e) => this.error.set(errorSocial(e)),
      });
  }
  protected restoreById() {
    if (!this.restoreId || !Number.isSafeInteger(this.restoreId) || this.restoreId < 1) {
      this.error.set('Indica un identificador positivo válido.');
      return;
    }
    this.restore(this.restoreId, this.restoreType === 'COMENTARIO');
  }
  protected restore(id: number, comment: boolean) {
    if (this.busy()) return;
    this.confirm
      .confirmar(
        '¿Restaurar este contenido? Las respuestas ocultas se restauran individualmente.',
        'Restaurar',
      )
      .pipe(
        filter(Boolean),
        tap(() => {
          this.busy.set(true);
          this.error.set('');
        }),
        switchMap(() => this.api.moderar(id, true, comment)),
        takeUntilDestroyed(this.destroy),
        finalize(() => this.busy.set(false)),
      )
      .subscribe({
        next: () =>
          this.notice.set('Contenido restaurado. El reporte conserva su historial de resolución.'),
        error: (e) => this.error.set(errorSocial(e)),
      });
  }
}
