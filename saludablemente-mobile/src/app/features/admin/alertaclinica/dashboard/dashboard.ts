import { EtiquetaDashboardCard } from '../etiquetas/etiqueta-dashboard-card';
import { ClinicaNav } from '../etiquetas/clinica-nav';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { FormControl, FormsModule, ReactiveFormsModule } from '@angular/forms';
import {
  TuiButton,
  TuiDropdown,
  TuiFilterByInputPipe,
  TuiIcon,
  TuiTextfield,
  tuiItemsHandlersProvider,
} from '@taiga-ui/core';
import { TuiBadge, TuiCalendarRange, TuiChevron, TuiComboBox, TuiDataListWrapper, TuiInputDate, TuiSegmented } from '@taiga-ui/kit';
import { TuiDayRange, TuiDay } from '@taiga-ui/cdk/date-time';
import { PeriodoService } from '../../catalogo/periodo/periodo-service';
import { Periodo } from '../../catalogo/periodo/periodo.model';
import { NgxEchartsDirective } from 'ngx-echarts';
import type { EChartsOption } from 'echarts';
import { catchError, forkJoin, of } from 'rxjs';
import { AlertaClinicaService } from '../alerta/alerta-clinica-service';
import {
  AlertaClinica,
  SeveridadAlerta,
  TipoEvaluacionOrigen,
} from '../alerta/alerta-clinica.model';
import { PersonaNombreService } from '../alerta/persona-nombre-service';
import { ThemeService } from '../../../../core/theme/theme-service';

// -------------------------------------------------------------------
// Funciones puras (testeables): todo el dashboard deriva de buscar()
// -------------------------------------------------------------------

export type RangoDias = 7 | 14 | 30 | 0;

export interface Tendencia {
  dias: string[];
  leve: number[];
  moderada: number[];
  critica: number[];
}

export interface TopPersona {
  personaId: number;
  nombre: string;
  total: number;
  criticas: number;
  maxPuntaje: number;
  ultimaFecha: string;
  idAlertaReciente: number;
}

export function contarPorSeveridad(
  alertas: AlertaClinica[],
): Record<SeveridadAlerta, number> {
  const r: Record<SeveridadAlerta, number> = { LEVE: 0, MODERADA: 0, CRITICA: 0 };
  for (const a of alertas) {
    if (a.nivelSeveridad in r) r[a.nivelSeveridad] += 1;
  }
  return r;
}

export function contarPorOrigen(alertas: AlertaClinica[]): {
  NUTRICIONAL: number;
  APTITUD_FISICA: number;
} {
  const r = { NUTRICIONAL: 0, APTITUD_FISICA: 0 };
  for (const a of alertas) {
    if (a.tipoEvaluacionOrigen in r) {
      r[a.tipoEvaluacionOrigen as TipoEvaluacionOrigen] += 1;
    }
  }
  return r;
}

export function tasaAtencion(alertas: AlertaClinica[]): number {
  if (alertas.length === 0) return 0;
  const atendidas = alertas.filter((a) => a.estado === 'ATENDIDA').length;
  return Math.round((atendidas / alertas.length) * 100);
}

function claveDia(d: Date): string {
  return `${d.getFullYear()}-${d.getMonth()}-${d.getDate()}`;
}

/**
 * Rango de fechas (ISO) que cubre un periodo académico.
 * El backend de alertas no filtra por periodo, así que se traduce a desde/hasta.
 */
/** Parsea 'YYYY-MM-DD' (o ISO con hora) como fecha local, no UTC. */
function diaLocal(fecha: string, finDelDia: boolean): Date {
  const m = /^(\d{4})-(\d{2})-(\d{2})/.exec(fecha);
  const d = m
    ? new Date(Number(m[1]), Number(m[2]) - 1, Number(m[3]))
    : new Date(fecha);
  if (finDelDia) d.setHours(23, 59, 59, 999);
  else d.setHours(0, 0, 0, 0);
  return d;
}

export function rangoFechasPeriodo(p: Periodo): { desde: string; hasta: string } {
  const desde = diaLocal(p.fechaInicio, false);
  const hasta = diaLocal(p.fechaFin, true);
  return { desde: desde.toISOString(), hasta: hasta.toISOString() };
}

function etiquetaDia(d: Date): string {
  return `${String(d.getDate()).padStart(2, '0')}/${String(d.getMonth() + 1).padStart(2, '0')}`;
}

/**
 * Agrupa por día si el rango es de 31 días o menos; por semana si es mayor
 * (un semestre completo en barras diarias sería ilegible).
 */
export function agruparPorRango(
  alertas: AlertaClinica[],
  desde: Date,
  hasta: Date,
): Tendencia {
  const inicio = new Date(desde);
  inicio.setHours(0, 0, 0, 0);
  const fin = new Date(hasta);
  fin.setHours(0, 0, 0, 0);
  const spanDias = Math.max(1, Math.round((fin.getTime() - inicio.getTime()) / 86400000) + 1);
  const semanal = spanDias > 31;
  const paso = semanal ? 7 : 1;

  const etiquetas: string[] = [];
  const inicios: Date[] = [];
  for (let d = new Date(inicio); d <= fin; d.setDate(d.getDate() + paso)) {
    inicios.push(new Date(d));
    etiquetas.push(semanal ? `sem ${etiquetaDia(d)}` : etiquetaDia(d));
  }
  const leve = new Array<number>(etiquetas.length).fill(0);
  const moderada = new Array<number>(etiquetas.length).fill(0);
  const critica = new Array<number>(etiquetas.length).fill(0);
  for (const a of alertas) {
    const f = new Date(a.fechaGeneracion);
    if (Number.isNaN(f.getTime())) continue;
    f.setHours(0, 0, 0, 0);
    if (f < inicio || f > fin) continue;
    const pos = Math.min(
      etiquetas.length - 1,
      Math.floor((f.getTime() - inicio.getTime()) / 86400000 / paso),
    );
    if (a.nivelSeveridad === 'CRITICA') critica[pos] += 1;
    else if (a.nivelSeveridad === 'MODERADA') moderada[pos] += 1;
    else leve[pos] += 1;
  }
  return { dias: etiquetas, leve, moderada, critica };
}

export function agruparPorDia(alertas: AlertaClinica[], dias: number): Tendencia {
  const hoy = new Date();
  hoy.setHours(0, 0, 0, 0);
  const etiquetas: string[] = [];
  const indice = new Map<string, number>();
  for (let i = dias - 1; i >= 0; i--) {
    const d = new Date(hoy);
    d.setDate(hoy.getDate() - i);
    indice.set(claveDia(d), dias - 1 - i);
    etiquetas.push(
      `${String(d.getDate()).padStart(2, '0')}/${String(d.getMonth() + 1).padStart(2, '0')}`,
    );
  }
  const leve = new Array<number>(dias).fill(0);
  const moderada = new Array<number>(dias).fill(0);
  const critica = new Array<number>(dias).fill(0);
  for (const a of alertas) {
    const f = new Date(a.fechaGeneracion);
    if (Number.isNaN(f.getTime())) continue;
    const pos = indice.get(claveDia(f));
    if (pos === undefined) continue;
    if (a.nivelSeveridad === 'CRITICA') critica[pos] += 1;
    else if (a.nivelSeveridad === 'MODERADA') moderada[pos] += 1;
    else leve[pos] += 1;
  }
  return { dias: etiquetas, leve, moderada, critica };
}

export function topPersonasEnRiesgo(
  alertas: AlertaClinica[],
  nombres: Map<number, string>,
  top = 8,
): TopPersona[] {
  const porPersona = new Map<number, AlertaClinica[]>();
  for (const a of alertas) {
    if (a.estado !== 'PENDIENTE') continue;
    const lista = porPersona.get(a.personaId) ?? [];
    lista.push(a);
    porPersona.set(a.personaId, lista);
  }
  const filas: TopPersona[] = [];
  for (const [personaId, lista] of porPersona) {
    const ordenadas = [...lista].sort((x, y) =>
      y.fechaGeneracion.localeCompare(x.fechaGeneracion),
    );
    filas.push({
      personaId,
      nombre: nombres.get(personaId) ?? `Persona #${personaId}`,
      total: lista.length,
      criticas: lista.filter((a) => a.nivelSeveridad === 'CRITICA').length,
      maxPuntaje: Math.max(...lista.map((a) => a.puntajeTotal)),
      ultimaFecha: ordenadas[0].fechaGeneracion,
      idAlertaReciente: ordenadas[0].idAlerta,
    });
  }
  return filas
    .sort((x, y) => y.criticas - x.criticas || y.total - x.total || y.maxPuntaje - x.maxPuntaje)
    .slice(0, top);
}

// -------------------------------------------------------------------
// Opciones ECharts (paleta válida en tema claro y oscuro)
// -------------------------------------------------------------------

const C_LEVE = '#16cdc7';
const C_MODERADA = '#ffae1f';
const C_CRITICA = '#e57373';
const C_NUTRI = '#00a1ff';
const C_APTITUD = '#8965e5';

export interface TemaGrafico {
  texto: string;
  titulo: string;
  linea: string;
  fondo: string;
  borde: string;
}

const TEMA_POR_DEFECTO: TemaGrafico = {
  texto: '#5a6b7c',
  titulo: '#111c2d',
  linea: '#e4ebf0',
  fondo: '#ffffff',
  borde: '#e4ebf0',
};

/** Lee la paleta actual del documento (claro/oscuro). Seguro sin DOM (tests/SSR). */
export function temaGrafico(): TemaGrafico {
  try {
    if (typeof document === 'undefined' || typeof getComputedStyle === 'undefined') {
      return { ...TEMA_POR_DEFECTO };
    }
    const css = getComputedStyle(document.body);
    const leer = (variable: string, fallback: string): string =>
      css.getPropertyValue(variable).trim() || fallback;
    return {
      texto: leer('--sal-ink-soft', TEMA_POR_DEFECTO.texto),
      titulo: leer('--sal-ink', TEMA_POR_DEFECTO.titulo),
      linea: leer('--sal-line', TEMA_POR_DEFECTO.linea),
      fondo: leer('--sal-card', TEMA_POR_DEFECTO.fondo),
      borde: leer('--sal-line', TEMA_POR_DEFECTO.borde),
    };
  } catch {
    return { ...TEMA_POR_DEFECTO };
  }
}

export function construirOpcionesTendencia(t: Tendencia, tema: TemaGrafico = temaGrafico()): EChartsOption {
  return {
    tooltip: {
      trigger: 'axis',
      backgroundColor: tema.fondo,
      borderColor: tema.borde,
      textStyle: { color: tema.titulo },
    },
    legend: { bottom: 0, textStyle: { color: tema.texto } },
    grid: { left: '6%', right: '4%', bottom: '14%', top: '8%', containLabel: true },
    xAxis: {
      type: 'category',
      data: t.dias,
      axisLabel: { color: tema.texto, fontSize: 10 },
    },
    yAxis: {
      type: 'value',
      minInterval: 1,
      axisLabel: { color: tema.texto },
      splitLine: { lineStyle: { color: tema.linea } },
    },
    series: [
      { name: 'Leves', type: 'bar', stack: 'total', data: t.leve, itemStyle: { color: C_LEVE } },
      { name: 'Moderadas', type: 'bar', stack: 'total', data: t.moderada, itemStyle: { color: C_MODERADA } },
      { name: 'Críticas', type: 'bar', stack: 'total', data: t.critica, itemStyle: { color: C_CRITICA } },
    ],
  };
}

export function construirOpcionesSeveridad(
  sev: Record<SeveridadAlerta, number>,
  tema: TemaGrafico = temaGrafico(),
): EChartsOption {
  return {
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c}',
      backgroundColor: tema.fondo,
      borderColor: tema.borde,
      textStyle: { color: tema.titulo },
    },
    legend: { bottom: 0, textStyle: { color: tema.texto } },
    series: [
      {
        type: 'pie',
        radius: ['48%', '70%'],
        center: ['50%', '44%'],
        label: { show: false },
        data: [
          { name: 'Leves', value: sev.LEVE, itemStyle: { color: C_LEVE } },
          { name: 'Moderadas', value: sev.MODERADA, itemStyle: { color: C_MODERADA } },
          { name: 'Críticas', value: sev.CRITICA, itemStyle: { color: C_CRITICA } },
        ],
      },
    ],
  };
}

export function construirOpcionesOrigen(
  origen: {
    NUTRICIONAL: number;
    APTITUD_FISICA: number;
  },
  tema: TemaGrafico = temaGrafico(),
): EChartsOption {
  return {
    tooltip: {
      trigger: 'axis',
      backgroundColor: tema.fondo,
      borderColor: tema.borde,
      textStyle: { color: tema.titulo },
    },
    grid: { left: '6%', right: '8%', bottom: '6%', top: '12%', containLabel: true },
    xAxis: {
      type: 'value',
      minInterval: 1,
      axisLabel: { color: tema.texto },
      splitLine: { lineStyle: { color: tema.linea } },
    },
    yAxis: {
      type: 'category',
      data: ['Nutricional', 'Aptitud física'],
      axisLabel: { color: tema.texto },
    },
    series: [
      {
        type: 'bar',
        data: [
          { value: origen.NUTRICIONAL, itemStyle: { color: C_NUTRI, borderRadius: [0, 8, 8, 0] } },
          { value: origen.APTITUD_FISICA, itemStyle: { color: C_APTITUD, borderRadius: [0, 8, 8, 0] } },
        ],
        label: { show: true, position: 'right', color: tema.texto },
      },
    ],
  };
}

function tieneCanvas(): boolean {
  try {
    if (typeof document === 'undefined') return false;
    if (typeof ResizeObserver === 'undefined') return false;
    const ctx = document.createElement('canvas').getContext?.('2d');
    return !!ctx;
  } catch {
    return false;
  }
}

// -------------------------------------------------------------------
// Componente
// -------------------------------------------------------------------

import { IMG_DASHBOARD_CLINICO } from '../ilustracion-alerta';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [ClinicaNav, EtiquetaDashboardCard,
    CommonModule,
    RouterLink,
    FormsModule,
    ReactiveFormsModule,
    TuiButton,
    TuiBadge,
    TuiCalendarRange,
    TuiChevron,
    TuiComboBox,
    TuiDataListWrapper,
    TuiDropdown,
    TuiFilterByInputPipe,
    TuiIcon,
    TuiInputDate,
    TuiTextfield,
    TuiSegmented,
    NgxEchartsDirective,
  ],
  templateUrl: './dashboard.html',
  styleUrls: ['./dashboard.css'],
  providers: [
    tuiItemsHandlersProvider({
      stringify: signal(
        (item: unknown): string => (item as { label?: string } | null)?.label ?? '',
      ),
      identityMatcher: signal(
        (a: unknown, b: unknown): boolean =>
          ((a as { id?: unknown } | null)?.id ?? null) ===
          ((b as { id?: unknown } | null)?.id ?? null),
      ),
    }),
  ],
})

export class Dashboard implements OnInit {
  protected readonly portada = IMG_DASHBOARD_CLINICO;
  private readonly alertaService = inject(AlertaClinicaService);
  private readonly nombresSrv = inject(PersonaNombreService);
  private readonly periodoSrv = inject(PeriodoService);
  private readonly theme = inject(ThemeService);

  protected readonly graficosActivos = signal(tieneCanvas());
  protected readonly items = signal<AlertaClinica[]>([]);
  protected readonly nombres = signal<Map<number, string>>(new Map());
  protected readonly periodos = signal<Periodo[]>([]);
  protected readonly loading = signal(false);
  protected readonly error = signal<string | null>(null);

  protected readonly rangoDias = signal<RangoDias>(0);
  protected readonly filtroPeriodo = signal<string>('');
  protected readonly filtroSeveridad = signal<SeveridadAlerta | ''>('');
  protected readonly filtroOrigen = signal<TipoEvaluacionOrigen | ''>('');

  protected readonly rangos: { id: RangoDias; label: string }[] = [
    { id: 7, label: '7 días' },
    { id: 14, label: '14 días' },
    { id: 30, label: '30 días' },
    { id: 0, label: 'Todo' },
  ];
  protected readonly severidades: { id: SeveridadAlerta | ''; label: string }[] = [
    { id: '', label: 'Todas' },
    { id: 'LEVE', label: 'Leve' },
    { id: 'MODERADA', label: 'Moderada' },
    { id: 'CRITICA', label: 'Crítica' },
  ];
  protected readonly origenes: { id: TipoEvaluacionOrigen | ''; label: string }[] = [
    { id: '', label: 'Todos' },
    { id: 'NUTRICIONAL', label: 'Nutricional' },
    { id: 'APTITUD_FISICA', label: 'Aptitud física' },
  ];

  // ---- Opciones para los segmented ----
  protected readonly periodoOpts = computed(() => [
    { id: '', label: 'Todo' },
    ...this.periodos().map((p) => ({ id: p.codigo, label: p.codigo })),
  ]);

  protected readonly periodoCtrl = new FormControl<{ id: string; label: string } | null>(null);

  protected readonly rangoCtrl = new FormControl<TuiDayRange | null>(null);

  protected idxRango = computed(() =>
    Math.max(
      0,
      this.rangos.findIndex((r) => r.id === this.rangoDias()),
    ),
  );
  protected idxSeveridad = computed(() =>
    Math.max(
      0,
      this.severidades.findIndex((s) => s.id === this.filtroSeveridad()),
    ),
  );
  protected idxOrigen = computed(() =>
    Math.max(
      0,
      this.origenes.findIndex((o) => o.id === this.filtroOrigen()),
    ),
  );

  /** Rango efectivo de fechas: el periodo manda; si no hay periodo, manda el rango de días. */
  protected readonly rangoEfectivo = computed((): { desde?: string; hasta?: string } => {
    const codigo = this.filtroPeriodo();
    if (codigo) {
      const p = this.periodos().find((x) => x.codigo === codigo);
      if (p) return rangoFechasPeriodo(p);
    }
    const dias = this.rangoDias();
    if (dias === 0) return {};
    const d = new Date();
    d.setHours(0, 0, 0, 0);
    d.setDate(d.getDate() - (dias - 1));
    return { desde: d.toISOString() };
  });

  // ---- Derivados ----
  protected readonly total = computed(() => this.items().length);
  protected readonly pendientes = computed(
    () => this.items().filter((a) => a.estado === 'PENDIENTE').length,
  );
  protected readonly criticasPendientes = computed(
    () =>
      this.items().filter((a) => a.estado === 'PENDIENTE' && a.nivelSeveridad === 'CRITICA')
        .length,
  );
  protected readonly atendidas = computed(
    () => this.items().filter((a) => a.estado === 'ATENDIDA').length,
  );
  protected readonly tasa = computed(() => tasaAtencion(this.items()));
  protected readonly sevCounts = computed(() => contarPorSeveridad(this.items()));
  protected readonly origenCounts = computed(() => contarPorOrigen(this.items()));
  protected readonly tendencia = computed(() => {
    const dias = this.rangoDias();
    if (this.filtroPeriodo()) {
      const r = this.rangoEfectivo();
      if (r.desde && r.hasta) return agruparPorRango(this.items(), new Date(r.desde), new Date(r.hasta));
    }
    return agruparPorDia(this.items(), dias === 0 ? 30 : dias);
  });
  protected readonly top8 = computed(() => topPersonasEnRiesgo(this.items(), this.nombres()));

  /** Paleta viva del documento. Lee el tema para reconstruir las opciones al alternar. */
  protected readonly temaVivo = computed<TemaGrafico>(() => {
    this.theme.enOscuro();
    return temaGrafico();
  });

  protected readonly opcionesTendencia = computed<EChartsOption>(() =>
    construirOpcionesTendencia(this.tendencia(), this.temaVivo()),
  );
  protected readonly opcionesSeveridad = computed<EChartsOption>(() =>
    construirOpcionesSeveridad(this.sevCounts(), this.temaVivo()),
  );
  protected readonly opcionesOrigen = computed<EChartsOption>(() =>
    construirOpcionesOrigen(this.origenCounts(), this.temaVivo()),
  );

  ngOnInit(): void {
    this.periodoCtrl.valueChanges.subscribe((o) => {
      this.filtroPeriodo.set(o?.id ?? '');
      this.cargar();
    });
    this.periodoSrv
      .listar()
      .pipe(catchError(() => of([] as Periodo[])))
      .subscribe((ps) => {
        this.periodos.set(ps);
        const vigente = ps.find((p) => p.estado === 'VIGENTE');
        if (vigente) this.filtroPeriodo.set(vigente.codigo);
        this.sincronizarPeriodoCtrl();
        this.cargar();
      });

    // Sincronizar rango de fechas cuando cambia el periodo
    this.periodoCtrl.valueChanges.subscribe((o) => {
      const codigo = o?.id ?? '';
      if (codigo) {
        const p = this.periodos().find((x) => x.codigo === codigo);
        if (p) {
          const rango = rangoFechasPeriodo(p);
          this.rangoCtrl.setValue(
            new TuiDayRange(
              TuiDay.fromLocalNativeDate(new Date(rango.desde)),
              TuiDay.fromLocalNativeDate(new Date(rango.hasta))
            ),
            { emitEvent: false }
          );
        }
      } else {
        this.rangoCtrl.setValue(null, { emitEvent: false });
      }
    });

    // Cuando el usuario cambia manualmente el rango, limpiar el periodo
    this.rangoCtrl.valueChanges.subscribe((rango) => {
      if (rango) {
        this.filtroPeriodo.set('');
      }
    });
  }

  private sincronizarPeriodoCtrl(): void {
    this.periodoCtrl.setValue(
      this.periodoOpts().find((o) => o.id === this.filtroPeriodo()) ?? null,
      { emitEvent: false },
    );
  }

  protected elegirRangoPorIndice(i: number): void {
    const r = this.rangos[i];
    if (!r) return;
    this.rangoDias.set(r.id);
    this.filtroPeriodo.set('');
    this.sincronizarPeriodoCtrl();
    this.cargar();
  }

  protected elegirSeveridadPorIndice(i: number): void {
    const s = this.severidades[i];
    if (!s) return;
    this.filtroSeveridad.set(s.id);
    this.cargar();
  }

  protected elegirOrigenPorIndice(i: number): void {
    const o = this.origenes[i];
    if (!o) return;
    this.filtroOrigen.set(o.id);
    this.cargar();
  }

  cargar(): void {
    this.loading.set(true);
    this.error.set(null);

    const rango = this.rangoEfectivo();

    forkJoin({
      alertas: this.alertaService
        .listar({
          ...(rango.desde ? { desde: rango.desde } : {}),
          ...(rango.hasta ? { hasta: rango.hasta } : {}),
          ...(this.filtroSeveridad() ? { severidad: this.filtroSeveridad() as SeveridadAlerta } : {}),
          ...(this.filtroOrigen() ? { tipoOrigen: this.filtroOrigen() as TipoEvaluacionOrigen } : {}),
          ordenarPor: 'fechaGeneracion',
          direccion: 'DESC',
        })
        .pipe(catchError(() => of([] as AlertaClinica[]))),
      nombres: this.nombresSrv.nombres().pipe(catchError(() => of(new Map<number, string>()))),
    }).subscribe({
      next: ({ alertas, nombres }) => {
        this.items.set(alertas);
        this.nombres.set(nombres);
        this.loading.set(false);
      },
      error: (err: HttpErrorResponse) => {
        this.error.set(
          err.status === 0
            ? 'No se pudo conectar con el servidor.'
            : 'No se pudo cargar el dashboard.',
        );
        this.loading.set(false);
      },
    });
  }

  protected iniciales(nombre: string): string {
    return nombre
      .split(' ')
      .filter((x) => x.length > 0 && !x.startsWith('Persona #'))
      .slice(0, 2)
      .map((x) => x.charAt(0).toUpperCase())
      .join('') || '•';
  }

  protected avatarAppearance(t: TopPersona): string {
    const top = this.top8();
    const max = Math.max(...top.map((x) => x.criticas), 0);
    if (t.criticas > 0 && t.criticas >= max && max > 0) return 'negative';
    if (t.criticas > 0) return 'neutral';
    return 'positive';
  }

  protected sevBadge(sev: string): string {
    return sev === 'CRITICA' ? 'negative' : sev === 'MODERADA' ? 'neutral' : 'positive';
  }
}

