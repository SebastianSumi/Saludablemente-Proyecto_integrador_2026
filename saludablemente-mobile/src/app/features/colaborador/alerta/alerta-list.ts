import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { Subscription } from 'rxjs';
import { EtiquetasClinicas } from '../../../shared/components/etiquetas-clinicas/etiquetas-clinicas';
import { Component, DestroyRef, OnInit, computed, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { TuiButton, TuiIcon, TuiInput, TuiTextfield } from '@taiga-ui/core';
import { TuiTable } from '@taiga-ui/addon-table';
import { debounceTime, distinctUntilChanged } from 'rxjs/operators';
import { ColAlertaService } from './alerta-service';
import { imagenPortada } from '../../admin/alertaclinica/ilustracion-alerta';
import type { AlertaClinica, EstadoAlerta, SeveridadAlerta, TipoEvaluacionOrigen } from './alerta.model';

type ModoVista = 'lista' | 'tarjetas';
type Orden = 'fecha' | 'puntaje' | 'severidad';

@Component({
  selector: 'app-col-alerta-list',
  standalone: true,
  imports: [EtiquetasClinicas,DatePipe, RouterLink, ReactiveFormsModule, TuiButton, TuiIcon, TuiInput, TuiTextfield, TuiTable],
  templateUrl: './alerta-list.html',
  styleUrls: ['./alerta-list.css', './alerta-list-resultados.css'],
})
export class ColAlertaList implements OnInit {
  private static readonly RANGO_SEV: Record<SeveridadAlerta, number> = {
    CRITICA: 0,
    MODERADA: 1,
    LEVE: 2,
  };

  private readonly service = inject(ColAlertaService);

  private readonly destroyRef = inject(DestroyRef);
  private consulta?: Subscription;
  protected readonly filtroEtiqueta = signal('');
  protected readonly etiquetasDisponibles = computed(() => {
    const etiquetas = new Map<string, { codigo: string; nombre: string; cantidad: number }>();
    for (const alerta of this.items()) {
      const vistas = new Set<string>();
      for (const e of alerta.etiquetas || []) {
        if (vistas.has(e.codigo)) continue;
        vistas.add(e.codigo);
        const anterior = etiquetas.get(e.codigo);
        if (anterior) anterior.cantidad++;
        else etiquetas.set(e.codigo, { codigo: e.codigo, nombre: e.nombre, cantidad: 1 });
      }
    }
    return [...etiquetas.values()].sort((a, b) => a.nombre.localeCompare(b.nombre, 'es'));
  });
  protected readonly portada = imagenPortada();

  protected readonly items = signal<AlertaClinica[]>([]);
  protected readonly loading = signal(false);
  protected readonly error = signal<string | null>(null);

  protected readonly modo = signal<ModoVista>('tarjetas');
  protected readonly expandido = signal<number | null>(null);

  protected readonly filtroEstado = signal<EstadoAlerta | ''>('PENDIENTE');
  protected readonly filtroSeveridad = signal<SeveridadAlerta | ''>('');
  protected readonly filtroTipo = signal<TipoEvaluacionOrigen | ''>('');
  protected readonly filtroDesde = signal('');
  protected readonly filtroHasta = signal('');
  protected readonly filtroTexto = signal('');
  protected readonly orden = signal<Orden>('fecha');

  protected readonly textoCtrl = new FormControl('', { nonNullable: true });

  protected readonly conteos = computed(() => {
    const all = this.items();
    const por = (fn: (a: AlertaClinica) => boolean): number => all.filter(fn).length;
    return {
      total: all.length,
      pendientes: por((a) => a.estado === 'PENDIENTE'),
      atendidas: por((a) => a.estado === 'ATENDIDA'),
      anuladas: por((a) => a.estado === 'ANULADA'),
      criticas: por((a) => a.nivelSeveridad === 'CRITICA'),
      moderadas: por((a) => a.nivelSeveridad === 'MODERADA'),
      leves: por((a) => a.nivelSeveridad === 'LEVE'),
      nutricional: por((a) => a.tipoEvaluacionOrigen === 'NUTRICIONAL'),
      aptitud: por((a) => a.tipoEvaluacionOrigen === 'APTITUD_FISICA'),
    };
  });

  protected readonly filtradas = computed(() => {
    const etiqueta = this.filtroEtiqueta();
    const estado = this.filtroEstado();
    const sev = this.filtroSeveridad();
    const tipo = this.filtroTipo();
    const desde = this.filtroDesde();
    const hasta = this.filtroHasta();
    const texto = this.filtroTexto().trim().toLowerCase();
    return this.items().filter((a) => {
      if (etiqueta && !(a.etiquetas || []).some(e => e.codigo === etiqueta)) return false;
      if (estado && a.estado !== estado) return false;
      if (sev && a.nivelSeveridad !== sev) return false;
      if (tipo && a.tipoEvaluacionOrigen !== tipo) return false;
      const dia = (a.fechaGeneracion ?? '').slice(0, 10);
      if (desde && dia < desde) return false;
      if (hasta && dia > hasta) return false;
      if (texto) {
        const blob = [
          a.mensajeResumen,
          ...(a.etiquetas || []).map(e => `${e.nombre} ${e.codigo}`),
          this.etiquetaOrigen(a.tipoEvaluacionOrigen),
          ...a.detalles.map((d) => `${d.tipoIndicador} ${d.mensaje}`),
        ]
          .join(' ')
          .toLowerCase();
        if (!blob.includes(texto)) return false;
      }
      return true;
    });
  });

  protected readonly ordenadas = computed(() => {
    const arr = [...this.filtradas()];
    switch (this.orden()) {
      case 'puntaje':
        arr.sort((a, b) => b.puntajeTotal - a.puntajeTotal);
        break;
      case 'severidad':
        arr.sort(
          (a, b) =>
            ColAlertaList.RANGO_SEV[a.nivelSeveridad] - ColAlertaList.RANGO_SEV[b.nivelSeveridad],
        );
        break;
      default:
        arr.sort((a, b) => (b.fechaGeneracion ?? '').localeCompare(a.fechaGeneracion ?? ''));
    }
    return arr;
  });

  ngOnInit(): void {
    this.textoCtrl.valueChanges
      .pipe(debounceTime(300), distinctUntilChanged(), takeUntilDestroyed(this.destroyRef))
      .subscribe((v) => this.filtroTexto.set(v ?? ''));
    this.cargar();
  }

  cargar(): void {
    this.consulta?.unsubscribe();
    this.loading.set(true);
    this.error.set(null);
    this.consulta = this.service.listarMias().pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: (data) => {
        this.items.set(data);
        if (this.filtroEtiqueta() && !data.some(a => (a.etiquetas || []).some(e => e.codigo === this.filtroEtiqueta())))
          this.filtroEtiqueta.set('');
      },
      error: (err: HttpErrorResponse | Error) => {
        this.error.set(this.mapearError(err));
        this.loading.set(false);
      },
      complete: () => this.loading.set(false),
    });
  }

  protected filtrarEstado(valor: EstadoAlerta | ''): void {
    this.filtroEstado.set(valor);
  }

  protected filtrarSeveridad(valor: SeveridadAlerta | ''): void {
    this.filtroSeveridad.set(valor);
  }

  protected filtrarTipo(valor: TipoEvaluacionOrigen | ''): void {
    this.filtroTipo.set(valor);
  }

  protected setOrden(valor: Orden): void {
    this.orden.set(valor);
  }

  protected toggleExpandir(id: number): void {
    this.expandido.set(this.expandido() === id ? null : id);
  }

  protected limpiarFiltros(): void {
    this.filtroEtiqueta.set('');
    this.filtroEstado.set('PENDIENTE');
    this.filtroSeveridad.set('');
    this.filtroTipo.set('');
    this.filtroDesde.set('');
    this.filtroHasta.set('');
    this.filtroTexto.set('');
    this.orden.set('fecha');
    this.textoCtrl.setValue('', { emitEvent: false });
    this.expandido.set(null);
  }

  protected etiquetaSeveridad(sev: SeveridadAlerta): string {
    return sev === 'CRITICA' ? 'Crítica' : sev === 'MODERADA' ? 'Moderada' : 'Leve';
  }

  protected pillSeveridad(sev: SeveridadAlerta): string {
    return sev === 'CRITICA'
      ? 'pill pill-error'
      : sev === 'MODERADA'
        ? 'pill pill-warning'
        : 'pill pill-success';
  }

  protected tileSeveridad(sev: SeveridadAlerta): string {
    return sev === 'CRITICA'
      ? 'tile tile-rounded tile-soft-error'
      : sev === 'MODERADA'
        ? 'tile tile-rounded tile-soft-warning'
        : 'tile tile-rounded tile-soft-success';
  }

  protected iconoSeveridad(sev: SeveridadAlerta): string {
    return sev === 'CRITICA'
      ? '@tui.triangle-alert'
      : sev === 'MODERADA'
        ? '@tui.info'
        : '@tui.check';
  }

  protected etiquetaEstado(estado: EstadoAlerta): string {
    return estado === 'PENDIENTE' ? 'Pendiente' : estado === 'ATENDIDA' ? 'Atendida' : 'Anulada';
  }

  protected pillEstado(estado: EstadoAlerta): string {
    return estado === 'PENDIENTE'
      ? 'pill pill-warning'
      : estado === 'ATENDIDA'
        ? 'pill pill-success'
        : 'pill pill-neutral';
  }

  protected etiquetaOrigen(tipo: TipoEvaluacionOrigen): string {
    return tipo === 'NUTRICIONAL' ? 'Nutricional' : 'Aptitud física';
  }

  protected iconoOrigen(tipo: TipoEvaluacionOrigen): string {
    return tipo === 'NUTRICIONAL' ? '@tui.apple' : '@tui.activity';
  }

  protected pillOrigen(tipo: TipoEvaluacionOrigen): string {
    return tipo === 'NUTRICIONAL' ? 'pill pill-success' : 'pill pill-violet';
  }

  protected rutaEvaluacion(a: AlertaClinica): string[] {
    const base = a.tipoEvaluacionOrigen === 'NUTRICIONAL' ? 'nutricional' : 'aptitud';
    return ['/colaborador/evaluaciones', base, String(a.evaluacionOrigenId)];
  }

  protected rango(valor: number | null): string {
    return valor === null ? '—' : String(valor);
  }

  private mapearError(err: HttpErrorResponse | Error): string {
    if (err instanceof Error && !(err instanceof HttpErrorResponse)) return err.message;
    const h = err as HttpErrorResponse;
    if (h.status === 0) return 'No se pudo conectar con el servidor.';
    if (h.status === 404) return 'No se encontró el recurso.';
    if (h.status === 500) return 'Error interno del servidor.';
    return (h.error as { message?: string } | undefined)?.message ?? 'No se pudo cargar tu lista.';
  }
}
