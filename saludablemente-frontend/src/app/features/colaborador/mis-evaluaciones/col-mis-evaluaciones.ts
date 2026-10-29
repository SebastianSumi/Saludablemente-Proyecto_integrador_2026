import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { TuiButton, TuiIcon, TuiInput, TuiTextfield } from '@taiga-ui/core';
import { TuiTable } from '@taiga-ui/addon-table';
import { forkJoin, of } from 'rxjs';
import { catchError, debounceTime, distinctUntilChanged, switchMap } from 'rxjs/operators';
import { PerfilService } from '../../admin/perfil/perfil-service';
import { EvaluacionService as NutSrv } from '../../admin/nutricional/evaluacion/evaluacion-service';
import type { EvaluacionResumen as NutResumen } from '../../admin/nutricional/evaluacion/evaluacion.model';
import { EvaluacionService as AptSrv } from '../../admin/aptitud/evaluacion/evaluacion-service';
import type { EvaluacionResumen as AptResumen } from '../../admin/aptitud/evaluacion/evaluacion.model';
import { imagenPortada } from './imagen-evaluacion';

export type TipoEval = 'nutricional' | 'aptitud';
export type TipoFiltro = 'todo' | TipoEval;
export type ModoVista = 'tarjetas' | 'lista';
export type Orden = 'fecha' | 'tipo';

export interface EvalItem {
  tipo: TipoEval;
  id: number;
  /** Fecha yyyy-MM-dd para ordenar/filtrar. */
  fecha: string;
  titulo: string;
  subtitulo: string;
  estado: string;
  ruta: (string | number)[];
  periodo?: string;
  dictamen?: string;
  fitnessScore?: number | null;
  area?: string;
}

@Component({
  selector: 'app-col-evals',
  standalone: true,
  imports: [RouterLink, ReactiveFormsModule, DatePipe, TuiButton, TuiIcon, TuiInput, TuiTextfield, TuiTable],
  templateUrl: './col-mis-evaluaciones.html',
  styleUrls: ['./col-mis-evaluaciones.css'],
})
export class ColMisEvaluaciones implements OnInit {
  private readonly perfilSrv = inject(PerfilService);
  private readonly nutSrv = inject(NutSrv);
  private readonly aptSrv = inject(AptSrv);

  protected readonly portada = imagenPortada();

  protected readonly nut = signal<NutResumen[]>([]);
  protected readonly apt = signal<AptResumen[]>([]);
  protected readonly loading = signal(false);
  protected readonly error = signal<string | null>(null);

  protected readonly tipo = signal<TipoFiltro>('todo');
  protected readonly estado = signal('');
  protected readonly texto = signal('');
  protected readonly desde = signal('');
  protected readonly hasta = signal('');
  protected readonly orden = signal<Orden>('fecha');
  protected readonly modo = signal<ModoVista>('tarjetas');
  protected readonly expandido = signal<string | null>(null);

  protected readonly textoCtrl = new FormControl('', { nonNullable: true });

  /** Historial unificado (sin filtrar) de ambos tipos. */
  private readonly todas = computed<EvalItem[]>(() => {
    const out: EvalItem[] = [];
    for (const n of this.nut()) {
      out.push({
        tipo: 'nutricional',
        id: n.idEvaluacion,
        fecha: (n.fechaEvaluacion ?? '').slice(0, 10),
        titulo: `Evaluación nutricional #${n.idEvaluacion}`,
        subtitulo: `Periodo ${n.idPeriodo}`,
        estado: n.estadoEvaluacion,
        ruta: ['/colaborador/evaluaciones/nutricional', n.idEvaluacion],
        periodo: n.idPeriodo,
        area: n.areaTrabajoAlMomento,
      });
    }
    for (const a of this.apt()) {
      out.push({
        tipo: 'aptitud',
        id: a.id,
        fecha: (a.fechaEvaluacion ?? '').slice(0, 10),
        titulo: `Evaluación de aptitud #${a.id}`,
        subtitulo: `${this.etiquetaDictamen(a.dictamenGlobal)}${a.fitnessScore != null ? ` · ${a.fitnessScore} pts` : ''}`,
        estado: a.estadoEvaluacion ?? 'PENDIENTE',
        ruta: ['/colaborador/evaluaciones/aptitud', a.id],
        dictamen: a.dictamenGlobal,
        fitnessScore: a.fitnessScore ?? null,
        area: a.areaTrabajoAlMomento,
      });
    }
    return out;
  });

  protected readonly conteos = computed(() => {
    const all = this.todas();
    const por = (fn: (e: EvalItem) => boolean): number => all.filter(fn).length;
    return {
      total: all.length,
      nutricional: por((e) => e.tipo === 'nutricional'),
      aptitud: por((e) => e.tipo === 'aptitud'),
      completas: por((e) => e.estado === 'COMPLETA'),
      pendientes: por((e) => e.estado === 'PENDIENTE'),
      anuladas: por((e) => e.estado === 'ANULADA'),
    };
  });

  protected readonly evaluaciones = computed<EvalItem[]>(() => {
    const tipo = this.tipo();
    const estado = this.estado();
    const texto = this.texto().trim().toLowerCase();
    const desde = this.desde();
    const hasta = this.hasta();

    const arr = this.todas().filter((e) => {
      if (tipo !== 'todo' && e.tipo !== tipo) return false;
      if (estado && e.estado !== estado) return false;
      if (desde && e.fecha < desde) return false;
      if (hasta && e.fecha > hasta) return false;
      if (texto) {
        const blob = [e.titulo, e.subtitulo, e.area ?? '', this.etiquetaTipo(e.tipo)]
          .join(' ')
          .toLowerCase();
        if (!blob.includes(texto)) return false;
      }
      return true;
    });

    return arr.sort((x, y) =>
      this.orden() === 'tipo'
        ? x.tipo.localeCompare(y.tipo) || y.fecha.localeCompare(x.fecha)
        : y.fecha.localeCompare(x.fecha),
    );
  });

  ngOnInit(): void {
    this.textoCtrl.valueChanges
      .pipe(debounceTime(300), distinctUntilChanged())
      .subscribe((v) => this.texto.set(v ?? ''));
    this.cargar();
  }

  cargar(): void {
    this.loading.set(true);
    this.error.set(null);
    this.perfilSrv
      .obtener()
      .pipe(
        switchMap((p) =>
          forkJoin({
            nut: this.nutSrv.historialPorPersona(p.idPersona).pipe(catchError(() => of([] as NutResumen[]))),
            apt: this.aptSrv.historial(p.idPersona).pipe(catchError(() => of([] as AptResumen[]))),
          }),
        ),
      )
      .subscribe({
        next: ({ nut, apt }) => {
          this.nut.set(nut);
          this.apt.set(apt);
          this.loading.set(false);
        },
        error: (err: HttpErrorResponse | Error) => {
          this.error.set(this.mapearError(err));
          this.loading.set(false);
        },
      });
  }

  protected filtrarTipo(valor: TipoFiltro): void {
    this.tipo.set(valor);
  }

  protected filtrarEstado(valor: string): void {
    this.estado.set(valor);
  }

  protected setOrden(valor: Orden): void {
    this.orden.set(valor);
  }

  protected toggleExpandir(clave: string): void {
    this.expandido.set(this.expandido() === clave ? null : clave);
  }

  protected limpiarFiltros(): void {
    this.tipo.set('todo');
    this.estado.set('');
    this.texto.set('');
    this.desde.set('');
    this.hasta.set('');
    this.orden.set('fecha');
    this.textoCtrl.setValue('', { emitEvent: false });
    this.expandido.set(null);
  }

  protected clave(e: EvalItem): string {
    return `${e.tipo}-${e.id}`;
  }

  protected etiquetaTipo(tipo: TipoEval): string {
    return tipo === 'nutricional' ? 'Nutricional' : 'Aptitud física';
  }

  protected iconoTipo(tipo: TipoEval): string {
    return tipo === 'nutricional' ? '@tui.apple' : '@tui.activity';
  }

  protected tileTipo(tipo: TipoEval): string {
    return tipo === 'nutricional'
      ? 'tile tile-rounded tile-soft-success'
      : 'tile tile-rounded tile-soft-violet';
  }

  protected pillTipo(tipo: TipoEval): string {
    return tipo === 'nutricional' ? 'pill pill-success' : 'pill pill-violet';
  }

  protected etiquetaEstado(estado: string): string {
    switch (estado) {
      case 'COMPLETA':
        return 'Completa';
      case 'ANULADA':
        return 'Anulada';
      default:
        return 'Pendiente';
    }
  }

  protected pillEstado(estado: string): string {
    switch (estado) {
      case 'COMPLETA':
        return 'pill pill-success';
      case 'ANULADA':
        return 'pill pill-error';
      default:
        return 'pill pill-warning';
    }
  }

  protected pillDictamen(dictamen?: string): string {
    switch (dictamen) {
      case 'APTO':
        return 'pill pill-success';
      case 'APTO_CON_RESTRICCIONES':
        return 'pill pill-warning';
      case 'NO_APTO':
        return 'pill pill-error';
      default:
        return 'pill pill-neutral';
    }
  }

  protected etiquetaDictamen(dictamen?: string): string {
    switch (dictamen) {
      case 'APTO':
        return 'Apto';
      case 'APTO_CON_RESTRICCIONES':
        return 'Apto con restricciones';
      case 'NO_APTO':
        return 'No apto';
      default:
        return 'Sin dictamen';
    }
  }

  private mapearError(err: HttpErrorResponse | Error): string {
    if (err instanceof Error && !(err instanceof HttpErrorResponse)) return err.message;
    const h = err as HttpErrorResponse;
    if (h.status === 0) return 'No se pudo conectar con el servidor.';
    return (h.error as { message?: string } | undefined)?.message ?? 'No se pudo cargar tus evaluaciones.';
  }
}
