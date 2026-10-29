import { Component, DestroyRef, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { TuiButton, TuiIcon } from '@taiga-ui/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { Subscription, finalize } from 'rxjs';
import { EtiquetaClinicaService, mensajeClinico } from './etiqueta-service';
import { EtiquetaResumen, ParametroClinico, ReporteEtiquetas } from './etiqueta.model';
import { PersonaNombreService } from '../alerta/persona-nombre-service';
import { ClinicaNav } from './clinica-nav';

@Component({ selector: 'app-etiqueta-reporte', standalone: true,
  imports: [CommonModule, FormsModule, RouterLink, TuiButton, TuiIcon, ClinicaNav], templateUrl: './etiqueta-reporte.html', styleUrls: ['./clinica.css'] })
export class EtiquetaReporte implements OnInit {
  private readonly service = inject(EtiquetaClinicaService); private readonly destroyRef = inject(DestroyRef); private readonly nombresService = inject(PersonaNombreService);
  readonly reporte = signal<ReporteEtiquetas | null>(null); readonly etiquetas = signal<EtiquetaResumen[]>([]); readonly parametros = signal<ParametroClinico[]>([]);
  readonly nombres = signal<Map<number, string>>(new Map()); readonly cargando = signal(false); readonly error = signal(''); readonly errorOpciones = signal('');
  readonly aplicado = signal('Todas las fechas de asignación');
  estadoEtiqueta = ''; idEtiqueta = ''; idParametro = ''; estadoAlerta = ''; severidad = ''; desde = ''; hasta = '';
  private consulta?: Subscription;
  ngOnInit(): void { this.opciones(); this.cargar(); this.nombresService.nombres().pipe(takeUntilDestroyed(this.destroyRef)).subscribe(n => this.nombres.set(n)); }
  opciones(): void {
    this.errorOpciones.set('');
    this.service.todas().pipe(takeUntilDestroyed(this.destroyRef)).subscribe({ next: e => this.etiquetas.set(e), error: err => this.errorOpciones.set(mensajeClinico(err)) });
    this.service.parametros().pipe(takeUntilDestroyed(this.destroyRef)).subscribe({ next: p => this.parametros.set(p), error: err => this.errorOpciones.set(mensajeClinico(err)) });
  }
  cargar(): void {
    this.error.set('');
    if (this.desde && this.hasta && this.desde > this.hasta) { this.error.set('La fecha inicial no puede ser posterior a la final.'); return; }
    this.consulta?.unsubscribe(); this.cargando.set(true);
    const periodo = this.desde || this.hasta ? `${this.desde || 'Inicio'} → ${this.hasta || 'Hoy'} · Fecha de asignación` : 'Todas las fechas de asignación';
    this.consulta = this.service.reporte({ estadoEtiqueta: this.estadoEtiqueta, idEtiqueta: this.idEtiqueta, idParametro: this.idParametro,
      estadoAlerta: this.estadoAlerta, severidad: this.severidad, desde: this.desde ? `${this.desde}T00:00:00` : undefined,
      hasta: this.hasta ? `${this.hasta}T23:59:59.999999999` : undefined }).pipe(takeUntilDestroyed(this.destroyRef), finalize(() => this.cargando.set(false))).subscribe({
        next: r => { this.reporte.set(r); this.aplicado.set(periodo); }, error: err => this.error.set(mensajeClinico(err)),
      });
  }
  limpiar(): void { this.estadoEtiqueta = ''; this.idEtiqueta = ''; this.idParametro = ''; this.estadoAlerta = ''; this.severidad = ''; this.desde = ''; this.hasta = ''; this.cargar(); }
  nombre(id: number): string { return this.nombresService.nombreDe(this.nombres(), id); }
  color(value: string): string { return /^#[0-9a-f]{6}$/i.test(value) ? value : 'var(--sal-primary)'; }
}
