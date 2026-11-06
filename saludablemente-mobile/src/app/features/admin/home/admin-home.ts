import { Component, OnInit, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { TuiButton, TuiIcon } from '@taiga-ui/core';
import { NgxEchartsDirective } from 'ngx-echarts';
import type { EChartsOption } from 'echarts';
import { forkJoin, of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { AuthService } from '../../../core/auth/auth-service';
import { ADMIN_MODULOS } from './admin-modulos';
import { PersonaService } from '../personal/persona/persona-service';
import { TeamService } from '../teams/team-service';
import { EvaluacionService } from '../nutricional/evaluacion/evaluacion-service';
import {
  AlertaClinica,
  SeveridadAlerta,
} from '../alertaclinica/alerta/alerta-clinica.model';
import { AlertaClinicaService } from '../alertaclinica/alerta/alerta-clinica-service';

export interface ResumenGeneral {
  personas: number;
  personasActivas: number;
  equipos: number;
  evaluaciones: number;
  alertasPendientes: number;
  alertasCriticas: number;
}

export function contarPorSeveridad(
  alertas: AlertaClinica[],
): Record<SeveridadAlerta, number> {
  const r: Record<SeveridadAlerta, number> = {
    LEVE: 0,
    MODERADA: 0,
    CRITICA: 0,
  };
  for (const a of alertas) {
    if (a.nivelSeveridad in r) r[a.nivelSeveridad] += 1;
  }
  return r;
}

export function construirOpcionesResumen(r: ResumenGeneral): EChartsOption {
  return {
    tooltip: { trigger: 'axis' },
    grid: { left: '8%', right: '6%', bottom: '12%', top: '14%' },
    xAxis: {
      type: 'category',
      data: ['Personas', 'Equipos', 'Evaluaciones', 'Alertas pend.'],
    },
    yAxis: { type: 'value', minInterval: 1 },
    series: [
      {
        type: 'bar',
        data: [r.personas, r.equipos, r.evaluaciones, r.alertasPendientes],
        itemStyle: { color: '#00a1ff', borderRadius: [8, 8, 0, 0] },
        label: { show: true, position: 'top' },
      },
    ],
  };
}

export function construirOpcionesSeveridad(
  sev: Record<SeveridadAlerta, number>,
): EChartsOption {
  return {
    tooltip: { trigger: 'item', formatter: '{b}: {c}' },
    legend: { bottom: 0 },
    series: [
      {
        type: 'pie',
        radius: ['45%', '68%'],
        center: ['50%', '44%'],
        label: { show: false },
        data: [
          { name: 'Leves', value: sev.LEVE, itemStyle: { color: '#16cdc7' } },
          { name: 'Moderadas', value: sev.MODERADA, itemStyle: { color: '#ffae1f' } },
          { name: 'Críticas', value: sev.CRITICA, itemStyle: { color: '#e57373' } },
        ],
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

@Component({
  selector: 'app-admin-home',
  imports: [
    RouterLink,
    TuiButton,
    TuiIcon,
    NgxEchartsDirective,
    DatePipe,
  ],
  templateUrl: './admin-home.html',
  styleUrl: './admin-home.css',
})
export class AdminHome implements OnInit {
  protected readonly auth = inject(AuthService);
  private readonly personasSrv = inject(PersonaService);
  private readonly equiposSrv = inject(TeamService);
  private readonly evaluacionesSrv = inject(EvaluacionService);
  private readonly alertasSrv = inject(AlertaClinicaService);

  protected readonly modulos = ADMIN_MODULOS;
  protected readonly graficosActivos = signal(tieneCanvas());

  protected readonly cargando = signal(true);
  protected readonly resumen = signal<ResumenGeneral>({
    personas: 0,
    personasActivas: 0,
    equipos: 0,
    evaluaciones: 0,
    alertasPendientes: 0,
    alertasCriticas: 0,
  });
  protected readonly ultimasAlertas = signal<AlertaClinica[]>([]);
  protected readonly maxPuntaje = signal(0);

  protected pillSeveridad(s: SeveridadAlerta): string {
    return s === 'CRITICA'
      ? 'pill pill-error'
      : s === 'MODERADA'
        ? 'pill pill-warning'
        : 'pill pill-secondary';
  }

  protected inicial(texto: string): string {
    const t = (texto ?? '').trim();
    return t.length > 0 ? t.charAt(0).toUpperCase() : '•';
  }

  protected tileAlerta(s: SeveridadAlerta): string {
    return s === 'CRITICA'
      ? 'tile tile-circle tile-soft-error avatar-letra'
      : s === 'MODERADA'
        ? 'tile tile-circle tile-soft-warning avatar-letra'
        : 'tile tile-circle tile-soft-secondary avatar-letra';
  }
  protected readonly opcionesResumen = signal<EChartsOption>({});
  protected readonly opcionesSeveridad = signal<EChartsOption>({});

  ngOnInit(): void {
    forkJoin({
      personas: this.personasSrv.listar().pipe(catchError(() => of([]))),
      equipos: this.equiposSrv.listar().pipe(catchError(() => of([]))),
      agregado: this.evaluacionesSrv
        .agregados()
        .pipe(catchError(() => of({ total: 0 }))),
      pendientes: this.alertasSrv
        .listar({ estado: 'PENDIENTE' })
        .pipe(catchError(() => of([]))),
    }).subscribe(({ personas, equipos, agregado, pendientes }) => {
      const sev = contarPorSeveridad(pendientes);
      this.resumen.set({
        personas: personas.length,
        personasActivas: personas.filter((p) => p.activo).length,
        equipos: equipos.length,
        evaluaciones: agregado?.total ?? 0,
        alertasPendientes: pendientes.length,
        alertasCriticas: sev.CRITICA,
      });
      const ordenadas = [...pendientes].sort((a, b) =>
        b.fechaGeneracion.localeCompare(a.fechaGeneracion),
      );
      this.ultimasAlertas.set(ordenadas.slice(0, 5));
      this.maxPuntaje.set(
        ordenadas.reduce((m, a) => Math.max(m, a.puntajeTotal), 0),
      );
      if (this.graficosActivos()) {
        this.opcionesResumen.set(construirOpcionesResumen(this.resumen()));
        this.opcionesSeveridad.set(construirOpcionesSeveridad(sev));
      }
      this.cargando.set(false);
    });
  }

  protected sinImagen(event: Event): void {
    (event.target as HTMLImageElement | null)?.remove();
  }
}
