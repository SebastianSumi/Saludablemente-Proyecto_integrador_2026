import { Component, DestroyRef, OnInit, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { TuiButton, TuiIcon } from '@taiga-ui/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { finalize } from 'rxjs';
import { EtiquetaClinicaService, mensajeClinico } from './etiqueta-service';
import { ParametroClinico } from './etiqueta.model';
import { ClinicaNav } from './clinica-nav';

@Component({ selector: 'app-parametro-list', standalone: true, imports: [FormsModule, RouterLink, TuiButton, TuiIcon, ClinicaNav],
  template: `<div class="cx-page"><app-clinica-nav /><header class="cx-card cx-title"><span class="tile tile-rounded tile-soft-primary"><tui-icon icon="@tui.activity" /></span><div><h1>Parámetros clínicos</h1><p class="cx-muted">Consulta el catálogo y administra las etiquetas de cada parámetro.</p></div></header>
    <section class="cx-card"><div class="cx-fields"><label class="cx-field"><span>Nombre o código</span><input class="cx-input" [ngModel]="texto()" (ngModelChange)="texto.set($event)" placeholder="Buscar parámetro" /></label><label class="cx-field"><span>Estado del catálogo</span><select class="cx-select" [ngModel]="estado" (ngModelChange)="estado = $event; cargar()"><option value="ACTIVO">Activos</option><option value="INACTIVO">Inactivos</option></select></label></div>
      @if (error()) { <div class="cx-error" role="alert">{{ error() }} <button tuiButton appearance="flat" size="s" (click)="cargar()">Reintentar</button></div> }
      @if (cargando()) { <div class="cx-loading" role="status">Cargando parámetros…</div> } @else if (!error()) {
        <ul class="cx-list">@for (p of visibles(); track p.id) { <li class="cx-row"><div class="cx-row-main"><span class="tile tile-rounded tile-soft-primary"><tui-icon icon="@tui.activity" /></span><div><a [routerLink]="[p.id]">{{ p.nombre }}</a><p class="cx-muted">{{ p.codigo }} · {{ p.unidad || 'Sin unidad' }}</p></div></div><a tuiButton appearance="outline" size="s" [routerLink]="[p.id]">Administrar etiquetas</a></li> } @empty { <li class="cx-empty">No hay parámetros para esta búsqueda y estado.</li> }</ul>
      }
    </section></div>`, styleUrls: ['./clinica.css'] })
export class ParametroList implements OnInit {
  private readonly service = inject(EtiquetaClinicaService); private readonly destroyRef = inject(DestroyRef);
  readonly items = signal<ParametroClinico[]>([]); readonly texto = signal(''); readonly cargando = signal(false); readonly error = signal('');
  readonly visibles = computed(() => this.items().filter(p => `${p.codigo} ${p.nombre}`.toLocaleLowerCase().includes(this.texto().toLocaleLowerCase())));
  estado = 'ACTIVO'; private consulta = 0;
  ngOnInit(): void { this.cargar(); }
  cargar(): void {
    const consulta = ++this.consulta; this.cargando.set(true); this.error.set('');
    this.service.parametros(this.estado).pipe(takeUntilDestroyed(this.destroyRef), finalize(() => { if (consulta === this.consulta) this.cargando.set(false); })).subscribe({
      next: p => { if (consulta === this.consulta) this.items.set(p); }, error: err => { if (consulta === this.consulta) this.error.set(mensajeClinico(err)); },
    });
  }
}
