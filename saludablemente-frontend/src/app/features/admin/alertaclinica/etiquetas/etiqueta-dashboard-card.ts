import { Component, DestroyRef, OnInit, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { TuiButton, TuiIcon } from '@taiga-ui/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { finalize } from 'rxjs';
import { ApiService } from '../../../../core/services/api-service';
import { mensajeClinico } from './etiqueta-service';

interface UsoEtiqueta { codigo: string; nombre: string; colorHex: string; cantidad: number; }
@Component({ selector: 'app-etiqueta-dashboard-card', standalone: true, imports: [RouterLink, TuiButton, TuiIcon],
  template: `<section class="cx-card"><div class="cx-head"><div class="cx-title"><span class="tile tile-rounded tile-soft-secondary"><tui-icon icon="@tui.tags" /></span><div><h2>Alertas por etiqueta</h2><p class="cx-muted">Histórico completo de asignaciones · Puede incluir varias etiquetas por alerta.</p></div></div><a tuiButton appearance="flat" size="s" routerLink="/alertaclinica/reportes/etiquetas">Ver reporte</a></div>
    @if (cargando()) { <p class="cx-loading" role="status">Cargando etiquetas…</p> } @else if (error()) { <div class="cx-error" role="alert">{{ error() }} <button tuiButton appearance="flat" size="s" (click)="cargar()">Reintentar</button></div> } @else {
      <ul class="cx-list">@for (e of items(); track $index) { <li class="cx-row"><div><strong>{{ e.nombre }}</strong><p class="cx-muted">{{ e.codigo }}</p></div><span class="cx-badge">{{ e.cantidad }} asignaciones</span></li> } @empty { <li class="cx-empty">Aún no hay alertas etiquetadas.</li> }</ul>
    }</section>`, styleUrls: ['./clinica.css'] })
export class EtiquetaDashboardCard implements OnInit {
  private readonly http = inject(HttpClient); private readonly api = inject(ApiService); private readonly destroyRef = inject(DestroyRef);
  readonly items = signal<UsoEtiqueta[]>([]); readonly cargando = signal(false); readonly error = signal('');
  ngOnInit(): void { this.cargar(); }
  cargar(): void { this.cargando.set(true); this.error.set(''); this.http.get<UsoEtiqueta[]>(this.api.buildUrl('/api/v1/alertas/resumen/etiquetas')).pipe(takeUntilDestroyed(this.destroyRef), finalize(() => this.cargando.set(false))).subscribe({ next: e => this.items.set(e.slice(0, 5)), error: err => this.error.set(mensajeClinico(err)) }); }
}
