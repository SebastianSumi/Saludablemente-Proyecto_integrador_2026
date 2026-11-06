import { Component, DestroyRef, OnInit, inject, signal, viewChildren } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { TuiButton, TuiIcon } from '@taiga-ui/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { finalize } from 'rxjs';
import { EtiquetaClinicaService, mensajeClinico } from './etiqueta-service';
import { ParametroClinico } from './etiqueta.model';
import { AsociacionesCard } from './asociaciones-card';
import { PaginaConCambios } from './cambios-pendientes';
import { ClinicaNav } from './clinica-nav';

@Component({ selector: 'app-parametro-detail', standalone: true, imports: [RouterLink, TuiButton, TuiIcon, ClinicaNav, AsociacionesCard],
  template: `<div class="cx-page"><app-clinica-nav /><a tuiButton appearance="flat" size="s" routerLink="/alertaclinica/parametros"><tui-icon icon="@tui.arrow-left" />Volver a parámetros</a>
    @if (cargando()) { <div class="cx-card cx-loading" role="status">Cargando parámetro…</div> }
    @if (error()) { <div class="cx-error" role="alert">{{ error() }} <button tuiButton appearance="flat" size="s" (click)="cargar()">Reintentar</button></div> }
    @if (parametro(); as p) { <header class="cx-card"><div class="cx-title"><span class="tile tile-rounded tile-soft-primary"><tui-icon icon="@tui.activity" /></span><div><h1>{{ p.nombre }}</h1><p class="cx-muted">{{ p.codigo }} · Unidad: {{ p.unidad || 'Sin unidad' }}</p></div></div><p class="cx-note">Administra las etiquetas relacionadas con este parámetro. Los demás parámetros de cada etiqueta se conservan.</p></header><app-asociaciones-card tipo="parametro-etiquetas" [entidadId]="p.id" /> }
    </div>`, styleUrls: ['./clinica.css'] })
export class ParametroDetail extends PaginaConCambios implements OnInit {
  private readonly service = inject(EtiquetaClinicaService); private readonly route = inject(ActivatedRoute); private readonly destroyRef = inject(DestroyRef);
  readonly tarjetas = viewChildren(AsociacionesCard); readonly parametro = signal<ParametroClinico | null>(null); readonly cargando = signal(false); readonly error = signal('');
  cambiosPendientes(): boolean { return this.tarjetas().some(c => c.draft.pendiente()); }
  override operacionEnCurso(): boolean { return this.tarjetas().some(c => c.ocupado()); }
  ngOnInit(): void { this.cargar(); }
  cargar(): void { this.cargando.set(true); this.error.set(''); this.service.parametro(Number(this.route.snapshot.paramMap.get('id'))).pipe(takeUntilDestroyed(this.destroyRef), finalize(() => this.cargando.set(false))).subscribe({ next: p => this.parametro.set(p), error: err => this.error.set(mensajeClinico(err)) }); }
}
