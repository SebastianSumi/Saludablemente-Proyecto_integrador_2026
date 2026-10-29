import { computed, signal } from '@angular/core';
import { FormArray, FormControl } from '@angular/forms';
import { FilaAsociacion } from './etiqueta.model';

/** Coleccion de detalle: no persiste nada hasta confirmar el conjunto completo. */
export class AsociacionesDraft {
  readonly form = new FormArray<FormControl<FilaAsociacion>>([]);
  readonly filas = signal<FilaAsociacion[]>([]);
  readonly originales = signal<FilaAsociacion[]>([]);
  readonly agregadas = computed(() => this.filas().filter(f => !this.originales().some(o => o.id === f.id)));
  readonly retiradas = computed(() => this.originales().filter(o => !this.filas().some(f => f.id === o.id)));
  readonly pendiente = computed(() => this.agregadas().length > 0 || this.retiradas().length > 0);
  cargar(filas: FilaAsociacion[]): void { this.originales.set([...filas]); this.reemplazar(filas); }
  agregar(fila: FilaAsociacion): boolean {
    if (this.filas().length >= 500 || this.contiene(fila.id)) return false;
    const original = this.originales().find(f => f.id === fila.id);
    this.form.push(new FormControl(original || fila, { nonNullable: true })); this.sincronizar(); return true;
  }
  quitar(id: number): void {
    const index = this.filas().findIndex(f => f.id === id);
    if (index < 0 || this.filas()[index].origen === 'MOTOR') return;
    this.form.removeAt(index); this.sincronizar();
  }
  contiene(id: number): boolean { return this.filas().some(f => f.id === id); }
  esNueva(id: number): boolean { return !this.originales().some(f => f.id === id); }
  cancelar(): void { this.reemplazar(this.originales()); }
  resumen(): string {
    const agregadas = this.agregadas().map(f => f.nombre).join(', ') || 'Ninguna';
    const retiradas = this.retiradas().map(f => f.nombre).join(', ') || 'Ninguna';
    return `Agregar: ${agregadas}. Retirar: ${retiradas}. Total final: ${this.filas().length}.`;
  }
  private reemplazar(filas: FilaAsociacion[]): void {
    this.form.clear(); for (const fila of filas) this.form.push(new FormControl(fila, { nonNullable: true })); this.sincronizar();
  }
  private sincronizar(): void { this.filas.set(this.form.getRawValue()); }
}
