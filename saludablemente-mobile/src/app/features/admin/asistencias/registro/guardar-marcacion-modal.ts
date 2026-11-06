import { Component, inject, signal } from '@angular/core';
import { TuiButton, TuiIcon } from '@taiga-ui/core';
import { TuiButtonLoading } from '@taiga-ui/kit';
import { TuiDialogContext } from '@taiga-ui/core/portals/dialog';
import { injectContext } from '@taiga-ui/polymorpheus';

import { AsistenciaService } from '../asistencia-service';
import type { EstadoAsistencia, MarcaAsistencia, TipoAsistencia } from '../models';

export interface CambioMarca {
  idPersona: number;
  nombre: string;
  /** Estado en servidor (null = pendiente). */
  de: EstadoAsistencia | null;
  a: MarcaAsistencia;
}

export interface DatosGuardarMarcacion {
  idActividad: number;
  actividadNombre: string;
  tipoAsistencia: TipoAsistencia;
  latitud: number;
  longitud: number;
  cambios: CambioMarca[];
}

export interface FalloGuardado {
  idPersona: number;
  nombre: string;
  motivo: string;
}

export interface ResultadoGuardado {
  exitosos: number[];
  fallidos: FalloGuardado[];
}

/**
 * Modal de confirmación de Guardar: resumen + lista antes→después,
 * envía una sola cabecera con detalle; guardado atómico.
 */
@Component({
  selector: 'app-guardar-marcacion-modal',
  imports: [TuiButton, TuiButtonLoading, TuiIcon],
  templateUrl: './guardar-marcacion-modal.html',
  styleUrl: './guardar-marcacion-modal.css',
})
export class GuardarMarcacionModal {
  private readonly asistencias = inject(AsistenciaService);
  private readonly context =
    injectContext<TuiDialogContext<ResultadoGuardado | null, DatosGuardarMarcacion>>();

  protected readonly datos = this.context.data;
  protected readonly guardando = signal(false);
  protected readonly hechos = signal(0);
  protected readonly fallidos = signal<FalloGuardado[]>([]);
  protected readonly terminado = signal(false);

  protected readonly resumen = (() => {
    const r = { presentes: 0, tarde: 0, falta: 0 };
    for (const c of this.context.data.cambios) {
      if (c.a === 'PRESENTE') r.presentes++;
      else if (c.a === 'TARDE') r.tarde++;
      else r.falta++;
    }
    return r;
  })();

  protected readonly totalPasos = 1;

  protected etiquetaEstado(e: EstadoAsistencia | null): string {
    if (e == null) return 'Pendiente';
    if (e === 'ASISTIO') return 'Presente';
    if (e === 'TARDE') return 'Tarde';
    if (e === 'AUSENTE') return 'Falta';
    return e;
  }

  protected etiquetaMarca(m: MarcaAsistencia): string {
    return m === 'PRESENTE' ? 'Presente' : m === 'TARDE' ? 'Tarde' : 'Falta';
  }

  cancelar(): void {
    if (this.guardando()) return;
    this.context.completeWith(null);
  }

  confirmar(): void {
    if (this.guardando() || this.terminado()) return;
    const d = this.datos;
    this.guardando.set(true);
    this.hechos.set(0);
    this.fallidos.set([]);

    this.asistencias.guardarManualDetalle({
      idActividad: d.idActividad,
      tipoAsistencia: d.tipoAsistencia,
      latitud: d.latitud,
      longitud: d.longitud,
      detalle: d.cambios.map((c) => ({ idPersona: c.idPersona, marca: c.a })),
    }).subscribe({
      next: () => {
        this.hechos.set(1);
        this.guardando.set(false);
        this.terminado.set(true);
      },
      error: (err) => {
        this.fallidos.set(d.cambios.map((c) => ({
          idPersona: c.idPersona,
          nombre: c.nombre,
          motivo: err?.error?.message ?? 'No se guardó la lista. Intenta nuevamente.',
        })));
        this.guardando.set(false);
        this.terminado.set(true);
      },
    });
  }
  cerrar(): void {
    const exitosos: number[] = [];
    // El envío es atómico; el padre
    // reconcilia por id: todo cambio no fallido cuenta como éxito.
    const idsFallo = new Set(this.fallidos().map((f) => f.idPersona));
    for (const c of this.datos.cambios) {
      if (!idsFallo.has(c.idPersona)) exitosos.push(c.idPersona);
    }
    this.context.completeWith({ exitosos, fallidos: this.fallidos() });
  }
}

