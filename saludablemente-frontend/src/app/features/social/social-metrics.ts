import { Component, computed, DestroyRef, inject, signal } from '@angular/core';
import { DecimalPipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { TuiButton } from '@taiga-ui/core';
import { NgxEchartsDirective } from 'ngx-echarts';
import type { EChartsOption } from 'echarts';
import { Subject, catchError, of, switchMap, tap } from 'rxjs';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { SocialService } from './social-service';
import { Metricas } from './social.model';
import { errorSocial } from './social.utils';
@Component({
  selector: 'app-social-metrics',
  imports: [DecimalPipe, FormsModule, RouterLink, TuiButton, NgxEchartsDirective],
  styleUrl: './social.css',
  template: ` <div class="stack">
    <header>
      <span class="eyebrow">Administración</span>
      <h2>La comunidad en cifras</h2>
      <p class="muted">Actividad del contenido visible durante el periodo seleccionado.</p>
    </header>
    <form class="panel fields" (ngSubmit)="load()">
      <div class="two">
        <label>Desde<input type="date" name="desde" required [(ngModel)]="desde" /></label
        ><label>Hasta<input type="date" name="hasta" required [(ngModel)]="hasta" /></label>
      </div>
      <small>Selecciona un rango de hasta un año.</small>
      <div><button tuiButton type="submit" size="s">Consultar</button></div>
    </form>
    @if (error()) {
      <p class="error" role="alert">{{ error() }}</p>
    }
    @if (loading()) {
      <p class="loading" role="status">Calculando métricas…</p>
    } @else if (metrics(); as m) {
      <div class="stats">
        <section class="panel stat">
          <small>Publicaciones</small><strong>{{ m.totalPosts }}</strong>
        </section>
        <section class="panel stat">
          <small>Comentarios</small><strong>{{ m.totalComentarios }}</strong>
        </section>
        <section class="panel stat">
          <small>Reacciones</small><strong>{{ m.totalReacciones }}</strong>
        </section>
        <section class="panel stat">
          <small>Compartidos</small><strong>{{ m.totalShares }}</strong>
        </section>
        <section class="panel stat">
          <small>Usuarios activos</small><strong>{{ m.usuariosActivos }}</strong>
        </section>
        <section class="panel stat">
          <small>Interacciones por publicación</small
          ><strong>{{ m.engagement | number: '1.0-2' }}</strong>
        </section>
      </div>
      <section class="panel">
        <h3>Actividad por día</h3>
        @if (m.volumenPorDia.length) {
          <div
            echarts
            [options]="chart()"
            style="height:300px"
            aria-label="Gráfico de publicaciones, comentarios y reacciones por día"
          ></div>
          <details>
            <summary>Ver datos del gráfico</summary>
            <div class="table-scroll">
              <table>
                <thead>
                  <tr>
                    <th>Fecha</th>
                    <th>Publicaciones</th>
                    <th>Comentarios</th>
                    <th>Reacciones</th>
                  </tr>
                </thead>
                <tbody>
                  @for (d of m.volumenPorDia; track d.fecha) {
                    <tr>
                      <td>{{ d.fecha }}</td>
                      <td>{{ d.publicaciones }}</td>
                      <td>{{ d.comentarios }}</td>
                      <td>{{ d.reacciones }}</td>
                    </tr>
                  }
                </tbody>
              </table>
            </div>
          </details>
        } @else {
          <p class="empty">No hay actividad en este periodo.</p>
        }
      </section>
      <div class="two">
        <section class="panel">
          <h3>Publicaciones más comentadas</h3>
          @for (p of m.topComentados; track p.idPost) {
            <p>
              <a [routerLink]="['/social/posts', p.idPost]">{{ p.titulo || 'Publicación' }}</a> ·
              {{ p.total }}
            </p>
          } @empty {
            <p class="muted">Sin comentarios.</p>
          }
        </section>
        <section class="panel">
          <h3>Publicaciones con más reacciones</h3>
          @for (p of m.topReaccionados; track p.idPost) {
            <p>
              <a [routerLink]="['/social/posts', p.idPost]">{{ p.titulo || 'Publicación' }}</a> ·
              {{ p.total }}
            </p>
          } @empty {
            <p class="muted">Sin reacciones.</p>
          }
        </section>
        <section class="panel">
          <h3>Participación de la comunidad</h3>
          @for (u of m.topUsuarios; track u.idUsuario) {
            <p>
              <a [routerLink]="['/social/usuarios', u.idUsuario]">{{ u.autor.nombreCompleto }}</a> ·
              {{ u.total }}
            </p>
          } @empty {
            <p class="muted">Sin participación.</p>
          }
        </section>
        <section class="panel">
          <h3>Hashtags del periodo</h3>
          @for (t of m.hashtagsTendencia; track t.tag) {
            <p>
              <a [routerLink]="['/social/hashtags', t.tag]">#{{ t.tag }}</a> · {{ t.total }}
            </p>
          } @empty {
            <p class="muted">Sin hashtags.</p>
          }
        </section>
      </div>
    }
  </div>`,
})
export class SocialMetrics {
  private readonly api = inject(SocialService);
  private readonly destroy = inject(DestroyRef);
  private readonly queries = new Subject<{ desde: string; hasta: string }>();
  protected desde = this.localDate(new Date(new Date().getFullYear(), new Date().getMonth(), 1));
  protected hasta = this.localDate(new Date());
  protected readonly metrics = signal<Metricas | null>(null);
  protected readonly loading = signal(false);
  protected readonly error = signal('');
  protected readonly chart = computed<EChartsOption>(() => {
    const days = this.metrics()?.volumenPorDia ?? [];
    return {
      tooltip: { trigger: 'axis' },
      legend: { data: ['Publicaciones', 'Comentarios', 'Reacciones'] },
      grid: { left: 45, right: 16, bottom: 60, top: 50 },
      xAxis: { type: 'category', data: days.map((x) => x.fecha) },
      yAxis: { type: 'value', minInterval: 1 },
      series: [
        { name: 'Publicaciones', type: 'bar', data: days.map((x) => x.publicaciones) },
        { name: 'Comentarios', type: 'bar', data: days.map((x) => x.comentarios) },
        { name: 'Reacciones', type: 'bar', data: days.map((x) => x.reacciones) },
      ],
      aria: { enabled: true },
    };
  });
  constructor() {
    this.queries
      .pipe(
        tap(() => {
          this.loading.set(true);
          this.error.set('');
          this.metrics.set(null);
        }),
        switchMap((q) =>
          this.api.metricas(q.desde, q.hasta).pipe(
            catchError((e) => {
              this.error.set(errorSocial(e));
              return of(null);
            }),
          ),
        ),
        takeUntilDestroyed(this.destroy),
      )
      .subscribe((r) => {
        this.metrics.set(r);
        this.loading.set(false);
      });
    this.load();
  }
  private localDate(d: Date) {
    return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
  }
  protected load() {
    const from = new Date(`${this.desde}T00:00:00`);
    const to = new Date(`${this.hasta}T00:00:00`);
    const end = new Date(from);
    end.setFullYear(end.getFullYear() + 1);
    if (
      !this.desde ||
      !this.hasta ||
      !Number.isFinite(from.getTime()) ||
      !Number.isFinite(to.getTime()) ||
      to < from ||
      to > end
    ) {
      this.error.set('Selecciona fechas válidas, en orden y con un máximo de un año.');
      return;
    }
    this.queries.next({ desde: this.desde, hasta: this.hasta });
  }
}
