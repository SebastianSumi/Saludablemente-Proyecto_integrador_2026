import { Component, DestroyRef, OnInit, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { TuiButton, TuiIcon } from '@taiga-ui/core';
import { takeUntilDestroyed, toSignal } from '@angular/core/rxjs-interop';
import { finalize, take } from 'rxjs';
import { ConfirmService } from '../../../../core/services/confirm-service';
import { EtiquetaClinicaService, mensajeClinico } from './etiqueta-service';
import { ParametroClinico } from './etiqueta.model';
import { AsociacionesDraft } from './asociaciones-draft';
import { PaginaConCambios } from './cambios-pendientes';
import { ClinicaNav } from './clinica-nav';

@Component({ selector: 'app-etiqueta-form', standalone: true,
  imports: [ReactiveFormsModule, RouterLink, TuiButton, TuiIcon, ClinicaNav], templateUrl: './etiqueta-form.html', styleUrls: ['./clinica.css'] })
export class EtiquetaForm extends PaginaConCambios implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly service = inject(EtiquetaClinicaService);
  private readonly confirm = inject(ConfirmService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);
  private readonly destroyRef = inject(DestroyRef);
  readonly id = this.route.snapshot.paramMap.get('id') ? Number(this.route.snapshot.paramMap.get('id')) : null;
  readonly form = this.fb.nonNullable.group({ codigo: ['', [Validators.required, Validators.maxLength(40), Validators.pattern(/^[A-Z][A-Z0-9_]*$/)]],
    nombre: ['', [Validators.required, Validators.maxLength(100), Validators.pattern(/\S/)]], descripcion: ['', Validators.maxLength(300)],
    colorHex: ['#2E7D5B', [Validators.required, Validators.pattern(/^#[0-9a-fA-F]{6}$/)]] });
  readonly valores = toSignal(this.form.valueChanges, { initialValue: this.form.getRawValue() });
  readonly original = signal(JSON.stringify(this.form.getRawValue()));
  readonly draft = new AsociacionesDraft();
  readonly parametros = signal<ParametroClinico[]>([]); readonly texto = signal('');
  readonly opciones = computed(() => this.parametros().filter(p => `${p.codigo} ${p.nombre}`.toLocaleLowerCase().includes(this.texto().toLocaleLowerCase())));
  readonly cargando = signal(false); readonly ocupado = signal(false); readonly listo = signal(this.id === null);
  readonly error = signal(''); readonly errorParametros = signal(''); readonly cargandoParametros = signal(false); readonly conflicto = signal(false);
  readonly alertasAsociadas = signal(0); readonly estado = signal('ACTIVO');
  private revision?: string;
  ngOnInit(): void { this.cargarParametros(); if (this.id !== null) this.cargar(); }
  cambiosPendientes(): boolean { this.valores(); return this.draft.pendiente() || JSON.stringify(this.form.getRawValue()) !== this.original(); }
  override operacionEnCurso(): boolean { return this.ocupado(); }
  cargarParametros(): void {
    this.cargandoParametros.set(true); this.errorParametros.set('');
    this.service.parametros().pipe(takeUntilDestroyed(this.destroyRef), finalize(() => this.cargandoParametros.set(false))).subscribe({ next: p => this.parametros.set(p), error: err => this.errorParametros.set(mensajeClinico(err)) });
  }
  cargar(): void {
    if (this.id === null) return;
    this.cargando.set(true); this.error.set('');
    this.service.obtener(this.id).pipe(takeUntilDestroyed(this.destroyRef), finalize(() => this.cargando.set(false))).subscribe({
      next: e => {
        this.form.reset({ codigo: e.codigo, nombre: e.nombre, descripcion: e.descripcion || '', colorHex: e.colorHex });
        this.original.set(JSON.stringify(this.form.getRawValue())); this.revision = e.revision;
        this.draft.cargar(e.parametros.map(p => ({ id: p.idParametro, nombre: p.nombre, codigo: p.codigo, descripcion: p.unidad })));
        this.alertasAsociadas.set(e.alertas.length); this.estado.set(e.estado); this.listo.set(true); this.conflicto.set(false);
      }, error: err => this.error.set(mensajeClinico(err)),
    });
  }
  recargar(): void {
    if (this.ocupado()) return;
    if (!this.cambiosPendientes()) { this.cargar(); return; }
    this.confirm.confirmar('Se descartarán los cambios del formulario para cargar la versión actual. ¿Continuar?', 'Descartar y recargar')
      .pipe(take(1), takeUntilDestroyed(this.destroyRef)).subscribe(ok => { if (ok) this.cargar(); });
  }
  agregar(p: ParametroClinico): void { if (!this.ocupado()) this.draft.agregar({ id: p.id, nombre: p.nombre, codigo: p.codigo, descripcion: p.unidad }); }
  color(): string { const value = this.valores().colorHex || ''; return /^#[0-9a-f]{6}$/i.test(value) ? value : 'var(--sal-primary)'; }
  guardar(): void {
    this.form.markAllAsTouched();
    if (this.form.invalid || !this.listo() || this.ocupado() || !this.cambiosPendientes() || this.conflicto()) return;
    const request = { ...this.form.getRawValue(), revision: this.revision, detalles: this.draft.filas().map(p => ({ idParametro: p.id })) };
    this.ocupado.set(true); this.error.set('');
    this.confirm.confirmar(`${this.id === null ? 'Crear' : 'Actualizar'} «${request.nombre}» (${request.codigo}). Color: ${request.colorHex}. ${this.draft.resumen()} ${request.detalles.length === 0 ? 'La etiqueta quedará sin parámetros, disponible para asignación manual.' : ''}`, 'Guardar etiqueta')
      .pipe(take(1), takeUntilDestroyed(this.destroyRef)).subscribe({
        next: ok => {
          if (!ok) { this.ocupado.set(false); return; }
          this.service.guardar(this.id, request).pipe(takeUntilDestroyed(this.destroyRef), finalize(() => this.ocupado.set(false))).subscribe({
            next: e => { this.original.set(JSON.stringify(this.form.getRawValue())); this.draft.cargar(this.draft.filas()); this.ocupado.set(false); this.router.navigate(['/alertaclinica/etiquetas', e.id]); },
            error: err => { this.error.set(mensajeClinico(err)); this.conflicto.set(err.status === 409 && this.id !== null); },
          });
        }, error: err => { this.ocupado.set(false); this.error.set(mensajeClinico(err)); },
      });
  }
}
