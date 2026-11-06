import { Component, DestroyRef, OnInit, computed, inject, input, output, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { TuiButton, TuiIcon } from '@taiga-ui/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { Subscription, finalize, take } from 'rxjs';
import { ConfirmService } from '../../../../core/services/confirm-service';
import { PersonaNombreService } from '../alerta/persona-nombre-service';
import { EtiquetaClinicaService, mensajeClinico } from './etiqueta-service';
import { FilaAsociacion, TipoAsociacion } from './etiqueta.model';
import { AsociacionesDraft } from './asociaciones-draft';

@Component({
  selector: 'app-asociaciones-card', standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule, RouterLink, TuiButton, TuiIcon],
  templateUrl: './asociaciones-card.html', styleUrls: ['./clinica.css'],
})
export class AsociacionesCard implements OnInit {
  readonly tipo = input.required<TipoAsociacion>();
  readonly entidadId = input.required<number>();
  readonly bloqueado = input(false);
  readonly permiteAgregar = input(true);
  readonly guardado = output<void>();
  readonly draft = new AsociacionesDraft();
  readonly ocupado = signal(false);
  readonly cargando = signal(true);
  readonly listo = signal(false);
  readonly error = signal('');
  readonly exito = signal('');
  readonly conflicto = signal(false);
  readonly selector = signal(false);
  readonly opciones = signal<FilaAsociacion[]>([]);
  readonly buscando = signal(false);
  readonly errorOpciones = signal('');
  readonly pagina = signal(0);
  readonly totalPaginas = signal(0);
  readonly totalOpciones = signal(0);
  readonly personas = signal<Map<number, string>>(new Map());
  readonly personasOpciones = computed(() => [...this.personas()].map(([id, nombre]) => ({ id, nombre })));
  readonly titulo = computed(() => this.tipo() === 'etiqueta-alertas' ? 'Alertas asociadas' : this.tipo() === 'etiqueta-parametros' ? 'Parámetros asociados' : 'Etiquetas asociadas');
  readonly automaticas = computed(() => this.draft.filas().filter(f => f.origen === 'MOTOR').length);
  texto = ''; personaId = ''; estado = ''; desde = ''; hasta = '';
  private revision = '';
  private readonly service = inject(EtiquetaClinicaService);
  private readonly confirmar = inject(ConfirmService);
  private readonly nombres = inject(PersonaNombreService);
  private readonly destroyRef = inject(DestroyRef);
  private busqueda?: Subscription;

  ngOnInit(): void {
    this.cargar();
    if (this.tipo() === 'etiqueta-alertas') this.nombres.nombres().pipe(takeUntilDestroyed(this.destroyRef)).subscribe(m => this.personas.set(m));
  }
  cargar(): void {
    this.cargando.set(true); this.error.set('');
    this.service.cargarTarjeta(this.tipo(), this.entidadId()).pipe(takeUntilDestroyed(this.destroyRef), finalize(() => this.cargando.set(false))).subscribe({
      next: tarjeta => { this.revision = tarjeta.revision; this.draft.cargar(tarjeta.detalles); this.listo.set(true); this.conflicto.set(false); },
      error: err => this.error.set(mensajeClinico(err)),
    });
  }
  recargar(): void {
    if (this.ocupado()) return;
    if (!this.draft.pendiente()) { this.cargar(); return; }
    this.confirmar.confirmar('Recargar descartará los cambios pendientes de esta tarjeta. ¿Continuar?', 'Descartar y recargar')
      .pipe(take(1), takeUntilDestroyed(this.destroyRef)).subscribe(ok => { if (ok) this.cargar(); });
  }
  abrir(): void { this.selector.update(v => !v); if (this.selector()) this.buscar(0); }
  buscar(pagina = 0): void {
    this.errorOpciones.set('');
    if (this.desde && this.hasta && this.desde > this.hasta) { this.errorOpciones.set('La fecha inicial no puede ser posterior a la final.'); return; }
    this.busqueda?.unsubscribe(); this.buscando.set(true); this.pagina.set(pagina);
    this.busqueda = this.service.opciones(this.tipo(), this.tipo() === 'etiqueta-alertas'
      ? { pagina, tamanio: 10, personaId: this.personaId, estado: this.estado,
          desde: this.desde ? `${this.desde}T00:00:00` : undefined, hasta: this.hasta ? `${this.hasta}T23:59:59.999999999` : undefined }
      : { texto: this.texto, pagina, tamanio: 10 }).pipe(takeUntilDestroyed(this.destroyRef), finalize(() => this.buscando.set(false))).subscribe({
      next: page => { this.opciones.set(page.content); this.totalPaginas.set(page.totalPages); this.totalOpciones.set(page.totalElements); },
      error: err => this.errorOpciones.set(mensajeClinico(err)),
    });
  }
  agregar(fila: FilaAsociacion): void {
    if (this.ocupado() || this.bloqueado() || !this.permiteAgregar()) return;
    if (!this.draft.agregar(fila)) this.errorOpciones.set('No se permiten duplicados ni más de 500 asociaciones por tarjeta.');
    this.exito.set('');
  }
  quitar(fila: FilaAsociacion): void {
    if (this.ocupado() || this.bloqueado()) return;
    this.draft.quitar(fila.id); this.exito.set('');
  }
  restaurar(): void { if (!this.ocupado()) { this.draft.cancelar(); this.error.set(''); this.exito.set(''); } }
  guardar(): void {
    if (!this.listo() || !this.draft.pendiente() || this.ocupado() || this.bloqueado() || this.conflicto()) return;
    const filas = [...this.draft.filas()];
    this.ocupado.set(true); this.error.set(''); this.exito.set('');
    this.confirmar.confirmar(`${this.titulo()}. ${this.draft.resumen()} ¿Guardar todos los cambios?`, 'Guardar cambios')
      .pipe(take(1), takeUntilDestroyed(this.destroyRef)).subscribe({
        next: ok => {
          if (!ok) { this.ocupado.set(false); return; }
          this.service.guardarTarjeta(this.tipo(), this.entidadId(), this.revision, filas)
            .pipe(takeUntilDestroyed(this.destroyRef), finalize(() => this.ocupado.set(false))).subscribe({
              next: tarjeta => { this.revision = tarjeta.revision; this.draft.cargar(tarjeta.detalles); this.exito.set('Todas las asociaciones se guardaron correctamente.'); this.guardado.emit(); },
              error: err => { this.error.set(mensajeClinico(err)); this.conflicto.set(err.status === 409); },
            });
        }, error: err => { this.error.set(mensajeClinico(err)); this.ocupado.set(false); },
      });
  }
  nombre(fila: FilaAsociacion): string {
    if (this.tipo() !== 'etiqueta-alertas') return fila.nombre;
    const id = Number(fila.codigo.replace('Persona #', ''));
    return `${fila.nombre} · ${this.personas().get(id) || fila.codigo}`;
  }
  color(value?: string): string { return value && /^#[0-9a-f]{6}$/i.test(value) ? value : 'var(--sal-primary)'; }
}
